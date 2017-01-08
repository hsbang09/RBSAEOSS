/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCursor;
import com.mongodb.DBCursor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import jess.Fact;
import jess.Rete;


/**
 *
 * @author Bang
 */
public class DBManagement {
    
    private MongoClient mongoClient;
    private String dbName = "arch_data";
    private String metaDataCollectionName = "metadata";
    private ArrayList<String> dataCollectionNames;
    private static DBManagement instance = null;
    
    private MongoDatabase Mdb;


    
    public DBManagement(){
        try{            
            mongoClient = new MongoClient( "localhost" , 27017 );
            Mdb = mongoClient.getDatabase(this.dbName);
            dataCollectionNames = new ArrayList<>();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }    
    
    public DBManagement(String dbName){
        try{            
            this.dbName = dbName;
            mongoClient = new MongoClient( "localhost" , 27017 );
            Mdb = mongoClient.getDatabase(this.dbName);
            dataCollectionNames = new ArrayList<>();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }





    public void setCollectionNames(ArrayList<String> colNames){
        this.dataCollectionNames = colNames;
    }
    
    public void createNewDB(){
        boolean dbExists = false;
        MongoCursor<String> iter = mongoClient.listDatabaseNames().iterator();
        while(iter.hasNext()){
            if(iter.next().equals(dbName)){
                dbExists = true;
            }
        }   
        if(dbExists) {
            mongoClient.getDatabase(dbName).drop();
        }
        Mdb = mongoClient.getDatabase(dbName);
    }
    public void createNewCollections(){
        DB db = mongoClient.getDB(dbName);

        if(db.collectionExists(metaDataCollectionName)){
            db.getCollection(metaDataCollectionName).drop();
        }
        for(String col:this.dataCollectionNames){
            if(db.collectionExists(col)){
                db.getCollection(col).drop();
            }        
        }
    }
    
    
    
    
    
    
    public void encodeMetadata(int id, String bitString){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(metaDataCollectionName);
        col.insertOne(
                new Document()
                    .append("id", id)
                    .append("bitString",bitString)
        );
    }
    
    public void encodeData(String collectionName, Rete r, QueryBuilder qb){
        MongoDatabase Mdb = mongoClient.getDatabase(dbName);
        MongoCollection col = Mdb.getCollection(collectionName);

        ArrayList<Integer> factsToEncode = new ArrayList<>();
        ArrayList<Integer> factsEncoded = new ArrayList<>();
            
        try{
            jess.Fact value = qb.makeQuery("AGGREGATION::VALUE").get(0);
            JessFactHandler jfh = new JessFactHandler(value, r, qb);
            factsToEncode = jfh.getParentFactIDs();

            int cnt =0;
            while(factsToEncode.size() > 0){
                // Get the first element from the array and remove it from the list
                jess.Fact thisFact = r.findFactByID(factsToEncode.get(0));
                // Encode the fact
                org.bson.Document doc = encodeFact(thisFact,r,qb);
                col.insertOne(doc);
                // Remove the encoded fact from the list
                factsToEncode.remove(0);
                factsEncoded.add(thisFact.getFactId());
                // Get new list of facts to encode
                jfh.setNewFact(thisFact);
                ArrayList<Integer> newFacts = jfh.getParentFactIDs();
                for(int fid:newFacts){
                    // If it is a new fact, add it to the list
                    if(!factsToEncode.contains(fid) && !factsEncoded.contains(fid)){
                        factsToEncode.add(fid);
                    }
                }           
                cnt++;
                if(cnt>3000){break;}
            }
            System.out.println(factsEncoded.size());

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public org.bson.Document encodeFact(jess.Fact f, Rete r, QueryBuilder qb){
        
        org.bson.Document doc = new org.bson.Document();
        doc.append("factName", f.getName());
        doc.append("factID",f.getFactId());
        doc.append("module", f.getModule());
        
        try{
            jess.Deftemplate factTemplate = f.getDeftemplate();
            String[] slots = factTemplate.getSlotNames();
            
            for(int i=0;i<slots.length;i++){
                String slot = slots[i];
                jess.Value slotVal = f.getSlotValue(slot);
                String slotVal_string = slotVal.toString();
                
                if(slotVal_string.contains("Java-Object")){
                    // Java Objects are not saved in the database for now
                    continue;
                }else if(slotVal_string.isEmpty() || slotVal_string.equals("\"\"")){
                    // Value is empty
                    continue;
                }
                
                if(factTemplate.isMultislot(i)){
                    // Save as string
                    doc.append(slot, slotVal_string);
                }else{
                    // Not a multi-slot
                    if(!slotVal_string.equals("nil")){
                        // If the value is nil then don't save it in the DB
                        if(slotVal.isNumeric(r.getGlobalContext())){
                            if(slotVal_string.contains(".")){ 
                                // float
                                double slotVal_double = slotVal.floatValue(r.getGlobalContext());
                                doc.append(slot,slotVal_double);
                            }else{ 
                                // integer
                                int slotVal_int = slotVal.intValue(r.getGlobalContext());
                                doc.append(slot,slotVal_int);
                            }
                        }else{
                            // Save as string
                            String slotVal_string2 = slotVal.stringValue(r.getGlobalContext());
                            doc.append(slot, slotVal_string2);
                        }
                    }
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(f.getFactId());
        }
        return doc;
    }
    
    
    


//    public ArrayList<String>[] queryAllCandidateFeatures(){
//        
//        ArrayList<String>[] candidateFeatures = new ArrayList[2];
//        ArrayList<String> cfn = new ArrayList<>(); // candidate feature names
//        ArrayList<String> cfe = new ArrayList<>(); // candidate feature expressions
//        
//        DB db = mongoClient.getDB(dbName);
//        DBCollection col = db.getCollection("asdfasdf");
//        DBCursor cursor = col.find();
//        
//        while(cursor.hasNext()){
//            DBObject doc = cursor.next();
//            cfn.add((String) doc.get("name"));
//            cfe.add((String) doc.get("expression"));
//        }
//        candidateFeatures[0] = cfn;
//        candidateFeatures[1] = cfe;
//        return candidateFeatures;
//    }
    public void queryAllArchitecture(){
//        DB db = mongoClient.getDB(dbName);
//        DBCollection col = db.getCollection(dataCollectionName);
//        DBCursor cursor = col.find();
//        ArrayList<Architecture> allArch = new ArrayList<>();
//        while(cursor.hasNext()){
//            DBObject doc = cursor.next();
//            int id = (int) doc.get("id");
//            ArrayList<String> inputs = (ArrayList<String>) doc.get("inputs");
//            ArrayList<String> outputs = (ArrayList<String>) doc.get("outputs");
//        }
    }
    
    public void queryArchitecture(int id){
//        DB db = mongoClient.getDB(dbName);
//        DBCollection col = db.getCollection(dataCollectionName);
//        BasicDBObject whereQuery = new BasicDBObject();
//        whereQuery.put("id", id);
//        DBCursor cursor = col.find(whereQuery);
//        DBObject doc = cursor.one();
//        ArrayList<String> inputs = (ArrayList<String>) doc.get("inputs");
//        ArrayList<String> outputs = (ArrayList<String>) doc.get("outputs");
    }
    
//    public ArrayList<String> queryInputNames(){
//        DB db = mongoClient.getDB(dbName);
//        DBCollection col = db.getCollection(metaDataCollectionName);
//        DBCursor cursor = col.find();
//        DBObject doc = cursor.one();
//        ArrayList<String> inputNames = (ArrayList<String>) doc.get("inputNames");
//        return inputNames;
//    }
    
    
    
    public class JessFactHandler{
        
        private jess.Fact f;
        private int factID;
        private String factName;
        private String factHistory;
        private Rete r;
        private QueryBuilder qb;
        
        public JessFactHandler(jess.Fact f, Rete r, QueryBuilder qb){
            try{
                this.factID = f.getFactId();
                this.factName = f.getName();
                String facthis = f.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                
//              "{R110 {R112 {R112 {R112 F672 S675} S674} S673}}"
                this.factHistory = facthis.replace('{', '(').replace('}',')');
                this.r = r;
                this.qb = qb;
                
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public String getName(){return this.factName;}
        public int getID(){return this.factID;}
        
        public void setNewFact(jess.Fact f){
            try{
                this.factID = f.getFactId();
                this.factName = f.getName();
                String facthis = f.getSlotValue("factHistory").stringValue(r.getGlobalContext());
                this.factHistory = facthis.replace('{', '(').replace('}',')');
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        
        /**
         * Returns a list of ID's of the Facts that are used to generate and modify the current Fact
         * 
         * @return ArrayList of factIDs
         */
        public ArrayList<Integer> getParentFactIDs(){
            ArrayList<Integer> factIDs = new ArrayList<>();

            JessExpressionAnalyzer jea = JessExpressionAnalyzer.getInstance();
            int level = jea.getNestedParenLevel(this.factHistory);
            
            for (int i=0; i < level; i++){
                
                String inside = jea.getInsideParen(factHistory,i+1);
                String[] insideSplit = inside.split(" ", 2);
                if (insideSplit[0].substring(1).equalsIgnoreCase("nil")){
//                    System.out.println("nil found: " + this.factID);
//                    System.out.println(factHistory);
                    continue;
                }
                // First element of a factHistory should be a rule
                int ruleID = Integer.parseInt(insideSplit[0].substring(1));
                String rest = insideSplit[1];
                rest = jea.collapseAllParenIntoSymbol(rest);
                
                String[] restSplit = rest.split(" ");                
                for (String tmp:restSplit){
                    if((tmp.startsWith("A")) || (tmp.startsWith("F")) || (tmp.startsWith("S")) || (tmp.startsWith("D")) || (tmp.startsWith("J"))){
                        // A: Newly asserted
                        // F: Modified
                        // S: Slot values are used
                        // D: Duplicated
                        // J: Asserted from Java
                        
                        
                        if(tmp.startsWith("F")){continue;}
                        
                        int id = Integer.parseInt(tmp.substring(1));
                        if(!factIDs.contains(id)){
                            factIDs.add(id);
                        }
                    }
                }                
            }            
            return factIDs;
        }
        

        
        
    }
        
        
    
   
    
    
    
    
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.local;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultCollection;
import rbsa.eoss.ResultManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import rbsa.eoss.DBManagement;


/**
 *
 * @author Bang
 */
public class RBSAEOSSEval {
    
    public static void main(String[] args){
        
        // Set a path to the project folder
        String path = "/Users/bang/workspace/RBSAEOSS";
        
        // Initialization
        ArchitectureEvaluator AE = ArchitectureEvaluator.getInstance();
        ArchitectureGenerator AG = ArchitectureGenerator.getInstance();
        ResultManager RM = ResultManager.getInstance();
        Params params = null;
        String search_clps = "";
        
        params = new Params( path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP
        AE.init(1);        


//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Evaluate single architecture ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////        
        

//        long t0 = System.currentTimeMillis();
//        
////
//        // Input a new architecture design
//        // There must be 5 orbits. Instrument name is represented by a capital letter, taken from {A,B,C,D,E,F,G,H,I,J,K,L}
//        ArrayList<String> input_arch = new ArrayList<>();
//        String orbit_1 = ""; input_arch.add(orbit_1);
//        String orbit_2 = ""; input_arch.add(orbit_2);
//        String orbit_3 = ""; input_arch.add(orbit_3);
//        String orbit_4 = ""; input_arch.add(orbit_4);
//        String orbit_5 = "HKL"; input_arch.add(orbit_5);
//
//        // Generate a new architecture
//        Architecture architecture = AG.defineNewArch(input_arch);
//        
//        //architecture = AG.getMaxArch();
//        
//        //Architecture architecture = new Architecture("110000110001000000000100000001000000000011000001100000010011",1);
//        
//        // Evaluate the architecture
//        Result result = AE.evaluateArchitecture(architecture,"Slow");
//        
//        // Save the score and the cost
//        double cost = result.getCost();
//        double science = result.getScience();
//        
//        System.out.println("Performance Score: " + science + ", Cost: " + cost);
//        
//        
//        long t1 = System.currentTimeMillis();
//        System.out.println( "Evaluation done in: " + String.valueOf(t1-t0) + " msec");
        
        

//////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////// Read in csv file and evaluate architectures ////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////


        int n = 6;
        int start = 0;

        long t0 = System.currentTimeMillis();

        String line = "";
        String splitBy = ",";
        
        // Read in a csv file and count the number of architectures already evaluated
        String writePath = "/Users/bang/workspace/RBSAEOSS/results/EOSS_data_recalculated.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(writePath))) {
        	
            while ((br.readLine()) != null) {start++;}
            br.close();
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // Read in a csv file and evaluate the architectures
        ArrayList<String> bitStrings = new ArrayList<>();
        String resultPath = "/Users/bang/workspace/RBSAEOSS/results/EOSS_data.csv";
        
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(resultPath))) {
        	
            while ((line = br.readLine()) != null && i< start+n) {
                
                if(i >= start){
                    // use comma as separator
                    String[] tmp = line.split(splitBy);
                    String bitString = tmp[0];
                    bitStrings.add(bitString);
                    double science = Double.parseDouble(tmp[1]);
                    double cost = Double.parseDouble(tmp[2]);
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        i=0;
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(writePath,true));
            
            for(String bitString:bitStrings){
                Architecture architecture = new Architecture(bitString,1);

                // Evaluate the architecture
                Result result = AE.evaluateArchitecture(architecture,"Slow");
                double sci = result.getScience();
                double cos = result.getCost();
                
                writer.write(bitString+","+sci+","+cos + "\n");
                
                System.out.println(i+ ":  "+ bitString+","+sci+","+cos);
                i++;
            }
            writer.flush();
            writer.close();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        

        long t1 = System.currentTimeMillis();
        System.out.println( "Evaluation done in: " + String.valueOf(t1-t0) + " msec");


//////////////////////////////////////////////////////////////////////////////////////////////////
////////////////// Read in a result file and write in a csv format  //////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////


//        //   Reading in the result file and writing a csv file.
//        try{
//            PrintWriter writer = new PrintWriter("/Users/bang/workspace/EOSS_NSGA_result_201705/2017-04-05_01-09-33_test.csv", "UTF-8");
//
//            // Load the existing result file
//            String result_path = "/Users/bang/workspace/EOSS_NSGA_result_201705/2017-04-05_01-09-33_test.rs";
//            Stack<Result> results;
//            ResultCollection RC = RM.loadResultCollectionFromFile(result_path);
//            Stack<Result> tmpResults = RC.getResults();
//            results = new Stack<Result>();
//            for (Result tmpResult:tmpResults){
//                if(tmpResult.getScience()>=0.001){
//                    results.add(tmpResult);
//                }
//            }
//            int nResults = results.size();
//            for (int i=0;i<nResults;i++){
//                double sci = results.get(i).getScience();
//                double cos = results.get(i).getCost();
//                Architecture arch = results.get(i).getArch();
//                
//                String bitString="";
//                for(boolean bool:arch.getBitString()){
//                    if(bool){bitString=bitString+"1";}
//                    else{bitString=bitString+"0";}
//                }
//                writer.println(bitString+","+sci+","+cos);
//            }
//	        
////            result_path = Params.path + "/results/3.rs";
////            RC = RM.loadResultCollectionFromFile(result_path);
////            tmpResults = RC.getResults();
////            results = new Stack<Result>();
////            for (Result tmpResult:tmpResults){
////                if(tmpResult.getScience()>=0.001){
////                    results.add(tmpResult);
////                }
////            }
////            nResults = results.size();
////            for (int i=0;i<nResults;i++){
////                double sci = results.get(i).getScience();
////                double cos = results.get(i).getCost();
////                Architecture arch = results.get(i).getArch();
////                
////                String bitString="";
////                for(boolean bool:arch.getBitString()){
////                    if(bool){bitString=bitString+"1";}
////                    else{bitString=bitString+"0";}
////                }
////                writer.println(bitString+","+sci+","+cos);
////            }
//	        
//        
//            writer.close();
//        } catch (IOException e) {
//            // do something
//        }






//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Print all rule names ////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////

//        File file = new File(path+"/results/rules.csv");
//        
//        
//        
//        
//        try{
//                    
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            String printRow = "";
//
//            HashMap rules = Params.rules_IDtoName_map;
//            Set idSet = rules.keySet();
//            Iterator iter = idSet.iterator();
//            while(iter.hasNext()){
//                String row = "";
//                int key = (int) iter.next();
//                row = key + "," + Params.rules_IDtoName_map.get(key);
//                writer.write(row + "\n");
//            }
//
//            System.out.println("Done");
//            writer.close();
//        
//        
//        
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        


//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////// Make Queries ////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
        

// Configure the database
// DBManagement dbm = new DBManagement();
// Initialize the database - do it only once
// dbm.createNewDB();
// dbm.encodeRules();





//    Query Data    

//    ArrayList<String> slots = new ArrayList<>();
//    ArrayList<String> conditions = new ArrayList<>();
//    ArrayList<String> values = new ArrayList<>();
//    ArrayList<String> valueTypes = new ArrayList<>();
    
//    slots.add("average-power#");
//    conditions.add("gt");
//    values.add("100");
//    valueTypes.add("Double");
//    
    
//    dbm.makeQuery("science","CAPABILITIES::Manifested-instrument",slots,conditions,values,valueTypes); 
    
    
    System.out.println("Done");

    }
 
}

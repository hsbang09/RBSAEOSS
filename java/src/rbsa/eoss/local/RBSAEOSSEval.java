/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.local;


import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultCollection;
import rbsa.eoss.ResultManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import rbsa.eoss.DBManagement;


/**
 *
 * @author Bang
 */
public class RBSAEOSSEval {
    
    public static void main(String[] args){
        
        // Set a path to the project folder
        String path = "/Users/bang/Documents/workspace/RBSAEOSS-Eval";
        
        // Configure the database
        DBManagement dbm = new DBManagement();
//        dbm.createNewDB();
                
//        
//        // Initialization
//        ArchitectureEvaluator AE = ArchitectureEvaluator.getInstance();
//        ArchitectureGenerator AG = ArchitectureGenerator.getInstance();
//        ResultManager RM = ResultManager.getInstance();
//        Params params = null;
//        String search_clps = "";
//        params = new Params( path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP
//        AE.init(1);
//        
//        // Input a new architecture design
//        // There must be 5 orbits. Instrument name is represented by a capital letter, taken from {A,B,C,D,E,F,G,H,I,J,K,L}
//        ArrayList<String> input_arch = new ArrayList<>();
//        String orbit_1 = "ALH"; input_arch.add(orbit_1);
//        String orbit_2 = "KG"; input_arch.add(orbit_2);
//        String orbit_3 = ""; input_arch.add(orbit_3);
//        String orbit_4 = ""; input_arch.add(orbit_4);
//        String orbit_5 = "BHK"; input_arch.add(orbit_5);
//
//        // Generate a new architecture
//        Architecture architecture = AG.defineNewArch(input_arch);
//        
//        
//        // Evaluate the architecture
//        Result result = AE.evaluateArchitecture(architecture,"Slow");
//        
//        // Save the score and the cost
//        double cost = result.getCost();
//        double science = result.getScience();
//        
//        System.out.println("Performance Score: " + science + ", Cost: " + cost);
        





        
//        try{
//            PrintWriter writer = new PrintWriter("EOSS_data.csv", "UTF-8");
//
//
//	        // Load the existing result file
//            String result_path = Params.path + "/results/1.rs";
//	        Stack<Result> results;
//	        ResultCollection RC = RM.loadResultCollectionFromFile(result_path);
//	        Stack<Result> tmpResults = RC.getResults();
//	        results = new Stack<Result>();
//	        for (Result tmpResult:tmpResults){
//	            if(tmpResult.getScience()>=0.001){
//	                results.add(tmpResult);
//	            }
//	        }
//	        int nResults = results.size();
//	        for (int i=0;i<nResults;i++){
//	            double sci = results.get(i).getScience();
//	            double cos = results.get(i).getCost();
//	            Architecture arch = results.get(i).getArch();
//	            writer.println(sci+","+cos);
//	        }
//	        
//            result_path = Params.path + "/results/3.rs";
//	        RC = RM.loadResultCollectionFromFile(result_path);
//	        tmpResults = RC.getResults();
//	        results = new Stack<Result>();
//	        for (Result tmpResult:tmpResults){
//	            if(tmpResult.getScience()>=0.001){
//	                results.add(tmpResult);
//	            }
//	        }
//	        nResults = results.size();
//	        for (int i=0;i<nResults;i++){
//	            double sci = results.get(i).getScience();
//	            double cos = results.get(i).getCost();
//	            Architecture arch = results.get(i).getArch();
//	            writer.println(sci+","+cos);
//	        }
//	        
//        
//            writer.close();
//        } catch (IOException e) {
//            // do something
//        }
        
        

    ArrayList<String> slots = new ArrayList<>();
    ArrayList<String> conditions = new ArrayList<>();
    ArrayList<String> values = new ArrayList<>();
    ArrayList<String> valueTypes = new ArrayList<>();
    
//    slots.add("average-power#");
//    conditions.add("gt");
//    values.add("100");
//    valueTypes.add("Double");
//    
    
    dbm.makeQuery("science","CAPABILITIES::Manifested-instrument",slots,conditions,values,valueTypes); 
    
    System.out.println("Done");

    }
 
}

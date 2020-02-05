import BIT.highBIT.*;
import BIT.lowBIT.CONSTANT_InterfaceMethodref_Info;
import BIT.lowBIT.CONSTANT_Methodref_Info;
import BIT.lowBIT.CONSTANT_NameAndType_Info;
import BIT.lowBIT.CONSTANT_String_Info;
import BIT.lowBIT.CONSTANT_Utf8_Info;

import java.io.*;
import java.util.*;

public class MetricsCollector {
        
	private static PrintStream out = null;

        public static void main(String argv[]) {

	        String file_in = argv[0];
        
        	if (file_in.endsWith(".class")) {
        		ClassInfo ci = new ClassInfo(file_in);
	        	String className = file_in.substring(file_in.lastIndexOf("/")+1,file_in.indexOf("."));    	
        	     	
        		if(className.equals("AStarStrategy") || className.equals("BFSStrategy") || className.equals("DFSStrategy")) {
        			for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();) {
                		        Routine routine = (Routine) e.nextElement();
					routine.addBefore("MetricsCollector", "methodStrategy", new Integer(1));
	                    	        if(routine.getMethodName().equals("solve")) {
	                            		addMemoryAllocs(routine,1);
				    	        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
	                        		BasicBlock bb = (BasicBlock) b.nextElement();
	                        	
					        bb.addBefore("MetricsCollector", "basicBlocksStrategy", new Integer(bb.size()));
	                	
						}		               
                        		}
        	       		}
        		}
        	
	    		if(className.equals("Coordinate")) {
        			for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();) {
                			Routine routine = (Routine) e.nextElement();
					routine.addBefore("MetricsCollector", "methodCoordinate", new Integer(1));
					if(routine.getMethodName().equals("getUnvisitedNeighboors") || routine.getMethodName().equals("getAllNeighboors")) {
			                    	addMemoryAllocs(routine,2);
	                                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
		    	                        	BasicBlock bb = (BasicBlock) b.nextElement();
	    			                        bb.addBefore("MetricsCollector", "basicBlocksCoordinate", new Integer(bb.size()));	                    		
		    			        }
					}                    
                                }
    		        }
		
			ci.write(file_in);
	       
		}
        }	

	public static synchronized void addMemoryAllocs(Routine routine, int type) {
    		for (Instruction instruction : routine.getInstructions()) {
            		
			int opcode = instruction.getOpcode();
		        if ((opcode == InstructionTable.NEW) || (opcode == InstructionTable.newarray) || (opcode == InstructionTable.anewarray) || (opcode == InstructionTable.multianewarray)) {                		    	    instruction.addBefore("MetricsCollector", "allocsCount", type);
		        }
        	}
        }

	public static synchronized void allocsCount(int incr) {
    		Metrics metrics = WebServer.getHashMap().get(Thread.currentThread().getId());
	    	if(incr == 1) {
		    	metrics.setMemoryAllocsStrategy(metrics.getMemoryAllocsStrategy() + 1);
    		}
	    	else {
 		    	metrics.setMemoryAllocsCoordinate(metrics.getMemoryAllocsCoordinate() + 1);
	    	}
	}   

	public static synchronized void methodStrategy(int incr) {
    		Metrics metrics = WebServer.getHashMap().get(Thread.currentThread().getId());   
		metrics.setMethodCountStrategy(metrics.getMethodCountStrategy() + 1); 
	}

	public static synchronized void methodCoordinate(int incr) {
    		Metrics metrics = WebServer.getHashMap().get(Thread.currentThread().getId());   
		metrics.setMethodCountCoordinate(metrics.getMethodCountCoordinate() + 1); 
	} 
    
    	public static synchronized void basicBlocksStrategy(int incr) {
    		Metrics metrics = WebServer.getHashMap().get(Thread.currentThread().getId());    
		metrics.setBasicBlocksStrategy(metrics.getBasicBlocksStrategy() + 1);
		metrics.setInstructionCountStrategy(metrics.getInstructionCountStrategy() + incr);
	}

	public static synchronized void basicBlocksCoordinate(int incr) {
    		Metrics metrics = WebServer.getHashMap().get(Thread.currentThread().getId());
	    	metrics.setBasicBlocksCoordinate(metrics.getBasicBlocksCoordinate() + 1);
		metrics.setInstructionCountCoordinate(metrics.getInstructionCountCoordinate() + incr);
	}

}


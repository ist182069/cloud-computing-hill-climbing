
import BIT.highBIT.*;
import BIT.lowBIT.CONSTANT_InterfaceMethodref_Info;
import BIT.lowBIT.CONSTANT_Methodref_Info;
import BIT.lowBIT.CONSTANT_NameAndType_Info;
import BIT.lowBIT.CONSTANT_String_Info;
import BIT.lowBIT.CONSTANT_Utf8_Info;


import java.io.*;
import java.util.*;


public class Metrics {
    private static PrintStream out = null;
    private static int i_count = 0, b_count = 0, m_count = 0;
    private static int loopsExecution = 0;

    public static void main(String argv[]) {
        String file_in = argv[0];

        System.out.println("CARALHO");
        
        if (file_in.endsWith(".class")) {
        	ClassInfo ci = new ClassInfo(file_in);
        	String className = file_in.substring(file_in.lastIndexOf("/")+1,file_in.indexOf("."));    	

            if(className.equals("Main")) {
            	for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                  Routine routine = (Routine) e.nextElement();
                  if(routine.getMethodName().equals("main")){
                  	routine.addBefore("Metrics", "startTimer", 0);
                  	routine.addAfter("Metrics", "endTimer", 0);
                  }
            	}
            }

        	if(className.equals("AStarStrategy") || className.equals("BreadthFirstSearchStrategy")) {
        		for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    if(routine.getMethodName().equals("run")){
                    	for(Instruction instruction : routine.getInstructions()) {
                    		if(instruction.getOpcode() == InstructionTable.invokestatic) {
                                short nameAndTypeIndex = ((CONSTANT_Methodref_Info) routine.getConstantPool()[instruction.getOperandValue()]).name_and_type_index;
                    			short nameIndex = ((CONSTANT_NameAndType_Info) routine.getConstantPool()[nameAndTypeIndex]).name_index;
                    			byte[] byteFunctionName = ((CONSTANT_Utf8_Info) routine.getConstantPool()[nameIndex]).bytes;
                    			try {
                    				String functionName = new String(byteFunctionName, "UTF-8");
                    				if(functionName.equals("run")) {
                        				System.out.println(functionName);
                    					instruction.addBefore("Metrics", "strategyRunning", 0);
                    				}
                    			} catch(Exception a) {				
                    			}
                    		}
                    	}
                    }
                }
        	}

        	if(className.equals("DepthFirstSearchStrategy")) {
        		for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine =  (Routine) e.nextElement();
                    if(routine.getMethodName().equals("solveAux")){
    					routine.addBefore("Metrics", "strategyRunning", 0);
                    }
        		}
            }


            ci.addAfter("Metrics", "printMetrics", ci.getClassName());
            ci.write(file_in);
        }
    }

    public static synchronized void printMetrics(String foo) {
        System.out.println(" Loops Executed:" + loopsExecution);
    }

    public static synchronized void strategyRunning(int incr) {
    	loopsExecution++;
    }
}

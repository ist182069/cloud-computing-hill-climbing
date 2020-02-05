    /* ICount.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */

import BIT.highBIT.*;
import java.io.*;
import java.util.*;


public class CountAlgorithmsOpCode {
    private static PrintStream out = null;
    private static int i_count = 0;
    private static HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
    private static String currentMethodName = null;

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        String file_name = argv[0];

        if (file_name.endsWith(".class")) {
            // create class info object
            ClassInfo ci = new ClassInfo(file_name);
            
	    int i = 0;
            // loop through all the routines
            // see java.util.Enumeration for more information on Enumeration class
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
		
                Instruction[] instructions = routine.getInstructions();
                for(Instruction instIter : instructions) {
                    String name = routine.getMethodName();
                    String currentOpcode = Integer.toString(instIter.getOpcode());
		    if(name.equals("solve")) {
                        i++;
                        routine.addBefore("CountAlgorithmsOpCode", "simpleInstructionCount", i);
                        instIter.addBefore("CountAlgorithmsOpCode", "countOp", currentOpcode);
    		        // JB: Nesta linha o que eu faco e ver se a rotina corresponde ao solve e se vamos sair. Se isso acontecer escreve para um ficheiro.
                        instIter.addBefore("CountAlgorithmsOpCode", "methodName", name);
                        instIter.addBefore("CountAlgorithmsOpCode", "exitWrite", instIter.getOpcode());
                    }
                }
            }
	ci.write(argv[1]);
        }
    }

    public static synchronized void printICount(String foo) {
        Iterator iter = hashMap.entrySet().iterator();
	while(iter.hasNext()) {
		Map.Entry pair = (Map.Entry)iter.next();
	        System.out.println(pair.getKey() + " / " + pair.getValue());
	        iter.remove();
	}
    }

    public static synchronized void countOp(String opCode) {
        if(hashMap.containsKey(opCode)) {
            int i = hashMap.get(opCode) + 1;
            hashMap.put(opCode, i);
        } else {
            int i = 1;
            hashMap.put(opCode, i);
        }
    }

    public static synchronized void methodName(String name) {
        currentMethodName = name;
    }
    
    public static synchronized void simpleInstructionCount(int count) {
        i_count = count;    
    }

    public static synchronized void exitWrite(int opcode) {
        try {
            if(currentMethodName.equals("solve") && opcode==177) {
		System.out.println("Entrou no if");
                BufferedWriter bw = new BufferedWriter(new FileWriter("instruction_count.txt"));
                bw.write("Instructions: ");
                bw.write(String.valueOf(i_count));
                bw.newLine();

                Iterator iter = hashMap.entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry pair = (Map.Entry)iter.next();
                    bw.write(pair.getKey() + " / " + pair.getValue());
                    bw.newLine();
                    iter.remove();
                }
                bw.close();    
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Could not write to file with path \"~/instruction_count.txt\"...");
        }
      
    }

    private static void foo() {
        for(int i = 0; i<3; i++) {
            System.out.println("foo");
        }
    }

}


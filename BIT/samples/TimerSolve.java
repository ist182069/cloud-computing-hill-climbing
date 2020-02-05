import BIT.highBIT.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimerSolve {
    private static String currentMethodName = null;
    private static boolean timer = false;
    private static long startTime;
    private static long endTime;

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        String file_name = argv[0];

        if (file_name.endsWith(".class")) {
            // create class info object
            ClassInfo ci = new ClassInfo(file_name);
            
            // loop through all the routines
            // see java.util.Enumeration for more information on Enumeration class
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
		
                Instruction[] instructions = routine.getInstructions();
                for(Instruction instIter : instructions) {
                    String name = routine.getMethodName();
                    String currentOpcode = Integer.toString(instIter.getOpcode());
		    if(name.equals("solve")) {
    		        // JB: Nesta linha o que eu faco e ver se a rotina corresponde ao solve e se vamos sair. Se isso acontecer escreve para um ficheiro.
    		        instIter.addBefore("TimerSolve", "countTimer", "");
                        instIter.addBefore("TimerSolve", "methodName", name);
                        instIter.addBefore("TimerSolve", "exitWrite", instIter.getOpcode());
                    }
                }
            }
	ci.write(argv[1]);
        }
    }

    public static synchronized void methodName(String name) {
        currentMethodName = name;
    }

    public static synchronized void countTimer(String notUsed) {
    	if(!timer) {
            startTime = System.nanoTime();
	    timer = true;
	}
    }
    
    public static synchronized void exitWrite(int opcode) {
        try {
            if(currentMethodName.equals("solve") && opcode==177) {
		endTime = System.nanoTime() - startTime; 
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
	
                BufferedWriter bw = new BufferedWriter(new FileWriter("timer_results/" + dateFormat.format(date) + ".txt"));
		bw.write(Long.toString(endTime));
	        bw.close();
	     }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Could not write to file with path \"~/timer_solve.txt\"...");
        }
      
    }

}


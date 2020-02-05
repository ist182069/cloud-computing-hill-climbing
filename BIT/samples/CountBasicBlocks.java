import BIT.highBIT.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CountBasicBlocks {
    private static String currentMethodName = null;
    private static boolean timer = false;
    private static int bb_count = 0;
    private static int i_count = 0;
    private static int m_count = 0;

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
	        routine.addBefore("CountBasicBlocks", "countMethod", new Integer(1));
                    
                for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("CountBasicBlocks", "countMethod", new Integer(bb.size()));
			routine.addBefore("CountBasicBlocks", "methodName", routine.getMethodName());
                }
            }
	ci.write(argv[1]);
        }
    }
    
    public static synchronized void countMethod(int incr) {
        m_count++;
    }

    public static synchronized void countBasicBlock(int incr) {
        bb_count += incr;
        i_count++;
    }
    
    public static synchronized void methodName(String name) {
        currentMethodName = name;
	System.out.println(name);
    }

    public static synchronized void exitWrite(int opcode) {
        try {
            if(currentMethodName.equals("solve") && opcode==177) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
	
                BufferedWriter bw = new BufferedWriter(new FileWriter("timer_results/" + dateFormat.format(date) + ".txt"));
		bw.write("foobar");
	        bw.close();
	     }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Could not write to file with path \"~/timer_solve.txt\"...");
        }
      
    }

}


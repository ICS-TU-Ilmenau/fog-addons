import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.UnknownFormatConversionException;



public class Main
{
	private static final int STEPSIZE = 500;
	private static final int THREADS = 2;
	
	private static final String ENV_LOOP = "import.rs_probability";
	private static final String[] ENV_LOOP_VALUES = new String[] {
		"0.0", "0.0125", "0.025", "0.05",
		"0.1",
		"0.2",
		"0.3",
		"0.4",
		"0.5",
		"0.6",
		"0.7",
		"0.8",
		"0.9",
		"0.95",
		"1.0"
	};
	
	private static final String DEFAULT_CMD = "start RandomConnect %1$d true true";
	
	
	private static void error(Exception exc)
	{
		System.err.println("command [-verbose] <working directory> <runconfig> <from> <to> [<step size=500> [<command='" +DEFAULT_CMD +"'> [<additional parameter>]]]");
		System.err.println("example 1: 'c:\\workspace' myrun 1500 3000");
		System.err.println("example 2: 'c:\\workspace' myrun 1000 1300 100 'start MyCommand'");
		
		if(exc != null) {
			exc.printStackTrace();
		}
		
		System.exit(1);
	}
	
	public static void main(String[] args)
	{
		boolean verbose = false;
		
		if(args.length < 4) {
			error(null);
		}
		if("-verbose".equals(args[0])) {
			if(args.length < 5) {
				error(null);
			}
			
			verbose = true;
			
			// remove "-verbose" from argument list
			String[] newArgs = new String[args.length -1];
			for(int i=1; i<args.length; i++) {
				newArgs[i -1] = args[i];
			}
			args = newArgs;
		}
		
		String path = args[0];
		LinkedList<String> paths = new LinkedList<String>();
		
		System.out.println("Working directory = " +path);

		if(path.endsWith("/")) {
			File dir = new File(path.replace("/", ""));
			
			// This filter only returns directories
			File[] files = dir.listFiles(new FileFilter() {
				@Override
			    public boolean accept(File file) {
			        return file.isDirectory();
			    }
			});
			
			for(File subdir : files) {
				System.out.println("   Directory = " +subdir.getPath());
				paths.addLast(subdir.getPath());
			}
			
			System.out.println(paths.size() +" working directories found");
		} else {
			paths.add(path);
		}
		
		int start = STEPSIZE;
		int stop = start;
		int stepsize = STEPSIZE;
		
		String runConfig = null;
		String command = DEFAULT_CMD;
		
		try {
			runConfig = args[1];
			start = Integer.parseInt(args[2]);
			stop = Integer.parseInt(args[3]);
			
			if(args.length >= 5) {
				stepsize = Integer.parseInt(args[4]);
				
				if(args.length >= 6) {
					command = args[5];
				}
			}
			
			if(stepsize <= 0) {
				throw new RuntimeException("Step size is zero or negative; please define a positive one");
			}
		}
		catch(Exception exc) {
			error(exc);
		}
		System.out.println("Loop from " +start +" to " +stop +" with steps of " +stepsize);

		
		try {
			for(String dir : paths) {
				for(int e=0; e<ENV_LOOP_VALUES.length; e++) {
					for(int i=start; i<=stop; i+=stepsize) {
						LinkedList<String> params = new LinkedList<String>();
						params.addLast("-Drun=" +runConfig);
						params.addLast("-D" +ENV_LOOP +"=" +ENV_LOOP_VALUES[e]);
						params.addLast("-Doutputprefix=" +runConfig +"-" +ENV_LOOP_VALUES[e] +"-" +i +"_");
						params.addLast("-Dexecute=" +String.format(command, i, ENV_LOOP_VALUES[e]));
						
						if(args.length >= 7) {
							for(int p=6; p<args.length; p++) {
								params.addLast(args[p]);
							}
						}
						
						sJobList.addLast(new Job(dir, params));
					}
				}
			}
			
			System.out.println("Starting " +THREADS +" worker");
			Worker[] worker = new Worker[THREADS];
			for(int w=0; w<THREADS; w++) {
				worker[w] = new Worker(verbose);
				worker[w].start();
			}
		}
		catch(UnknownFormatConversionException exc) {
			System.err.println("Wrong command format of '" +command +"'");
			exc.printStackTrace(System.err);
		}
	}
	
	public static Job getNextJob()
	{
		synchronized (sJobList) {
			if(!sJobList.isEmpty()) {
				Job job = sJobList.removeFirst();
				
				// wait a bit in order to give others time to startup
				// in special that ensures different starting times and therefore
				// different time stamps used for file names
				try {
					Thread.sleep(1000 *10);
				}
				catch (InterruptedException exc) {
					// ignore
				}
				
				return job;
			} else {
				return null;
			}
		}
	}

	private static LinkedList<Job> sJobList = new LinkedList<Job>();
}

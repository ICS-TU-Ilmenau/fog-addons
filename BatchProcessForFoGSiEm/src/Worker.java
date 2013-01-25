import java.io.File;
import java.util.Random;


public class Worker extends Thread
{
	/**
	 * Enables test run, which do not execute the command but
	 * prints it to std out.
	 */
	private static final boolean DEBUG = false;
	
	private static final String OSGI_JAR_NAME = "org.eclipse.osgi_3.6.1.R36x_v20100806.jar"; 

	public Worker()
	{
		super();
		setDaemon(false);
		
		workerNumber = new Random().nextInt(1000);
	}
	
	public void run()
	{
		while(true) {
			Job job = Main.getNextJob();

			if(job != null) {
				try {
					System.out.println(workerNumber +": " +job +" - start");
					long duration = run(job, Integer.toString(workerNumber));
					
					System.out.println(workerNumber +": " +job + " - took " +Math.round(((double)duration / 1000.0d) / 60.0d) +" minutes");
				}
				catch(Exception exc) {
					System.err.println(workerNumber +": " +job +" - exception");
					exc.printStackTrace();
				}
			} else {
				// no more jobs; exit loop
				break;
			}
		}
		
		System.out.println(workerNumber +" exiting");
	}
	
	private static boolean isAlive(Process process)
	{
		try {
			process.exitValue();
			return true;
		}
		catch(IllegalThreadStateException exc) {
			return false;
		}
		
	}

	private static long run(Job job, String watchdog) throws Exception
	{
		job.param.addFirst("-Dwatchdog=" +watchdog);
		job.param.addFirst("-Xmx15G");
		job.param.addFirst("-Xms400m");
		job.param.addFirst("java");
		job.param.addFirst("/c");
		job.param.addFirst("cmd");
		
		job.param.addLast("-jar");
		job.param.addLast(".\\" +OSGI_JAR_NAME);
		job.param.addLast("-console");
		
		long startTime = System.currentTimeMillis();
		
		if(!DEBUG) {
			//
			// REAL RUN
			//
			ProcessBuilder builder = new ProcessBuilder(job.param);
			builder.directory(new File(job.folder));
			
			Process p = null;
			try {
				p = builder.start();
				p.waitFor();
			}
			finally {
				if(p != null) {
					try {
						System.out.println(watchdog +": " +job +" - exit value = " +p.exitValue());
					}
					catch(IllegalThreadStateException exc) {
						System.err.println(watchdog +": " +job +" - destroy process");
						p.destroy();
					}
				}
			}
		} else {
			//
			// DEBUG PRINT TO STD OUT
			//
			StringBuilder cmd = new StringBuilder();
			for(String p : job.param) {
				boolean space = p.contains(" ");
				if(space) cmd.append("\"");
				cmd.append(p);
				if(space) cmd.append("\"");
				cmd.append(" ");
			}
			
			System.out.println(cmd.toString());
		}
		
		return System.currentTimeMillis() -startTime;
	}
	
	private int workerNumber = -1;
}

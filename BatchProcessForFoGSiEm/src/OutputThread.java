import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


class OutputThread extends Thread
{
	public OutputThread(InputStream stream)
	{
		super();
		
		this.stream = stream;
	}
	
	@Override
	public void run() 
	{
		try {
			InputStreamReader stdout = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(stdout);
			String read = reader.readLine();
			
			while(read != null) {
				System.out.println(read);
				read = reader.readLine();
			}
		}
		catch(IOException exc) {
			System.err.println("error while reading stream");
			exc.printStackTrace(System.err);
		}
	}
	
	private InputStream stream;
}
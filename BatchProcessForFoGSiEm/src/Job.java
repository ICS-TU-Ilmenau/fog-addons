import java.util.LinkedList;


public class Job
{
	public Job(String pFolder, LinkedList<String> pParameters)
	{
		folder = pFolder;
		param = pParameters;
	}
	
	public String toString()
	{
		return folder +">" +param;
	}
	
	public String folder;
	public String cmd;
	public LinkedList<String> param;
}

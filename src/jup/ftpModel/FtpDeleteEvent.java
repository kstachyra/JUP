package jup.ftpModel;

public class FtpDeleteEvent extends FtpEvent
{
	private final String name;
	private final String path;
	
	public FtpDeleteEvent(String path, String name)
	{
		this.name = name;
		this.path = path;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}
}

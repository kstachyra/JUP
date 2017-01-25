package jup.ftpModel;

public class FtpDownloadEvent extends FtpEvent
{
	private final String name;
	private final String path;
	
	public FtpDownloadEvent(String path, String name)
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
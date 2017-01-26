package jup.ftpModel;

public class FtpDownloadEvent extends FtpEvent
{
	private final String name;
	private final String path;
	private final String dir;
	
	public FtpDownloadEvent(String path, String name, String dir)
	{
		this.name = name;
		this.path = path;
		this.dir=dir;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public String getDir()
	{
		return dir;
	}
}
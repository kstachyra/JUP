package jup.event;

public class DownloadFileEvent extends JupEvent
{
	private final String path;
	private final String name;
	private final String dir;

	public DownloadFileEvent(String path, String name, String dir)
	{
		super();
		this.path = path;
		this.name = name;
		this.dir = dir;
	}
}
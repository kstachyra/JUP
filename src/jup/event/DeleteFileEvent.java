package jup.event;

public class DeleteFileEvent extends JupEvent
{
	private final String path;
	private final String name;
	
	public DeleteFileEvent(String path, String name)
	{
		super();
		this.path = path;
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public String getName()
	{
		return name;
	}
}
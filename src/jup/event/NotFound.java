package jup.event;

public class NotFound extends JupEvent
{
	private final String path;
	private final String name;
	
	public NotFound(String path, String name)
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

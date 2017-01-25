package jup.event;

import jup.model.FileStatus;

/**
 * uaktualnia informacje o pliku
 */
public class UpdateEvent extends JupEvent
{
	final private String path;
	final private String name;
	final private FileStatus status;
	
	public UpdateEvent(String path, String name, FileStatus status)
	{
		this.path = path;
		this.name = name;
		this.status = status;
	}

	public String getPath()
	{
		return path;
	}

	public String getName()
	{
		return name;
	}

	public FileStatus getStatus()
	{
		return status;
	}
}


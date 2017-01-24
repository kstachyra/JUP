package jup.ftpModel;

import jup.event.JupEvent;
import jup.ftpModel.FtpEvent;

public class FtpUploadEvent extends FtpEvent
{
	private final String name;
	private final String path;
	
	public FtpUploadEvent(String name, String path)
	{
		this.name = name;
		this.path=path;
	}
}
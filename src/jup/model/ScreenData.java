package jup.model;

import java.util.List;

public class ScreenData
{
	public List<JupFile> tableData;
	public JupStatus status;
	
	public ScreenData(List<JupFile> list, JupStatus status)
	{
		tableData = list;
		this.status = status;
	}
	
	public String getStatusText()
	{
		switch (status)
		{
		case CONNECT: return "connecting to server";
		case WORK: return "ready";
		case CLOSE: return "closing";
			
		default: return "error status";
		}
	}
}
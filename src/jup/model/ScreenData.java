package jup.model;

import java.util.List;

public class ScreenData
{
	public List<JupFile> tableData;
	public JupStatus status;
	
	public ScreenData(List<JupFile> list)
	{
		tableData = list;
		status = JupStatus.LOADING;
	}
	
	public String getStatusText()
	{
		switch (status)
		{
		case JEDEN: return "pierwszy status";
		case DWA: return "drugi status";
		case LOADING: return "loading";
			
		default: return "error status";
		}
	}
}
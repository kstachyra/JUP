package jup.model;

import java.util.List;

public class ScreenData
{
	public List<JupFile> tableData;
	
	public ScreenData(List<JupFile> list)
	{
		tableData = list;
	}
	
	public String getStatusText()
	{
		return "2017 --- Kacper Stachyra";
	}
}
package jup.model;

import java.util.LinkedList;

public class Model
{
	private LinkedList <JupFile> fileList;
	
	
	int liczbaModelu = 42;
	
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData();
		Object[][] k = {{"a", "b", liczbaModelu}};
		sd.tableData = k;

		return sd;
	}
	
	public void incLiczba()
	{
		liczbaModelu++;
	}
	
	
	
	

	public int getLiczbaModelu()
	{
		return liczbaModelu;
	}

	public void setLiczbaModelu(int liczbaModelu)
	{
		this.liczbaModelu = liczbaModelu;
	}

	public void addFile(String path, String name)
	{
		System.out.println("Model.addFile: file path = " + path);
		
	}
}

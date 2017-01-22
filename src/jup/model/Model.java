package jup.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Model
{
	/** lista wybranych plik�w */
	private List <JupFile> fileList = new ArrayList<JupFile>();

	/**
	 * metoda zwracaj�ca dane do wy�wietlenia dla widoku
	 */
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData();
		Object[][] k = {{"a", "b", 0}};
		sd.tableData = k;

		return sd;
	}

	/**
	 * na podstawie �cie�ki + nazwy pliku, dodaje informacje o nim do listy fileList
	 */
	public void addFile(String path, String name)
	{
		System.out.println("Model.addFile: dodaj� nowy plik do listy: " + path + name);
		File file = new File(path, name);
		
		if (file.canRead())
		{
			
			fileList.add(new JupFile(path, name, file.length()));
		}
		else
		{
			System.out.println("Model.addFile: brak mo�liwo�ci odczytu pliku");
		}
	}
}

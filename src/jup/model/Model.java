package jup.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Model
{
	/** lista wybranych plików */
	private List <JupFile> fileList = new ArrayList<JupFile>();

	/**
	 * metoda zwracaj¹ca dane do wyœwietlenia dla widoku
	 */
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData();
		Object[][] k = {{"a", "b", 0}};
		sd.tableData = k;

		return sd;
	}

	/**
	 * na podstawie œcie¿ki + nazwy pliku, dodaje informacje o nim do listy fileList
	 */
	public void addFile(String path, String name)
	{
		System.out.println("Model.addFile: dodajê nowy plik do listy: " + path + name);
		File file = new File(path, name);
		
		if (file.canRead())
		{
			
			fileList.add(new JupFile(path, name, file.length()));
		}
		else
		{
			System.out.println("Model.addFile: brak mo¿liwoœci odczytu pliku");
		}
	}
}

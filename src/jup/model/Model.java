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
	 * na podstawie œcie¿ki + nazwy pliku, dodaje/aktualizuje informacje o nim do listy fileList
	 */
	public void addFile(String path, String name)
	{
		System.out.println("Model.addFile: dodajê nowy plik do listy: " + path + "\\" + name);
		File file = new File(path, name);
		
		if (file.canRead())
		{
			JupFile newFile = new JupFile(path, name, file.length());
			//gdy nie jest to dok³adnie ten sam plik
			if (!fileList.contains(newFile))
			{
				//i nie istnieje taki o podanej œcie¿ce
				if (findFile(path, name) == null)
				{
					fileList.add(newFile);
				}
				//jest taka œcie¿ka, ale plik siê zmieni³
				else
				{
					changeStatus(path, name, FileStatus.EDITED);
				}
			}
			else System.out.println("Model.addFile: plik istnieje, pomijam dodawanie");
		}
		else System.out.println("Model.addFile: brak mo¿liwoœci odczytu pliku");
	}

	/**
	 * wyœwietla listê plików w konsoli
	 */
	public void printFileList()
	{
		System.out.println("Model.printFileList: obecnie na liscie znajduj¹ siê pliki");
		for (JupFile el : fileList)
		{
		  System.out.println("\tstatus: " + el.getStatus() + "\tsum: " + el.getChecksum() + "\t" + el.getPath() +"\\" + el.getName());
		}
	}
	
	/**
	 * zwraca element listy plików o podanej œcie¿ce
	 */
	private JupFile findFile(String path, String name)
	{
		System.out.println("Model.findFile: szukam na liœcie plików pliku o podanej œcie¿ce" + path + "\\" + name);
		for (JupFile el : fileList)
		{
			if (el.getName().equals(name) && el.getPath().equals(path))
			{
				System.out.println("Model.findFile: znaleziono");
				return el;
			}
		}
		System.out.println("Model.findFile: nie znaleziono");
		return null;
	}
	
	/**
	 * zwraca element listy plików o podanej œcie¿ce
	 */
	private void changeStatus(String path, String name, FileStatus status)
	{
		System.out.println("Model.changeStatus: zmieniam status pliku na " + status);
		findFile(path, name).setStatus(status);
	}
}
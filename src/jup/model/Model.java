package jup.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import jup.ftpController.FtpController;
import jup.ftpController.FtpEvent;
import jup.ftpController.FtpLoadEvent;

public class Model
{
	/** lista wybranych plików */
	private List <JupFile> fileList = new ArrayList<JupFile>();
	
	/** status programu */
	private JupStatus status;
	
	/** kolejka zdarzeñ ftp */
	private BlockingQueue<FtpEvent> ftpQueue;
	

	/**
	 * tworzenie modelu, uruchomienie w¹tków ftp, ustawienie statusu programu
	 */
	public Model(FtpController ftp)
	{
		status = JupStatus.START;
	    ftp.start();
	    ftpQueue = ftp.ftpQueue;
	    
	    try
		{
			ftpQueue.put(new FtpLoadEvent());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//TODO run w¹tki, pobierz info o plikach, zmien status oswie¿ widok...
	    //a jak nie to error koniec kaput
	}

	/**
	 * metoda zwracaj¹ca dane do wyœwietlenia dla widoku
	 */
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData(fileList, status);
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
	 * synchronizuje informacje miêdzy serwerem a klientem
	 */
	public void update()
	{
		System.out.println("Model.update: porównujê lokaln¹ listê plików z informacjami pobranymi z serwera");
		for (JupFile el : fileList)
		{
			//TODO !!!
		}
	}
	
	/**
	 * koñczy pracê programu
	 */

	public void exit()
	{
		System.out.println("Model.exit: koñczê pracê");
		//TODO poczekaj na w¹tki...
	}

	/**
	 * przekazuje do kolejki ftp zdarzenie pobrania pliku
	 */
	public void downloadFile(String path, String name, String dir)
	{
		System.out.println("Model.downloadFile: wstawiam do kolejki FTP ¿¹danie pobrania " + name);
		try
		{
			ftpQueue.put(new FtpLoadEvent());
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	
	
	
	/**
	 * zwraca element listy plików pliku o podanej œcie¿ce
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
	 * zmienia status danego pliku
	 */
	private void changeStatus(String path, String name, FileStatus status)
	{
		System.out.println("Model.changeStatus: zmieniam status pliku na " + status);
		findFile(path, name).setStatus(status);
	}
	
}
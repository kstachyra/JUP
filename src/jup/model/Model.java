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
	/** lista wybranych plik�w */
	private List <JupFile> fileList = new ArrayList<JupFile>();
	
	/** status programu */
	private JupStatus status;
	
	/** kolejka zdarze� ftp */
	private BlockingQueue<FtpEvent> ftpQueue;
	

	/**
	 * tworzenie modelu, uruchomienie w�tk�w ftp, ustawienie statusu programu
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
		//TODO run w�tki, pobierz info o plikach, zmien status oswie� widok...
	    //a jak nie to error koniec kaput
	}

	/**
	 * metoda zwracaj�ca dane do wy�wietlenia dla widoku
	 */
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData(fileList, status);
		return sd;
	}

	/**
	 * na podstawie �cie�ki + nazwy pliku, dodaje/aktualizuje informacje o nim do listy fileList
	 */
	public void addFile(String path, String name)
	{
		System.out.println("Model.addFile: dodaj� nowy plik do listy: " + path + "\\" + name);
		File file = new File(path, name);
		
		if (file.canRead())
		{
			JupFile newFile = new JupFile(path, name, file.length());
			//gdy nie jest to dok�adnie ten sam plik
			if (!fileList.contains(newFile))
			{
				//i nie istnieje taki o podanej �cie�ce
				if (findFile(path, name) == null)
				{
					fileList.add(newFile);
				}
				//jest taka �cie�ka, ale plik si� zmieni�
				else
				{
					changeStatus(path, name, FileStatus.EDITED);
				}
			}
			else System.out.println("Model.addFile: plik istnieje, pomijam dodawanie");
		}
		else System.out.println("Model.addFile: brak mo�liwo�ci odczytu pliku");
	}

	/**
	 * synchronizuje informacje mi�dzy serwerem a klientem
	 */
	public void update()
	{
		System.out.println("Model.update: por�wnuj� lokaln� list� plik�w z informacjami pobranymi z serwera");
		for (JupFile el : fileList)
		{
			//TODO !!!
		}
	}
	
	/**
	 * ko�czy prac� programu
	 */

	public void exit()
	{
		System.out.println("Model.exit: ko�cz� prac�");
		//TODO poczekaj na w�tki...
	}

	/**
	 * przekazuje do kolejki ftp zdarzenie pobrania pliku
	 */
	public void downloadFile(String path, String name, String dir)
	{
		System.out.println("Model.downloadFile: wstawiam do kolejki FTP ��danie pobrania " + name);
		try
		{
			ftpQueue.put(new FtpLoadEvent());
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	
	
	
	/**
	 * zwraca element listy plik�w pliku o podanej �cie�ce
	 */
	private JupFile findFile(String path, String name)
	{
		System.out.println("Model.findFile: szukam na li�cie plik�w pliku o podanej �cie�ce" + path + "\\" + name);
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
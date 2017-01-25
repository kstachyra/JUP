package jup.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.JOptionPane;

import jup.ftpModel.*;

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
	public Model(FtpModel ftp)
	{
		status = JupStatus.CONNECT;
	    ftp.start();
	    ftpQueue = ftp.ftpQueue;
	    
	    try
		{
			ftpQueue.put(new FtpConnectEvent());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//TODO run w�tki, pobierz info o plikach, zmien status oswie� widok...
	    loadFileList();
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
		boolean toUpload = false;
		File file = new File(path, name);
		
		if (file.canRead())
		{
			JupFile newFile = new JupFile(path, name, file.length());
			//gdy nie jest to dok�adnie ten sam plik
			if (!fileList.contains(newFile))
			{
				JupFile ff = findFile(path, name);
				//nie istnieje taki o podanej �cie�ce
				if (ff == null)
				{
					fileList.add(newFile);
					toUpload = true;
				}
				//jest taka �cie�ka, ale plik si� zmieni� i ju� jest zuploadowany
				else if (ff.getStatus() == FileStatus.UPLOADED || ff.getStatus() == FileStatus.DOWNLOADED)
				{
					changeStatus(path, name, FileStatus.EDITED);
					toUpload = true;
				}
				//je�li w�a�nie jest u�ywany
				else if (ff.getStatus() == FileStatus.UPLOADING || ff.getStatus() == FileStatus.DOWNLOADING || ff.getStatus() == FileStatus.TO_DOWNLOAD)
				{
					JOptionPane.showMessageDialog(null, "ERROR file is being used\nOK to continue");
				}
				//w pozosta�ych przypadkach jest na li�cie ftp i mo�na pobra� nowy z dysku
			}
			else System.out.println("Model.addFile: plik istnieje, pomijam dodawanie");
		}
		else System.out.println("Model.addFile: brak mo�liwo�ci odczytu pliku");
		
		//je�li powiniene� go dodawa� do kolejki ftp
		if (toUpload)
		{
			//w obu przypadkach dodaj go do kolejki ftp
			try
			{
				System.out.println("Model.addFile: wysy�am informacje do FTP o upload pliku");
				ftpQueue.put(new FtpUploadEvent(path, name));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * uaktualnia status pliku zgodnie z informacjami od serwera
	 */
	public void updateFileStatus(String path, String name, FileStatus status)
	{
		for (JupFile el : fileList)
		{
			if (el.getPath() == path && el.getName() == name)
			{
				changeStatus(path, name, status);
			}
		}
	}
	
	/**
	 * ko�czy prac� programu
	 */
	public void exit()
	{
		System.out.println("Model.exit: ko�cz� prac�");
		saveFileList();
		//TODO poczekaj na w�tki...
	}

	/**
	 * przekazuje do kolejki ftp zdarzenie pobrania pliku
	 */
	public void downloadFile(String path, String name, String dir)
	{
		if (findFile(path, name).getStatus() == FileStatus.UPLOADED)
		{
			changeStatus(path, name, FileStatus.TO_DOWNLOAD);
			System.out.println("Model.downloadFile: wstawiam do kolejki FTP ��danie pobrania " + name);
			try
			{
				ftpQueue.put(new FtpDownloadEvent(path, name));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "ERROR file not uploaded\nOK to continue");
		}
	}

	/**
	 * zwraca element listy plik�w pliku o podanej �cie�ce
	 */
	private JupFile findFile(String path, String name)
	{
		for (JupFile el : fileList)
		{
			if (el.getName().equals(name) && el.getPath().equals(path))
			{
				return el;
			}
		}
		return null;
	}
	
	/**
	 * zmienia status danego pliku (�cie�ka + nazwa)
	 */
	private void changeStatus(String path, String name, FileStatus status)
	{
		System.out.println("Model.changeStatus: zmieniam status pliku na " + status);
		findFile(path, name).setStatus(status);
	}
	
	/**
	 * pobiera informacje o plikach po starcie programu z dysku
	 */
	private void loadFileList()
	{
		//TODO
	}
	/**
	 * zapisuje informacje o plikach przy zakonczeniu programu z dysku
	 */
	private void saveFileList()
	{
		//TODO
	}
	
}
package jup.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.JOptionPane;

import jup.ftpModel.*;

public class Model
{
	/** lista wybranych plików */
	private List <JupFile> fileList = new ArrayList<JupFile>();
	
	/** status programu */
	private JupStatus status;
	
	/** kolejka zdarzeñ ftp */
	private BlockingQueue<FtpEvent> ftpQueue;
	
	private FtpModel ftp;
	

	/**
	 * tworzenie modelu, uruchomienie w¹tków ftp, ustawienie statusu programu
	 */
	public Model(FtpModel ftp)
	{
		this.ftp = ftp;
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
	    loadFileList();
	    fillFtpQueue();
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
		boolean toUpload = false;
		File file = new File(path, name);
		
		if (file.canRead())
		{
			JupFile newFile = new JupFile(path, name, file.length());
			//gdy nie jest to dok³adnie ten sam plik
			if (!fileList.contains(newFile))
			{
				JupFile ff = findFile(path, name);
				//nie istnieje taki o podanej œcie¿ce
				if (ff == null)
				{
					fileList.add(newFile);
					toUpload = true;
				}
				//jest taka œcie¿ka, ale plik siê zmieni³ i ju¿ jest zuploadowany
				else if (ff.getStatus() == FileStatus.UPLOADED || ff.getStatus() == FileStatus.DOWNLOADED)
				{
					changeStatus(path, name, FileStatus.EDITED);
					ff.updateChecksum();
					toUpload = true;
				}
				//jeœli w³aœnie jest u¿ywany
				else if (ff.getStatus() == FileStatus.UPLOADING || ff.getStatus() == FileStatus.DOWNLOADING || ff.getStatus() == FileStatus.TO_DOWNLOAD)
				{
					JOptionPane.showMessageDialog(null, "ERROR file is being used\nOK to continue");
				}
				//w pozosta³ych przypadkach jest na liœcie ftp i mo¿na pobraæ nowy z dysku
			}
			else System.out.println("Model.addFile: plik istnieje, pomijam dodawanie");
		}
		else System.out.println("Model.addFile: brak mo¿liwoœci odczytu pliku");
		
		//jeœli powinieneœ go dodawaæ do kolejki ftp
		if (toUpload)
		{
			//w obu przypadkach dodaj go do kolejki ftp
			try
			{
				System.out.println("Model.addFile: wysy³am informacje do FTP o upload pliku");
				ftpQueue.put(new FtpUploadEvent(path, name));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * przekazuje do kolejki ftp zdarzenie pobrania pliku
	 */
	public void downloadFile(String path, String name, String dir)
	{
		if (findFile(path, name).getStatus() == FileStatus.UPLOADED || findFile(path, name).getStatus() == FileStatus.DOWNLOADED)
		{
			changeStatus(path, name, FileStatus.TO_DOWNLOAD);
			System.out.println("Model.downloadFile: wstawiam do kolejki FTP ¿¹danie pobrania " + name);
			try
			{
				ftpQueue.put(new FtpDownloadEvent(path, name, dir));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			if (findFile(path, name).getStatus() == FileStatus.TO_DOWNLOAD || findFile(path, name).getStatus() == FileStatus.DOWNLOADING) JOptionPane.showMessageDialog(null, "ERROR file is already downloading\nOK to continue");
			else JOptionPane.showMessageDialog(null, "ERROR file not uploaded\nOK to continue");
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
	 * dla wszystkich plików na liœcie sprawdza, czy zmieni³ siê
	 * jeœli tak zmienia jego status i przekazuje do kolejki ftp
	 */
	public void checkEditions()
	{
		System.out.println("Model.checkEditions: sprawdzam zmiany...");
		for (JupFile el : fileList)
		{
			JupFile newFile = new JupFile(el.getPath(), el.getName(), el.getSize());
			if (!newFile.getChecksum().equals(el.getChecksum()))
			{
				System.out.println("Model.checkEditions: znaleziono zmianê");
				el.setStatus(FileStatus.EDITED);
				el.updateChecksum();
				try
				{
					ftpQueue.put(new FtpUploadEvent(el.getPath(), el.getName()));
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		System.out.println("Model.checkEditions: OK!");
	}

	/**
	 * koñczy pracê programu
	 */
	public void exit()
	{
		System.out.println("Model.exit: koñczê pracê");
		saveFileList();
		try
		{
			ftpQueue.put(new FtpDisconnectEvent());
			ftp.t1.join();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	public JupStatus getJupStatus()
	{
		return status;
	}

	public void setJupStatus(JupStatus status)
	{
		this.status = status;
	}

	/**
	 * dla wszystkich plików z listy zleca upload dla potrzebuj¹cych
	 */
	private void fillFtpQueue()
	{
		for (JupFile el : fileList)
		{
			if (el.getStatus() == FileStatus.NEW || el.getStatus() == FileStatus.EDITED)
			{
				try
				{
					ftpQueue.put(new FtpUploadEvent(el.getPath(), el.getName()));
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * pobiera informacje o plikach po starcie programu z dysku
	 */
	private void loadFileList()
	{
		File fin = new File("JUPFileList.txt");
		
		try (BufferedReader br = new BufferedReader(new FileReader(fin)))
		{
		    String linePath, lineName, lineStatus, lineChecksum, lineSize;
		    while ((linePath = br.readLine()) != null)
		    {
		    	lineName = br.readLine();
		    	lineStatus = br.readLine();
		    	lineChecksum = br.readLine();
		    	lineSize = br.readLine();
		    	
		    	fileList.add(new JupFile(linePath, lineName, FileStatus.valueOf(lineStatus), Long.parseLong(lineChecksum), Long.parseLong(lineSize)));
		    }
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	
	/**
	 * zapisuje informacje o plikach przy zakonczeniu programu na dysku
	 */
	private void saveFileList()
	{
		try
		{
		    PrintWriter writer = new PrintWriter("JUPFileList.txt");
			for (JupFile el : fileList)
			{
				if (el.getStatus() == FileStatus.UPLOADING)
				{
					el.setStatus(FileStatus.NEW);
				}
				else if (el.getStatus() != FileStatus.EDITED && el.getStatus() != FileStatus.NEW)
				{
					el.setStatus(FileStatus.UPLOADED);
				}
				writer.println(el.getPath());
				writer.println(el.getName());
				writer.println(el.getStatus());
				writer.println(el.getChecksum());
				writer.println(el.getSize());
			}
		    
		    writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * zwraca element listy plików pliku o podanej œcie¿ce
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
	 * zmienia status danego pliku (œcie¿ka + nazwa)
	 */
	private void changeStatus(String path, String name, FileStatus status)
	{
		System.out.println("Model.changeStatus: zmieniam status pliku na " + status);
		findFile(path, name).setStatus(status);
	}

	public void connectionError()
	{
		JOptionPane.showMessageDialog(null, "unable to connect to server\nOK to quit");
		exit();
	}
}
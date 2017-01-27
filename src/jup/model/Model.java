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

import jup.event.JupEvent;
import jup.ftpModel.*;

public class Model
{
	/** lista wybranych plik�w */
	private List <JupFile> fileList = new ArrayList<JupFile>();
		
	/** kolejka zdarze� ftp */
	private BlockingQueue<FtpEvent> ftpQueue;
	
	/** modu� obs�ugi �cieciowej */
	private FtpModel ftp;
	
	/** scheduler (powo�ywany oddzielny w�tek) */
	private Scheduler scheduler;
	
	

	/**
	 * tworzenie modelu, uruchomienie w�tk�w ftp, czytanie pliku konfiguracyjnego, tworzenie scheduler�w
	 */
	public Model(FtpModel ftp, final BlockingQueue<JupEvent> controllerQueue)
	{
		this.ftp = ftp;
		ftpQueue = ftp.ftpQueue;
				
		scheduler = new Scheduler(controllerQueue);
		
		//start w�tk�w
	    ftp.start();
	    scheduler.start();
	    //ustawienie priorytet�w
	    ftp.t1.setPriority(Thread.MAX_PRIORITY/3 + Thread.MIN_PRIORITY);
	    scheduler.t1.setPriority(Thread.MAX_PRIORITY/5 + Thread.MIN_PRIORITY);
	    
	    // START czytanie danych konfiguracyjnych
		File fin = new File("JUPConfig.txt");
		
		//domy�lne warto�ci, gdy plik pusty TODO
		String server = null;
		String login = null;
		String pass = null;
		int port = 0;
		int time = 5000;
		
		try (BufferedReader br = new BufferedReader(new FileReader(fin)))
		{
			String line;
		    for (int i = 0; i<5; ++i)
		    {
		    	line = br.readLine();
		    	String[] field = line.split(" ");
		    	System.out.println("Model.Model: " + field[0] + " ustawiam -> " + field[1]);
		    	
		    	switch (i)
		    	{
		    	case 0: time = Integer.parseInt(field[1]); break;
		    	case 1: server = field[1]; break;
		    	case 2: login = field[1]; break;
		    	case 3: pass = field[1]; break;
		    	case 4: port = Integer.parseInt(field[1]);
		    	}
		    }
		} catch (IOException e)
		{
			e.printStackTrace();
		} // END
		
		scheduler.setTime(time);
	    
	    try
		{
			ftpQueue.put(new FtpConnectEvent(server, login, pass, port));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	    loadFileList();
	    fillFtpQueue();
	}

	/**
	 * metoda zwracaj�ca dane do wy�wietlenia dla widoku
	 */
	public ScreenData getScreenData()
	{
		ScreenData sd = new ScreenData(fileList);
		return sd;
	}

	/**
	 * na podstawie �cie�ki + nazwy pliku, dodaje/aktualizuje informacje o nim do listy fileList
	 */
	public void addFile(String path, String name)
	{
		boolean toUpload = false;
		File file = new File(path, name);
		
		if (file.canRead() && file.length()>0)
		{
			JupFile newFile = new JupFile(path, name);
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
					ff.updateChecksum();
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
	 * przekazuje do kolejki ftp zdarzenie pobrania pliku
	 */
	public void downloadFile(String path, String name, String dir)
	{
		if (findFile(path, name).getStatus() == FileStatus.UPLOADED || findFile(path, name).getStatus() == FileStatus.DOWNLOADED || findFile(path, name).getStatus() == FileStatus.ONLY_ONLINE)
		{
			changeStatus(path, name, FileStatus.TO_DOWNLOAD);
			System.out.println("Model.downloadFile: wstawiam do kolejki FTP ��danie pobrania " + name);
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
	 * usuwa plik z listy
	 */
	public void deleteFile(String path, String name)
	{
		JupFile ff = findFile(path, name);
		fileList.remove(ff);
		
		try
		{
			ftpQueue.put(new FtpDeleteEvent(path, name));
		} catch (InterruptedException e)
		{
			e.printStackTrace();
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
	 * dla wszystkich plik�w na li�cie sprawdza, czy zmieni� si�
	 * je�li tak zmienia jego status i przekazuje do kolejki ftp
	 */
	public void checkEditions()
	{
		System.out.println("Model.checkEditions: sprawdzam zmiany...");
		for (JupFile el : fileList)
		{
			JupFile newFile = new JupFile(el.getPath(), el.getName());
			
			if (newFile.getSize() == 0)
			{
				System.out.println("Model.checkEditions: plik nie istnieje");
				el.setStatus(FileStatus.ONLY_ONLINE);
			}
			else if (!newFile.getChecksum().equals(el.getChecksum()))
			{
				System.out.println("Model.checkEditions: znaleziono zmian�");
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
	 * wy�wietla komunikat i wy��cza program
	 */
	public void connectionError()
	{
		JOptionPane.showMessageDialog(null, "server connection failed\nOK to quit");
		exit();
	}

	/**
	 * je�li nie znaleziono pliku na serwerze, zmienia status i pr�buje go wys�a� ponownie
	 */
	public void fileNotFound(String path, String name)
	{
		if (findFile(path, name).getStatus() != FileStatus.ONLY_ONLINE)
		{
			changeStatus(path,  name, FileStatus.NOT_FOUND_ONLINE);
			try
			{
				JOptionPane.showMessageDialog(null, "ERROR file not found. Try to upload again\nOK to continue");
				ftpQueue.put(new FtpUploadEvent(path, name));
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * ko�czy prac� programu
	 */
	public void exit()
	{
		System.out.println("Model.exit: ko�cz� prac�");
		
		//zapisuje dane te co s�, gdyby u�ytkownik wymusi� zamkni�cie
		saveFileList();
		try
		{
			ftpQueue.put(new FtpDisconnectEvent());
			scheduler.t1.interrupt();
			ftp.t1.join();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//zapisuje dane po sko�czeniu w�tku serwera
		saveFileList();

		System.exit(0);
	}
	
	/**
	 * dla wszystkich plik�w z listy zleca upload dla NEW i EDITED
	 */
	private void fillFtpQueue()
	{
		for (JupFile el : fileList)
		{
			File file = new File(el.getPath(), el.getName());
			if (file.length()>0)
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
			else if(el.getStatus() != FileStatus.FTP_FAIL)
			{
				el.setStatus(FileStatus.ONLY_ONLINE);
				file.delete();
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
				if (el.getStatus() == FileStatus.FTP_FAIL)
				{
					System.out.println("Model.saveFileList: b��dne wpisy, pomijam");
				}
				else if (el.getStatus() == FileStatus.UPLOADING || el.getStatus()==FileStatus.ONLY_ONLINE)
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
}
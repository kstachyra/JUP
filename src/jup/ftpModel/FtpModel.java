package jup.ftpModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jup.event.ConnectedEvent;
import jup.event.ConnectionErrorEvent;
import jup.event.JupEvent;
import jup.event.NotFound;
import jup.event.UpdateEvent;
import jup.model.FileStatus;

/**
 * system po��czenia ftp, przyjmuje zlecenia z kolejki i je wykonuje
 */
public class FtpModel implements Runnable
{
	/** w�tek obs�ugi sieciowej */
	public Thread t1;
	/** kolejka zdarze� obs�ugiwanych przezftpModel */
	public final BlockingQueue<FtpEvent> ftpQueue  = new LinkedBlockingQueue<FtpEvent>();
	/** kolejka zdarze� przesy�anych do kontrolera */
	public final BlockingQueue<JupEvent> controllerQueue;
	
	/**mapa t�umacz�ca FtpEvent -> EventStrategy
	 * co robimy (jak� strategi�) w zale�no�ci od tego, jakie zdarzenie otrzymamy */
	private final HashMap<Class<? extends FtpEvent>, EventStrategy> eventStrategyMap;
	
	/** flaga informuj�ca o dzia�aniu programu */
	private boolean running = true;
	
	/** modu� klientFtp */
	private JupFtpClient ftpClient;
	
	public FtpModel (final BlockingQueue<JupEvent> controllerQueue)
	{
		this.controllerQueue = controllerQueue;
		eventStrategyMap = new HashMap<Class<? extends FtpEvent>, EventStrategy>();
		fillEventStrategyMap();
	}

	/**
	 * abstrakcyjna klasa strategii dla eventu
	 */
	private abstract class EventStrategy
	{
		abstract public void runStrategy(final FtpEvent event) throws InterruptedException;
	}
	
	/**
	 * strategia dla ��czenia z serwerem ftp
	 */
	private class ConnectStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.ConnectStrategy...");
			
			try
			{
				ftpClient = new JupFtpClient("jup", "jup");
				controllerQueue.put(new ConnectedEvent());
			} catch (Exception e)
			{
				controllerQueue.put(new ConnectionErrorEvent());
			}
		}
	}
	
	/**
	 * strategia dla wysy�ania pliku
	 */
	private class UploadStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.UploadStrategy...");
			FtpUploadEvent e = (FtpUploadEvent) event;
			controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.UPLOADING));

			try
			{
				if (ftpClient.upload(e.getPath(), e.getName()))
				{
					controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.UPLOADED));
				}
				else
				{
					controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.INACCESSIBLE));
				}
			} catch (IOException e1)
			{
				e1.printStackTrace();
				failFtp(e.getPath(), e.getName());
			}
		}
	}
	
	/**
	 * strategia dla pobierania pliku
	 */
	private class DownloadStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.DownloadStrategy...");
			FtpDownloadEvent e = (FtpDownloadEvent) event;
			controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.DOWNLOADING));
			
			try
			{
				if (ftpClient.download(e.getPath(), e.getName(), e.getDir()))
				{
					controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.DOWNLOADED));
				}
				else
				{
					controllerQueue.put(new NotFound(e.getPath(), e.getName()));
				}
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
				failFtp(e.getPath(), e.getName());
			}
		}
	}
	
	/**
	 * strategia dla usuwania pliku
	 */
	private class DeleteStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.DeleteStrategy...");
			FtpDeleteEvent e = (FtpDeleteEvent) event;
			
			try
			{
				ftpClient.delete(e.getPath(), e.getName());
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * strategia roz��czania 
	 */
	private class DisconnectStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.DisconnectStrategy...");

			ftpClient.stop();

			running = false;
		}
	}
	
	/**
	 * zape�nianie mapy strategii
	 */
	private void fillEventStrategyMap()
	{
		eventStrategyMap.put(FtpUploadEvent.class, new UploadStrategy());
		eventStrategyMap.put(FtpDownloadEvent.class, new DownloadStrategy());
		eventStrategyMap.put(FtpConnectEvent.class, new ConnectStrategy());
		eventStrategyMap.put(FtpDeleteEvent.class, new DeleteStrategy());

		eventStrategyMap.put(FtpDisconnectEvent.class, new DisconnectStrategy());
	}
	
	/**
	 * uruchamianie w�tku obs�uguj�cego zdarzenia w p�tli
	 */
	@Override
	public void run()
	{	
		while(running)
		{
			try
			{
				FtpEvent event = ftpQueue.take();
				EventStrategy eventStrategy = eventStrategyMap.get(event.getClass());
				eventStrategy.runStrategy(event);
				
				/* TODO nie dzia�a co�
				//je�li w kolejcie jest zdarzenie roz��czenia
				for (Object value : eventStrategyMap.values())
				{
					if (value instanceof DisconnectStrategy)
					{
						//to wyczy�� reszt�, wyjd� z p�tli iteruj�cej i roz��cz
						eventStrategyMap.clear();
						DisconnectStrategy s = new DisconnectStrategy();
						s.runStrategy(null);
						break;
					}
				}*/
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	System.out.println("FTP: ko�cz� prac� w�tku");
	}
	
	public void start()
	{
		System.out.println("FTP: uruchamiam w�tek");
		if (t1 == null)
		{
	        t1 = new Thread (this);
	        t1.start ();
		}
	}
	
	private void failFtp(String path, String name) throws InterruptedException
	{
		controllerQueue.put(new UpdateEvent(path, name, FileStatus.FTP_FAIL));
		controllerQueue.put(new ConnectionErrorEvent());
		running = false;
	}
}
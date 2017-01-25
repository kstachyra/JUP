package jup.ftpModel;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jup.event.JupEvent;
import jup.event.UpdateEvent;
import jup.model.FileStatus;

/**
 * system po³¹czenia ftp, przyjmuje zlecenia z kolejki i je wykonuje
 */
public class FtpModel implements Runnable
{
	/** w¹tek obs³ugi sieciowej */
	public Thread t1;
	/** kolejka zdarzeñ obs³ugiwanych przezftpModel */
	public final BlockingQueue<FtpEvent> ftpQueue  = new LinkedBlockingQueue<FtpEvent>();
	/** kolejka zdarzeñ przesy³anych do kontrolera */
	public final BlockingQueue<JupEvent> controllerQueue;
	
	/**mapa t³umacz¹ca FtpEvent -> EventStrategy
	 * co robimy (jak¹ strategiê) w zale¿noœci od tego, jakie zdarzenie otrzymamy */
	private final HashMap<Class<? extends FtpEvent>, EventStrategy> eventStrategyMap;
	
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
	 * strategia dla ³¹czenia z serwerem ftp
	 */
	private class ConnectStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.ConnectStrategy...");
			Thread.sleep(10000);
		}
	}
	
	/**
	 * strategia dla wysy³ania pliku
	 */
	private class UploadStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.UploadStrategy...");
			FtpUploadEvent e = (FtpUploadEvent) event;
			controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.UPLOADING));

			Thread.sleep(10000);
			
			controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.UPLOADED));
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
			
			Thread.sleep(10000);
			
			controllerQueue.put(new UpdateEvent(e.getPath(), e.getName(), FileStatus.DOWNLOADED));

		}
	}
	
	/**
	 * strategia roz³¹czania 
	 */
	private class DisconnectStrategy extends EventStrategy
	{
		@Override
		public void runStrategy(FtpEvent event) throws InterruptedException
		{
			System.out.println("FTP.DisconnectStrategy...");

			Thread.sleep(5000);

		}
	}
	
	/**
	 * zape³nianie mapy strategii
	 */
	private void fillEventStrategyMap()
	{
		eventStrategyMap.put(FtpUploadEvent.class, new UploadStrategy());
		eventStrategyMap.put(FtpDownloadEvent.class, new DownloadStrategy());
		eventStrategyMap.put(FtpConnectEvent.class, new ConnectStrategy());
		eventStrategyMap.put(FtpDisconnectEvent.class, new DisconnectStrategy());
	}
	
	/**
	 * uruchamianie w¹tku obs³uguj¹cego zdarzenia w pêtli
	 */
	@Override
	public void run()
	{	
		while(true)
		{
			try
			{
				FtpEvent event = ftpQueue.take();
				EventStrategy eventStrategy = eventStrategyMap.get(event.getClass());
				eventStrategy.runStrategy(event);
				
				//TODO odœwie¿ widok???
				//controllerQueue.put(new UpdateEvent());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	//TODO koñczenie            System.out.println("FTP: koñczê pracê w¹tku");
	}
	
	public void start()
	{
		System.out.println("FTP: uruchamiam w¹tek");
		if (t1 == null)
		{
	        t1 = new Thread (this);
	        t1.start ();
		}
	}
}
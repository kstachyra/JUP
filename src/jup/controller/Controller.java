package jup.controller;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import jup.model.Model;
import jup.view.View;
import jup.event.*;
import jup.ftpModel.FtpModel;

public class Controller
{
	private final View view;
	private final Model model;
	private final BlockingQueue<JupEvent> bq;
	
	private final FtpModel ftp;
	
	/**mapa t�umacz�ca JupEvent -> EventStrategy
	 * co robimy (jak� strategi�) w zale�no�ci od tego, jakie zdarzenie otrzymamy */
	private final HashMap<Class<? extends JupEvent>, EventStrategy> eventStrategyMap;
	
	/**
	 * konstruktor kontrolera, tworzy i zape�nia map� strategii
	 */
	public Controller(final View view, final Model model, final BlockingQueue<JupEvent> blockingQueue, final FtpModel ftp)
	{
		this.view = view;
		this.model = model;
		this.bq = blockingQueue;
		
		this.ftp = ftp;
		
		eventStrategyMap = new HashMap<Class<? extends JupEvent>, EventStrategy>();
		fillEventStrategyMap();
	}
	
	/**
	 * sterowanie kontrolera
	 * pobiera zdarzenie z kolejki, sprawdza w mapie co robi�, uruchamia odpowiedni� strategi�, od�wie�a widok
	 */
	public void work()
	{	
		while(true)
		{
			try
			{
				JupEvent event = bq.take();
				EventStrategy eventStrategy = eventStrategyMap.get(event.getClass());
				eventStrategy.runStrategy(event);
				view.refresh(model.getScreenData());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * abstrakcyjna klasa strategii dla eventu
	 */
	private abstract class EventStrategy
	{
		abstract public void runStrategy(final JupEvent event);
	}
	
	/**
	 * dodawanie pliku do listy
	 */
	private final class AddFileStrategy extends EventStrategy
	{
		public void runStrategy(final JupEvent event)
		{
			AddFileEvent addFileEvent = (AddFileEvent) event;
			System.out.println("Controller.AddFileStrategy...");
			
			
			model.addFile(addFileEvent.getPath(), addFileEvent.getName());
		}
	}
	
	/**
	 * wy��czanie programu, sprz�tanie
	 */
	private final class ExitStrategy extends EventStrategy
	{
		public void runStrategy(final JupEvent event)
		{
			System.out.println("Controller.ExitStrategy...");
			model.exit();
		}
	}
	
	/**
	 * wy�wietlenie listy plik�w w konsoli
	 */
	private final class UpdateStrategy extends EventStrategy
	{
		public void runStrategy(final JupEvent event)
		{
			System.out.println("Controller.UpdateStrategy...");
			model.update();
		}
	}
	
	/**
	 * pobieranie pliku z serwera
	 */
	private final class DownloadFileStrategy extends EventStrategy
	{
		public void runStrategy(final JupEvent event)
		{
			DownloadFileEvent downloadFileEvent = (DownloadFileEvent) event;
			System.out.println("Controller.DownloadFileStrategy...");
			model.downloadFile(downloadFileEvent.getPath(), downloadFileEvent.getName(), downloadFileEvent.getDir());
		}
	}
	
	/**
	 * zape�nianie map� strategii
	 */
	private void fillEventStrategyMap()
	{
		eventStrategyMap.put(AddFileEvent.class, new AddFileStrategy());
		eventStrategyMap.put(UpdateEvent.class, new UpdateStrategy());
		eventStrategyMap.put(DownloadFileEvent.class, new DownloadFileStrategy());
		eventStrategyMap.put(ExitEvent.class, new ExitStrategy());
	}
}
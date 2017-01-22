package jup.controller;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import jup.model.Model;
import jup.view.View;
import jup.event.*;

public class Controller
{
	private final View view;
	private final Model model;
	private final BlockingQueue<JupEvent> bq;
	
	/**mapa t�umacz�ca JupEvent -> EventStrategy
	 * co robimy (jak� strategi�) w zale�no�ci od tego, jakie zdarzenie otrzymamy */
	private final HashMap<Class<? extends JupEvent>, EventStrategy> eventStrategyMap;
	
	/**
	 * konstruktor kontrolera, tworzy i zape�nia map� strategii
	 */
	public Controller(final View view, final Model model, final BlockingQueue<JupEvent> blockingQueue)
	{
		this.view = view;
		this.model = model;
		this.bq = blockingQueue;
		
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
	 * zape�niamy map� strategii
	 */
	private void fillEventStrategyMap()
	{
		eventStrategyMap.put(AddFileEvent.class, new AddFileStrategy());
	}
}
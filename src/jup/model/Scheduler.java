package jup.model;

import java.util.concurrent.BlockingQueue;

import jup.event.EditionsEvent;
import jup.event.JupEvent;

public class Scheduler implements Runnable
{	
	/** w�tek schedulera */
	Thread t1;
	
	/** czas oczekiwania w milisekundach */
	private long time;
	
	/** kolejka cotrnollera, by wysy�a� zdarzenia do modelu */
	private final BlockingQueue<JupEvent> controllerQueue;
	
	Scheduler(final BlockingQueue<JupEvent> controllerQueue)
	{		
		//czas domy�lny
		this.time = 5000;
		
		this.controllerQueue = controllerQueue;
	}

	@Override
	public void run()
	{	
		while(true)
		{
			try
			{
				Thread.sleep(time);
				System.out.println("SCHED: sprawdzam zmiany plik�w po zadanym czasie t = " + time);
				controllerQueue.put(new EditionsEvent());
				
			} catch (InterruptedException e)
			{
				System.out.println("SCHED: ko�cz� dzia�anie");
			}
		}
	}
	
	public void start()
	{
		System.out.println("SCHED: uruchamiam w�tek");
		if (t1 == null)
		{
	        t1 = new Thread (this);
	        t1.start ();
		}
	}
	
	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time*1000;
	}
}

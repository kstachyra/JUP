package jup.ftpController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import jup.event.JupEvent;
import jup.event.UpdateEvent;
import jup.model.ScreenData;

/**
 * kontroler po³¹czenia ftp, przyjmuje zlecenia z kolejki i je wykonuje
 */
public class FtpController implements Runnable
{
	public Thread t1;
	/** kolejka zdarzeñ obs³ugiwanych przezftpController */
	public final BlockingQueue<FtpEvent> ftpQueue  = new LinkedBlockingQueue<FtpEvent>();
	/** kolejka zdarzeñ przesy³anych do kontrolera */
	public static BlockingQueue<JupEvent> controllerQueue;
	
	public FtpController (final BlockingQueue<JupEvent> controllerQueue)
	{
		this.controllerQueue = controllerQueue;
	}
		
	@Override
	public void run()
	{
		/*for(int i = 4; i > 0; i--)
		{
            System.out.println("Thread: " + ", " + i);
            // Let the thread sleep for a while.
            try
			{
				Thread.sleep(5000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }*/
		
		while(true)
		{
			try
			{
				FtpEvent event = ftpQueue.take();
				//EventStrategy eventStrategy = eventStrategyMap.get(event.getClass());
				//eventStrategy.runStrategy(event);
				//view.refresh(model.getScreenData());
				
				System.out.println("FTP: obs³uguje ftpevent");
				Thread.sleep(5000);
				controllerQueue.put(new UpdateEvent());
				System.out.println("FTP: obs³u¿y³em ftpevent");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
	//System.out.println("FTP: koñczê pracê w¹tku");
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
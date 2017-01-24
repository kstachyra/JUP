package jup.ftpController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * kontroler po³¹czenia ftp, przyjmuje zlecenia z kolejki i je wykonuje
 */
public class FtpController implements Runnable
{
	public Thread t1;
	public final BlockingQueue<FtpEvent> blockingQueue  = new LinkedBlockingQueue<FtpEvent>();
	
	
	@Override
	public void run()
	{
		for(int i = 4; i > 0; i--)
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
        }
		/*while(true)
		{
			try
			{
				FtpEvent event = blockingQueue.take();
				//EventStrategy eventStrategy = eventStrategyMap.get(event.getClass());
				//eventStrategy.runStrategy(event);
				//view.refresh(model.getScreenData());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}*/
		
	System.out.println("FTP: koñczê pracê w¹tku");
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
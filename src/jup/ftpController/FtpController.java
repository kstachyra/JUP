package jup.ftpController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * kontroler po³¹czenia ftp, przyjmuje zlecenia z kolejki i je wykonuje
 */
public class FtpController implements Runnable
{
	public final BlockingQueue<FtpEvent> blockingQueue  = new LinkedBlockingQueue<FtpEvent>();
	int ftp;
	
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
	}
}
package jup;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jup.view.View;
import jup.controller.Controller;
import jup.event.JupEvent;
import jup.ftpModel.FtpModel;
import jup.model.Model;

public class Jup
{
	public static void main(String[] args)
	{
		try
		{
			final BlockingQueue<JupEvent> blockingQueue  = new LinkedBlockingQueue<JupEvent>();
			final FtpModel ftp = new FtpModel(blockingQueue);
			final Model model = new Model(ftp, blockingQueue);
			final View view = new View(blockingQueue, model.getScreenData());	
			final Controller controller = new Controller(view, model, blockingQueue);
			
			controller.work();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
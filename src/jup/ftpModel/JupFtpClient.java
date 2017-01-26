package jup.ftpModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class JupFtpClient
{
	private final String user;
	private final String pass;
	private final int port;
	private final String server;
	private final FTPClient ftpClient;


	public JupFtpClient(String user, String pass)
	{
		this.server = "127.0.0.1";
		this.port = 21;
		this.user = user;
		this.pass = pass;
		this.ftpClient = new FTPClient();

		try
		{
			connect();

		} catch (IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();

			try
			{
				disconnect();
			} catch (IOException ex2)
			{
				ex.printStackTrace();
			}
		}
	}
	
	void stop()
	{
		try
		{
			disconnect();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void disconnect() throws IOException
	{
		if (ftpClient.isConnected())
		{
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}

	private void connect() throws SocketException, IOException
	{
		ftpClient.connect(server, port);
		ftpClient.login(user, pass);
		ftpClient.enterLocalPassiveMode();
	}

	void upload1(String path, String name) throws IOException
	{
		{
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			File firstLocalFile = new File(path + "/" + name);

			String firstRemoteFile = "c.txt";
			InputStream inputStream = new FileInputStream(firstLocalFile);

			System.out.println("Start uploading first file");
			boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
			inputStream.close();

			if (done)
			{
				System.out.println("The first file is uploaded successfully.");
			}
		}
	}

	private void upload2() throws IOException
	{
		File firstLocalFile = new File("D:/wyslackacprowi.txt");

		InputStream inputStream = new FileInputStream(firstLocalFile);


		File secondLocalFile = new File("D:/wyslackacprowi.txt");
		String secondRemoteFile = "b.txt";
		inputStream = new FileInputStream(secondLocalFile);

		System.out.println("Start uploading second file");
		OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
		byte[] bytesIn = new byte[4096];
		int read = 0;

		while ((read = inputStream.read(bytesIn)) != -1)
		{
			outputStream.write(bytesIn, 0, read);
		}
		
		inputStream.close();
		outputStream.close();

		boolean completed = ftpClient.completePendingCommand();
		if (completed)
		{
			System.out.println("The second file is uploaded successfully.");
		}
	}

	private void download1() throws IOException
	{
		// APPROACH #1: using retrieveFile(String, OutputStream)
		String remoteFile1 = "/b.txt";
		File downloadFile1 = new File("D:/f.txt");
		OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
		boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
		outputStream1.close();

		if (success)
		{
			System.out.println("File #1 has been downloaded successfully.");
		}
	}

	private void download2() throws IOException
	{
		String remoteFile1 = "/b.txt";
		File downloadFile1 = new File("D:/f.txt");
		OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
		boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);

		String remoteFile2 = "c.txt";
		File downloadFile2 = new File("D:/e.txt");
		OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
		InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
		byte[] bytesArray = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(bytesArray)) != -1)
		{
			outputStream2.write(bytesArray, 0, bytesRead);
		}

		success = ftpClient.completePendingCommand();
		if (success)
		{
			System.out.println("File #2 has been downloaded successfully.");
		}
		outputStream2.close();
		inputStream.close();
	}


	public void manonono()
	{
		try
		{
			//upload1();
			upload2();

			download1();
			download2();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			try
			{
				disconnect();
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}

package jup.ftpModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

	void upload(String path, String name) throws IOException
	{
		{
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	
			File firstLocalFile = new File(path + "/" + name);
	
			String firstRemoteFile = name;
			InputStream inputStream = new FileInputStream(firstLocalFile);
	
			System.out.println("Start uploading first file");
			boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
			inputStream.close();
	
			if (done)
			{
				System.out.println("JupFtpClient.upload: file upload successful");
			}
			else
			{
				System.out.println("JupFtpClient.upload: file upload ERROR");
			}
		}
	}

	/**
	 * funkcja pobieraj¹ca plik z serwera ftp do okreœlonego katalogu
	 */
	boolean download(String path, String name, String dir) throws IOException
	{
		//jeœli separator w œcie¿kach jest typu windowsowego, zamieniam w œcie¿ce
		if (File.separatorChar == '\\')
		{
			dir = dir.replace("\\", "/");
			path = path.replace("\\", "/");
		}
				
		//plik pobierany z serwera
		String remoteFile1 = "/" + name;
	
		//plik do zapisu lokalnie
		File downloadFile1 = new File(dir + "/" + name);
		
		
		
		OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
		boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
		outputStream1.close();
	
		if (success)
		{
			System.out.println("JupFtpClient.download: file download successful");
		}
		else
		{
			System.out.println("JupFtpClient.download: file download ERROR");
		}
		return success;
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
}

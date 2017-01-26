package jup.ftpModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.Stack;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class JupFtpClient
{
	private final String user;
	private final String pass;
	private final int port;
	private final String server;
	private final FTPClient ftpClient;

	
	/**
	 * rozpoczyna pracê klienta ftp, ³¹czy siê z serwerem zgodnie z podanymi danymi
	 * @throws ConnectException 
	 */
	@SuppressWarnings("finally")
	public JupFtpClient(String user, String pass) throws ConnectException
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
			System.out.println("BBBBBBBBBBBBBBBBBBBB");
			try
			{
				disconnect();
			} catch (IOException ex2)
			{
				ex.printStackTrace();
			} finally
			{
				throw new ConnectException();
			}
		}
	}
	
	/**
	 * koñczy pracê klienta ftp
	 */
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

	/**
	 * wysy³a na serwer ftp plik z zachowaniem hierarchi katalogów
	 */
	boolean upload(String path, String name) throws IOException
	{
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		//plik z dysku do uploadu
		File firstLocalFile = new File(path + "/" + name);
		
		makeDirTree(path);

		//plik tworzony (aktualizowany) po stronie serwera
		String firstRemoteFile = name;

		InputStream inputStream = new FileInputStream(firstLocalFile);

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
		return done;
	}

	/**
	 * funkcja pobieraj¹ca plik z serwera ftp do okreœlonego katalogu
	 */
	boolean download(String path, String name, String dir) throws IOException
	{
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		
		//jeœli separator w œcie¿kach jest typu windowsowego, zamieniam w œcie¿ce
		if (File.separatorChar == '\\')
		{
			dir = dir.replace("\\", "/");
			path = path.replace("\\", "/");
		}
				
		//wycinamy dwukropki...
		path = decolon(path);
		//plik pobierany z serwera
		String remoteFile = path + "/" + name;
	
		//plik do zapisu lokalnie
		File downloadFile = new File(dir + "/" + name);
		
	
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
		boolean success = ftpClient.retrieveFile(remoteFile, outputStream);
		outputStream.close();
	
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

	/**
	 * roz³¹czanie klineta ftp z serwerem
	 */
	private void disconnect() throws IOException
	{
		if (ftpClient.isConnected())
		{
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}

	/**
	 * ³¹czenie siê klientaFtp z serwerem
	 */
	private void connect() throws SocketException, IOException
	{
		ftpClient.connect(server, port);
		ftpClient.login(user, pass);
		ftpClient.enterLocalPassiveMode();
	}

	/**
	 * tworzy (jeœli nie istnieje) i zmienia roboczy katalog o zadanej œcie¿ce
	 */
	private void makeDirTree(String path) throws IOException
	{
		Stack <String> dirs = new Stack <String>();
		File tempFile = new File(path);
		
		while (tempFile.getParent() != null)
		{
			dirs.push(tempFile.getPath());
			tempFile = new File(tempFile.getParent());
		}

		dirs.push(tempFile.getPath());
		
		while (!dirs.empty())
		{
			String subdir;
			subdir = dirs.pop();

			//wycinamy dwukropki...
			subdir = decolon(subdir);
				
			if (ftpClient.changeWorkingDirectory("/" + subdir) == false)
			{
				//punktem wyjœciowym dla tworzenia katalogów jest katalog g³ówny
				ftpClient.changeWorkingDirectory("/");
				//nie ma takiego folderu, wiêc twórz
				ftpClient.makeDirectory(subdir);
			}
		}
		path = decolon(path);
		ftpClient.changeWorkingDirectory("/" + path);
	}

	/**
	 * usuwa drugi znak ze Sringa (dwukropek oznaczaj¹cy literê dysku)
	 */
	private String decolon(String s)
	{
		return s.substring(0, 1) + s.substring(2);
	}
}

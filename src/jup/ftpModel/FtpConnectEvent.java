package jup.ftpModel;

public class FtpConnectEvent extends FtpEvent
{
	private final String server;
	private final String login;
	private final String pass;
	private final int port;
	
	public FtpConnectEvent(String server, String login, String pass, int port)
	{
		this.server = server;
		this.login = login;
		this.pass = pass;
		this.port = port;
	}

	public String getServer()
	{
		return server;
	}

	public String getLogin()
	{
		return login;
	}

	public String getPass()
	{
		return pass;
	}

	public int getPort()
	{
		return port;
	}
}
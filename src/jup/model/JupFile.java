package jup.model;

public class JupFile
{
	private final String path;
	private final String name;
	private Long size;
	private FileStatus status;
	
	/**
	 * konstruktor bez informacji o rozmiarze
	 */
	JupFile(String p, String n)
	{
		this.path = p;
		this.name = n;
	}
	
	/**
	 * konstruktor wraz z rozmiarem
	 */
	JupFile(String p, String n, long s)
	{
		this.path = p;
		this.name = n;
		this.size = s;
	}
}

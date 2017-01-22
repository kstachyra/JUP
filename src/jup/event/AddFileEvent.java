package jup.event;

public class AddFileEvent extends JupEvent
{
	private final String path;
	private final String name;
	
	/**
	 * konstruktor przyjmuj¹cy œcie¿kê pliku
	 */
	public AddFileEvent(String path, String name)
	{
		super();
		this.path = path;
		this.name = name;
	}
	
	/**
	 * zwraca œciezkê dodawanego pliku
	 */
	public String getPath()
	{
		return path;
	}
	
	/**
	 * zwraca nazwê dodawanego pliku
	 */
	public String getName()
	{
		return name;
	}
}
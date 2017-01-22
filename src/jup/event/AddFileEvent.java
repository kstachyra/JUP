package jup.event;

public class AddFileEvent extends JupEvent
{
	private final String path;
	private final String name;
	
	/**
	 * konstruktor przyjmuj�cy �cie�k� pliku
	 */
	public AddFileEvent(String path, String name)
	{
		super();
		this.path = path;
		this.name = name;
	}
	
	/**
	 * zwraca �ciezk� dodawanego pliku
	 */
	public String getPath()
	{
		return path;
	}
	
	/**
	 * zwraca nazw� dodawanego pliku
	 */
	public String getName()
	{
		return name;
	}
}
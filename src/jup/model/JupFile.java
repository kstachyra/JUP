package jup.model;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class JupFile
{
	private final String path;
	private final String name;

	private long size;
	private long checksum = 0L;
	private FileStatus status;
	
	/**
	 * konstruktor dla nowych plików
	 */
	JupFile(String p, String n)
	{
		this.path = p;
		this.name = n;
		this.status = FileStatus.NEW;
		this.size = (new File(path, name).length())/(1024);
		if (this.size == 0 ) this.checksum = 0;
		else updateChecksum();
	}
	
	/**
	 * konstruktor dla zapamiêtanych plików
	 */
	JupFile(String p, String n, FileStatus stat, long c, long s)
	{
		this.path = p;
		this.name = n;
		this.checksum = c;
		this.size = s;
		
		this.status = stat;
	}
	
	@Override
    public boolean equals(Object o)
	{
        if (o == this) return true;
        if (!(o instanceof JupFile)) return false;
        JupFile other = (JupFile) o;

        if (this.checksum == other.checksum && this.size == other.size &&
        	this.path.equals(other.path) && this.name.equals(other.name))
        {
        	return true;
        }
		return false;
    }
	
	@Override
    public int hashCode()
	{
        return Objects.hash(checksum, size, path, name);
    }
	
	public void updateChecksum()
	{
		try
		{
			this.checksum = Adler.calc(path, name);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Long getSize()
	{
		return size;
	}

	public Long getChecksum()
	{
		return checksum;
	}

	public FileStatus getStatus()
	{
		return status;
	}

	public void setStatus(FileStatus status)
	{
		this.status = status;
	}

	public String getPath()
	{
		return path;
	}

	public String getName()
	{
		return name;
	}
}

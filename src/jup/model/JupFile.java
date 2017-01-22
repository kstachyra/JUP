package jup.model;

import java.io.IOException;
import java.util.Objects;

public class JupFile
{
	private final String path;
	private final String name;

	private final long size;
	private final long lastModified;
	
	private long checksum = 0L;
	private FileStatus status;
	
	JupFile(String p, String n, long s, long lm)
	{
		this.path = p;
		this.name = n;
		this.size = s;
		this.lastModified = lm;
		this.status = FileStatus.NEW;
		
		try
		{
			this.checksum = Adler.calc(path, name);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
    public boolean equals(Object o)
	{
        if (o == this) return true;
        if (!(o instanceof JupFile)) return false;
        JupFile other = (JupFile) o;

        if (this.checksum == other.checksum && this.size == other.size &&
        	this.lastModified == other.lastModified &&
        	this.path.equals(other.path) && this.name.equals(other.name))
        {
        	return true;
        }
		return false;
    }
	
	@Override
    public int hashCode()
	{
        return Objects.hash(checksum, size, lastModified, path, name);
    }
	
	public Long getSize()
	{
		return size;
	}

	public Long getLastModified()
	{
		return lastModified;
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

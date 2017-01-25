package jup.ftpModel;

import java.util.Objects;

public class FtpDisconnectEvent extends FtpEvent
{
	@Override
    public boolean equals(Object o)
	{
        if (!(o instanceof FtpDisconnectEvent))
        	{
        	System.out.println("DSFGDSFGFDSFG");return false;
        	}
        else return true;
    }
	
	@Override
    public int hashCode()
	{
        return Objects.hash(this.getClass());
    }
}
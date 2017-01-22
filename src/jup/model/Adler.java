package jup.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/*
 * klasa zaczerpniêta z:
 * http://www.jguru.com/faq/view.jsp?EID=216274
 */

/**
 * oblicza sumê kontroln¹ dla pliku
 */
public class Adler
{
	public static long calc(String path, String name) throws IOException
    {
        FileInputStream file = new FileInputStream(new File(path, name));
        CheckedInputStream check = new CheckedInputStream(file, new Adler32());
        BufferedInputStream input = new BufferedInputStream(check);
        while (input.read() != -1){}
        input.close();
		return check.getChecksum().getValue();
    }
}
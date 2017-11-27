/*
 * CC BY-SA 4.0
 *
 * The code is licensed with "Attribution-ShareAlike 4.0 International license".
 * See the license details:
 *     https://creativecommons.org/licenses/by-sa/4.0/
 *
 * Copyright (C) 2017 Vladimír Ulman
 */
package de.mpicbg.ulman.inputParsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * this class implements a log reader for
 * DAIS wp1.3 -- an image transfer mini-library
 */
public class DAISwp13 extends AbstractParser
{
	///the handle to the log file
	final BufferedReader reader;

	///constructor
	public
	DAISwp13(final File logFile)
	throws IOException
	{
		//open the log file
		reader = Files.newBufferedReader(logFile.toPath());
	}

	protected void finalize()
	{
		//try to close,
		//possibly closing 2nd time (if reading in readNextXYMsg() went well)
		try {
			reader.close();
		}
		catch (IOException e)
		{ } //just don't complain
	}

	@Override
	protected
	void readNextXYMsg()
	{
		try {
			//get line from the file
			final String line = reader.readLine();
			if (line == null)
				throw new IOException("problem reading line");

			//have we reached end of file?
			if (! reader.ready())
			{
				reader.close();
				isThereNext = false;
			}

			//prepare message container
			currentEvent.msg.clear();

			//the message title
			final int delimIdx = line.indexOf(':');
			currentEvent.msg.add(String.format("%.3f secs",
			                       Float.parseFloat(line.substring(0,delimIdx-1))/1000.));
			//time stamps as 'y'
			//currentEvent.y = Integer.parseInt(line.substring(0,delimIdx-1));
			//line number as 'y'
			currentEvent.y = counter;
			counter += 1;
			//NB: were utilizing Event.title here, hence consider 2 lines per event

			//the message body
			final String restOfLine = line.substring(delimIdx+2);
			currentEvent.x = (restOfLine.startsWith("se") || restOfLine.startsWith("Se"))? "S" : "R";
			currentEvent.msg.add(restOfLine);
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	private long counter = 1;
}

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
public class MitoGen extends AbstractParser
{
	///the handle to the log file
	final BufferedReader reader;

	///constructor
	public
	MitoGen(final File logFile)
	throws IOException
	{
		//open the log file
		reader = Files.newBufferedReader(logFile.toPath());

		//reads until the intro part is over
		String line = reader.readLine();
		while (  line != null
		      && line.startsWith("scheduler.cpp::Run_st(): Starting") == false)
			line = reader.readLine();

		if (line == null)
			throw new IOException("Incorrect log format during initialization.");
			//isThereNext = false; //alternatively, just block from other reading
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
			//get always two lines from the file
			final String lineH = reader.readLine();
			final String lineM = reader.readLine();
			if (lineH == null || lineM == null)
				throw new IOException("problem reading pair of lines");

			//have we reached end of file?
			if (! reader.ready())
			{
				reader.close();
				isThereNext = false;
			}

			//prepare message container
			currentEvent.msg.clear();

			//sample log line:
			//Thread 1 at 00:05:04:248, Cell ID: 3,

			//the message source -- thread ID
			final int atPos = lineH.indexOf(" at");
			currentEvent.x = lineH.substring(0,atPos);

			//could extract time here....

			//line number as 'y'
			currentEvent.y = counter;
			counter += 2;
			//NB: were utilizing Event.title here, hence consider 2 lines per event

			//the message title and body
			currentEvent.msg.add(lineH.substring(atPos+4));
			currentEvent.msg.add(lineM);
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	private long counter = 1;
}

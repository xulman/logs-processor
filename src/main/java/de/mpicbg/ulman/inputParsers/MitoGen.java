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

	/**
	 * what section of the MitoGen log are we reading now:
	 * 0 - initial part, line by line, assigned to "Thread 1"
	 * 1 - middle part, by pairs of lines, assigned to parsed threads
	 * 2 - closing part, line by line, assigned to "Thread 1"
	 */
	int readingPhase = 0;

	///constructor
	public
	MitoGen(final File logFile)
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
			//prepare message container
			currentEvent.msg.clear();

			//helper whole-log-line-holder/variable for the switch
			String line = null;
			switch (readingPhase)
			{
			case 0:
				currentEvent.x = "Thread 1";
				currentEvent.y = 0;
				currentEvent.msg.add("00:00, initialization"); //title of the msg

				//reads until the intro part is over
				line = reader.readLine();
				while (  line != null
						&& line.startsWith("scheduler.cpp::Run_st(): Starting") == false)
				{
					currentEvent.msg.add(line); //body of the msg
					line = reader.readLine();
				}

				if (line == null)
					throw new IOException("Incorrect log format during initialization.");
					//isThereNext = false; //alternatively, just block from other reading
				currentEvent.msg.add(line); //body of the msg (the line that startsWith() == true)

				readingPhase = 1;
				break;

			case 1:
				//process always two lines from the file
				//get the first line
				line = reader.readLine();
				if (line == null)
					throw new IOException("problem reading first in the pair of lines");

				//sample "line" log line:
				//Thread 1 at 00:05:04:248, Cell ID: 3,

				final int atPos = line.indexOf(" at");
				if (atPos > 0)
				{
					//likely still in the middle section of the log,
					//continue processing it and read the second line later too

					//the message source -- thread ID
					currentEvent.x = line.substring(0,atPos);

					//extract time here...
					lastExtractedTime  = Long.parseLong(line.substring(atPos+4,atPos+6))*3600000;
					lastExtractedTime += Long.parseLong(line.substring(atPos+7,atPos+9))*60000;
					lastExtractedTime += Long.parseLong(line.substring(atPos+10,atPos+12))*1000;
					lastExtractedTime += Long.parseLong(line.substring(atPos+13,atPos+16));
					currentEvent.y = lastExtractedTime;

					//the message title
					currentEvent.msg.add(line.substring(atPos+4));

					//second line -- the message body
					line = reader.readLine();
					if (line == null)
						throw new IOException("problem reading second in the pair of lines");
					currentEvent.msg.add(line);
					break;
				}

				//if we got here, we likely have just reached first line of the last log section
				readingPhase = 2;
				currentEvent.x = "Thread 1";
				currentEvent.y = lastExtractedTime + 2*getTypicalTimeResolution();
				currentEvent.msg.add("99:99:99, closing"); //title of the msg
				currentEvent.msg.add(line);             //first line of the body of the msg

			case 2:
				//read until the end of file?
				while (reader.ready())
				{
					line = reader.readLine();
					if (line == null)
						throw new IOException("Incorrect log format during closing.");
					currentEvent.msg.add(line); //body of the msg
				}

				isThereNext = false;
				reader.close();

				readingPhase = 99; //make it jump to the empty default, just in case...
				break;

			default:
			}
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	///the last extracted time point value, required when readingPhase == 2
	private
	long lastExtractedTime = 0;

	@Override
	///use 100ms grouping by default
	public
	long getTypicalTimeResolution()
	{ return 100; }
}

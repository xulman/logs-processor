/*
 * CC BY-SA 4.0
 *
 * The code is licensed with "Attribution-ShareAlike 4.0 International license".
 * See the license details:
 *     https://creativecommons.org/licenses/by-sa/4.0/
 *
 * Copyright (C) 2017 Vladimír Ulman
 */
package de.mpicbg.ulman.outputPresenters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * this class just prints coordinates and messages on the standard output
 */
public class HTML extends AbstractPresenter
{
	///the handle to the log file
	final BufferedWriter writer;

	///constructor
	public
	HTML(final File htmlFile)
	throws IOException
	{
		//create the HTML file
		writer = Files.newBufferedWriter(htmlFile.toPath());
	}

	protected void finalize()
	{
		//try to close,
		//possibly closing 2nd time (if reading in readNextXYMsg() went well)
		try {
			writer.close();
		}
		catch (IOException e)
		{ } //just don't complain
	}

	@Override
	public
	void initialize(final long _xColumns,
	                final long _yMin,
	                final long _yMax,
	                final long _msgWidthChars,
	                final long _msgMaxLines)
	{
		backUpHints(_xColumns, _yMin,_yMax, _msgWidthChars,_msgMaxLines);
		ySpan = (yMax-yMin)*20*msgMaxLines;
		//ySpan = (yMax-yMin)/1000*20*msgMaxLines; - when timestamps are involved...

		try {
			writer.append("<body>");
			writer.newLine();
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	private long ySpan;

	@Override
	public
	void show(final String x, final long y, final String msg)
	{
		/*
		notes:
			20px na vysku per radek
			7px na sirku per pismeno (on average)
			misto \n musi byt <br/>
		*/
		try {
			long posX = 7*msgWidthChars * getColumnNo(x);
			long posY = (ySpan*(y - yMin))/(yMax-yMin);
			writer.append("<span style=\"position:absolute; left:"+posX+"px; top:"+posY+"px;\">");
			writer.append(msg);
			writer.append("</span>");
			writer.newLine();
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	@Override
	public
	void close()
	{
		try {
			writer.append("</body>");
			writer.close();
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}
}

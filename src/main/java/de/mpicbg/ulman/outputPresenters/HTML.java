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

import de.mpicbg.ulman.Event;

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
		ySpan = (yMax-yMin)*yCharStep; //*msgMaxLines;

		try {
			writer.append(
 "<!DOCTYPE html>\n"
+"<!-- Adopted from: https://www.w3schools.com/css/css_tooltip.asp -->\n"
+"<html>\n"
+"<style>\n"
+".tooltip {\n"
+"    position: relative;\n"
+"    display: inline-block;\n"
+"}\n"
+"\n"
+".tooltip .tooltiptext {\n"
+"    visibility: hidden;\n"
+"    width: "+(xCharStep*msgWidthChars)+"px;\n"
+"    background-color: #eee;\n"
+"    color: black;\n"
+"    text-align: left;\n"
+"    border-radius: 6px;\n"
+"    padding: "+padding+"px "+padding+"px;\n"
+"\n"
+"    /* Position the tooltip */\n"
+"    position: absolute;\n"
+"    z-index: 1;\n"
+"    top: -"+padding+"px;\n"
+"    left: -"+padding+"px;\n"
+"}\n"
+"\n"
+".tooltip:hover .tooltiptext {\n"
+"    visibility: visible;\n"
+"}\n"
+"</style>\n"
+"<body>\n");
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	///in the tooltip: border thickness around the text
	private long padding = 3;

	///approx/mean width and height of a box (in pixels) required to host one character
	private long xCharStep = 8;
	private long yCharStep = 20;

	private long ySpan;

	@Override
	public
	void show(final Event e)
	{
		/*
		notes:
			20px na vysku per radek
			7px na sirku per pismeno (on average)
			misto \n musi byt <br/>
		*/
		try {
			//position
			final long posX = padding+ (10 + xCharStep*msgWidthChars) * getColumnNo(e.x);
			final long posY = padding+ (ySpan*(e.y - yMin))/(yMax-yMin);

			writer.append("<div class=\"tooltip\" style=\"position:absolute; left:"+posX+"px; top:"+posY+"px;\">");
			//always visible content
			writer.append(e.msg.firstElement());
			writer.newLine();
			writer.append("<span class=\"tooltiptext\">");
			for (String msg : e.msg)
			{
				writer.append(msg);
				writer.append("<br/>");
			}
			writer.newLine();
			writer.append("</span></div>");
			writer.newLine();
		}
		catch (IOException E) {
			 System.err.format("IOException: %s%n", E);
			 throw new RuntimeException();
		}
	}

	@Override
	public
	void close()
	{
		try {
			writer.append("</body>\n</html>");
			writer.close();
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}
}

/*-
 * #%L
 * 2D Logs Processor
 * %%
 * Copyright (C) 2017 - 2025 Vladimir Ulman
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
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

	///constructor with no column headers
	public
	HTML(final File htmlFile, final int _columnWidthChars)
	throws IOException
	{
		this(htmlFile,_columnWidthChars,0);
	}

	///constructor with column headers of the given height (as no. of rows)
	public
	HTML(final File htmlFile, final int _columnWidthChars, final int _headerRows)
	throws IOException
	{
		//create the HTML file
		writer = Files.newBufferedWriter(htmlFile.toPath());
		columnWidthChars = _columnWidthChars;
		headerRows = _headerRows;
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

	///what is the width of every column
	final int columnWidthChars;

	///how many lines should be reserved for printing header on top of every column
	final int headerRows;

	@Override
	public
	void initialize(final long _xColumns,
	                final long _yMin,
	                final long _yMax,
	                final long _msgWidthChars,
	                final long _msgMaxLines)
	{
		backUpHints(_xColumns, _yMin,_yMax, _msgWidthChars,_msgMaxLines);

		if (yMax == yMin)
		{
			yMax = yMin+1;
			System.out.println("INIT: auto-fixing not to have zero y-span");
		}

		ySpan = (yMax-yMin)*yCharStep; //*msgMaxLines;

		try {
			writer.append(
 "<!DOCTYPE html>\n"
+"<!-- Adopted from: https://www.w3schools.com/css/css_tooltip.asp -->\n"
+"<html>\n"
+"<style>\n"
+".toolhead {\n"
+"    color: brown;\n"
+"    border-top: 1px dotted black;\n"
+"    position: absolute;\n"
+"    width: "+(xCharStep*columnWidthChars)+"px;\n"
+"}\n"
+"\n"
+".tooltip {\n"
+"    position: absolute;\n"
+"    display: inline-block;\n"
+"    width: "+(xCharStep*columnWidthChars)+"px;\n"
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
+".background {\n"
+"    position: absolute;\n"
+"    top: 0px;\n"
+"    width: "+(xCharStep*columnWidthChars)+"px;\n"
+"    height: "+(yCharStep*(yMax-yMin+1 +headerRows))+"px;\n"
+"}\n"
+"\n"
+".tooltip:hover .tooltiptext {\n"
+"    visibility: visible;\n"
+"}\n"
+"</style>\n"
+"<body>\n");
			final String[] colors = {"ffffcc", "d9ffcc", "ffe6cc", "ccf2ff"};
			for (int i=0; i < xColumns; ++i)
				writer.append("<div class=\"background\" style=\"background-color:#"+
				  colors[i%4]+"; left:"+
				  (padding+ (10 + xCharStep*columnWidthChars)*i)+"px;\"></div>\n");
		}
		catch (IOException e) {
			 System.err.format("IOException: %s%n", e);
			 throw new RuntimeException();
		}
	}

	///in the tooltip: border thickness around the text
	protected long padding = 3;

	///approx/mean width and height of a box (in pixels) required to host one character
	protected long xCharStep = 8;
	protected long yCharStep = 20;

	protected long ySpan;

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
			final long posX = padding+ (10 + xCharStep*columnWidthChars) * getColumnNo(e.x);
			final long posY = padding+ (ySpan*(e.y - yMin))/(yMax-yMin) +yCharStep*headerRows;

			if (isNewColumnStarted() && headerRows > 0)
			{
				//print header now, center-vertically
				final long hPosY = yCharStep*(headerRows-1)/2 + padding;
				writer.append("<div class=\"toolhead\" style=\"border-top: none; border-bottom: 1px dotted black; left:"+posX+"px; top:"+hPosY+"px;\">");
				//
				//make sure the header fits into the column
				if (e.x.length() < columnWidthChars) writer.append(e.x);
				else writer.append(e.x.substring(0,columnWidthChars-3)+"...");
				writer.append("</div>");
				writer.newLine();
			}

			writer.append("<div class=\"tooltip\" style=\"left:"+posX+"px; top:"+posY+"px;\">");
			//always visible content
			final String visLine = e.msg.firstElement();
			if (visLine.length() < columnWidthChars)
				//the whole line fits the column width
				writer.append(visLine);
			else
				//the line has to be trimmed
				writer.append(visLine.substring(0,columnWidthChars-3)+"...");
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

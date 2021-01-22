/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2017, Vladimír Ulman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.mpicbg.ulman.outputPresenters;

import de.mpicbg.ulman.Event;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * this class just prints coordinates and messages on the standard output
 */
public class HTMLwithHeaders extends HTML
{
	public
	HTMLwithHeaders(final File htmlFile, final int _columnWidthChars)
	throws IOException
	{
		super(htmlFile, _columnWidthChars);
	}

	@Override
	public
	void initialize(final long _xColumns,
	                final long _yMin,
	                final long _yMax,
	                final long _msgWidthChars,
	                final long _msgMaxLines)
	{
		super.initialize(_xColumns, 2*_yMin, 2*_yMax +1, _msgWidthChars, _msgMaxLines);
	}

	@Override
	public
	void show(final Event e)
	{
		try {
			//position
			final long posX = padding+ (10 + xCharStep*columnWidthChars) * getColumnNo(e.x);
			final long posY = padding+ (ySpan*(2*e.y - yMin))/(yMax-yMin);

			final Iterator<String> i = e.msg.iterator();
			writer.append("<div class=\"toolhead\" style=\"left:"+posX+"px; top:"+posY+"px;\">");
			//always visible title/header
			writer.append(i.next());
			writer.append("</div>");
			writer.newLine();
			writer.append("<div class=\"tooltip\"  style=\"left:"+posX+"px; top:"+(posY+yCharStep)+"px;\">");
			final String tmp = i.next();
			//always visible content
			if (tmp.length() < columnWidthChars)
				//the whole line fits the column width
				writer.append(tmp);
			else
				//the line has to be trimmed
				writer.append(tmp.substring(0,columnWidthChars-3)+"...");
			writer.newLine();
			writer.append("<span class=\"tooltiptext\">");
			writer.append(tmp);
			while (i.hasNext())
			{
				writer.append("<br/>");
				writer.append(i.next());
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
}

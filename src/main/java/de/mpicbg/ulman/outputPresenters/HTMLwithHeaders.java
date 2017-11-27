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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * this class just prints coordinates and messages on the standard output
 */
public class HTMLwithHeaders extends HTML
{
	public
	HTMLwithHeaders(final File htmlFile)
	throws IOException
	{
		super(htmlFile);
	}

	@Override
	public
	void show(final Event e)
	{
		try {
			//position
			final long posX = padding+ (10 + xCharStep*msgWidthChars) * getColumnNo(e.x);
			final long posY = padding+ (ySpan*(e.y - yMin))/(yMax-yMin);

			final Iterator<String> i = e.msg.iterator();
			writer.append("<div style=\"color: brown; border-top: 1px dotted black; position:absolute; left:"+posX+"px; top:"+posY+"px;\">");
			//always visible title/header
			writer.append(i.next());
			writer.append("</div>");
			writer.newLine();
			writer.append("<div class=\"tooltip\" style=\"position:absolute; left:"+posX+"px; top:"+(posY+yCharStep)+"px;\">");
			final String tmp = i.next();
			//always visible content
			writer.append(tmp);
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

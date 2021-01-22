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

import net.imagej.ImageJ;
import org.scijava.table.GenericTable;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.GenericColumn;

/**
 * this class just prints coordinates and messages on the standard output
 */
public class FijiTable extends AbstractPresenter
{
	///constructor
	public
	FijiTable(final ImageJ _ij)
	{
		//use existing ImageJ instance, if some is given to us
		if (_ij == null)
			ij = new ImageJ();
		else
			ij = _ij;

		//create the output table
		table = new DefaultGenericTable();
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
        ij.ui().showUI();
	}

	///handle to the instance that would show the finished table
	final ImageJ ij;

	///handles to the table and columns to build the table
	final GenericTable table;
	GenericColumn column = null;

	///helper variable to detect that a new column is being populated
	String lastX = null;

	///helper variable to detect that empty rows has to be added
	long lastY = 0;

	@Override
	public
	void show(final Event e)
	{
		//plan:
		//detect if we have started a new column;
		//a) we have: add the old one to the table, create a new column, reset row_counter
		//b) we haven't: nothing special
		//afterwards, check how many empty rows has to be added (which is the difference
		//between the current e.y and the row_counter minus 1, add them and add the event

		//new column?
		if (lastX == null || lastX.equals(e.x) == false)
		{
			//yes, new column
			//add existing one...
			if (column != null) table.add(column);

			//start a new one
			column = new GenericColumn(e.x);
			lastX = e.x;
			lastY = yMin-1;
		}

		//add empty rows?
		while (lastY+1 < e.y)
		{
			//lastY must finish just prior/row_above the current e.y...
			column.add(null);
			++lastY;
		}

		//add the current/requested row
		int linesRemaining = e.msg.size();
		StringBuffer s = new StringBuffer((int)(msgWidthChars*linesRemaining));
		for (String m : e.msg)
		{
			s.append(m);
			if (--linesRemaining > 0) s.append("\n");
		}
		column.add(s);
		lastY = e.y;
	}

	@Override
	public
	void close()
	{
		//add the column that's been edited the last
		if (column != null) table.add(column);
		ij.ui().show(table);
	}
}

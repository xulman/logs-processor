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

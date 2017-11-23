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

/**
 * this class just prints coordinates and messages on the standard output
 */
public class AbstractPresenter implements Presenter
{
	///backup of the hinting attributes
	protected long xColumns;
	protected long yMin,yMax;
	protected long msgWidthChars;
	protected long msgMaxLines;

	///helper function to do the backup
	protected
	void backUpHints(final long _xColumns,
	                 final long _yMin,
	                 final long _yMax,
	                 final long _msgWidthChars,
	                 final long _msgMaxLines)
	{
		xColumns      = _xColumns;
		yMin          = _yMin;
		yMax          = _yMax;
		msgWidthChars = _msgWidthChars;
		msgMaxLines   = _msgMaxLines;
	}

	@Override
	public
	void initialize(final long _xColumns,
	                final long _yMin,
	                final long _yMax,
	                final long _msgWidthChars,
	                final long _msgMaxLines)
	{
		//backup...
		backUpHints(_xColumns, _yMin,_yMax, _msgWidthChars,_msgMaxLines);

		System.out.println("INIT: 0 <= x <= "+(xColumns-1)+", "
			+yMin+" <= y <= "+yMax+", msgWidthChars: "
			+msgWidthChars+", msgMaxLines: "+msgMaxLines);
	}

	@Override
	public
	void show(final String x, final long y, final String msg)
	{
		System.out.println("MSG from \""+x+"\": "+getColumnNo(x)+","+y+": "+msg);
	}

	@Override
	public
	void close()
	{
		System.out.println("CLOSE:");
	}

	/**
	 * a helper function to convert label 'x' into "its" column index,
	 * this function relies heavily on the assumption that logs are
	 * fed in an appropriate order, see description of the Presenter interface
	 */
	protected
	long getColumnNo(final String x)
	{
		//hmm... just return the first column to be on the safe side
		if (x == null) return(0);

		//used for the first time?
		//or, are we seeing this 'x' for the first time?
		if (lastColumnLabel == null || lastColumnLabel.equals(x) == false)
		{
			//move to the new/next column index and remember its label 'x'
			++lastColumnIndex;
			lastColumnLabel = x;
		}

		return lastColumnIndex;
	}

	///the label of the last seen column, works in conjunction with getColumnNo()
	private String lastColumnLabel = null;
	///the index of the last seen column, works in conjunction with getColumnNo()
	private long   lastColumnIndex = -1;
}

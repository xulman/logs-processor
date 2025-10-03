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
	void show(final Event e)
	{
		for (String msg : e.msg)
			System.out.println("MSG from \""+e.x+"\": "+getColumnNo(e.x)+","+e.y+": "+msg);
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
			isNewColumnStarted = true;
		}
		else isNewColumnStarted = false;

		return lastColumnIndex;
	}

	///the label of the last seen column, works in conjunction with getColumnNo()
	private String lastColumnLabel = null;
	///the index of the last seen column, works in conjunction with getColumnNo()
	private long   lastColumnIndex = -1;

	/** flags if this.getColumnNo() has just started a new column,
	    this is useful to see if a column header should be drawn */
	private boolean isNewColumnStarted = false;
	protected boolean isNewColumnStarted() { return isNewColumnStarted; }
}

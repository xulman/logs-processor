/*
 * CC BY-SA 4.0
 *
 * The code is licensed with "Attribution-ShareAlike 4.0 International license".
 * See the license details:
 *     https://creativecommons.org/licenses/by-sa/4.0/
 *
 * Copyright (C) 2017 Vladimír Ulman
 */
package de.mpicbg.ulman;

import de.mpicbg.ulman.inputParsers.Parser;
import de.mpicbg.ulman.outputPresenters.Presenter;


public class ShowLogsStreamed
{
	/** no default/easy construction allowed */
	@SuppressWarnings("unused")
	private ShowLogsStreamed()
	{
		this(null,null, -1, -1,-1, -1,-1);
	}

	/** this is the main constructor; cannot switch parser/presenter during operation,
	    and must provide hints for the presenter (as this class does not pre-read
	    all the logs to figure out the hints itself) */
	public ShowLogsStreamed(final Parser _pa, final Presenter _pr,
	                        final long xColumns,
	                        final long yMin, final long yMax)
	{
		this(_pa,_pr, xColumns, yMin,yMax, 50,1);
	}

	/** this is the main constructor; cannot switch parser/presenter during operation,
	    and must provide hints for the presenter (as this class does not pre-read
	    all the logs to figure out the hints itself) */
	public ShowLogsStreamed(final Parser _pa, final Presenter _pr,
	                        final long xColumns,
	                        final long yMin, final long yMax,
	                        final long msgWrap,
	                        final long msgMaxLines)
	{
		parser = _pa; presenter = _pr;

		this.xColumns = xColumns;
		this.yMin = yMin;
		this.yMax = yMax;

		this.msgWrap = msgWrap;
		this.msgMaxLines = msgMaxLines;
	}

	/** the parser used in this story */
	final Parser parser;

	/** the presenter used in this story */
	final Presenter presenter;

	/** the number of columns expected */
	final long xColumns;

	/** interval for the y-axis */
	final long yMin;
	final long yMax;

	/** wrap messages after this number of characters; note that the Event messages can be multi-line */
	final long msgWrap;

	/** how many lines will be the longest message */
	final long msgMaxLines;


	/** the main worker: reads the whole input and feeds the presenter in the correct order (see Presenter) */
	public void process()
	{
		presenter.initialize(xColumns, yMin,yMax, msgWrap,msgMaxLines);
		while (parser.hasNext())
			presenter.show( parser.next() );
		presenter.close();
	}
}

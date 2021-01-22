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

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

import java.io.File;
import java.util.HashMap;

import de.mpicbg.ulman.inputParsers.Parser;
import de.mpicbg.ulman.outputPresenters.Presenter;


class loggerBackend
{
	///no default/easy construction allowed
	@SuppressWarnings("unused")
	private loggerBackend()
	{ parser = null; presenter = null; }

	///this is the main constructor; cannot switch parser/presenter during operation
	loggerBackend(final Parser _pa, final Presenter _pr)
	{ parser = _pa; presenter = _pr; }

	///the parser used in this story
	private final
	Parser parser;

	///the presenter used in this story
	private final
	Presenter presenter;

	///the number of characters at which line-breaks are introduced to the input messages
	int msgWrap = 50;


	//the main worker: reads the whole input and feeds the presenter in the correct order (see Presenter)
	void process()
	{
		//create a data structure to hold all the logs
		HashMap<String, HashMap<Long,String> > logs = new HashMap<>(50);
		//NB:     'x'            'y'  'msg'

		//how "large" will be the largest message
		long msgMaxLines = 1;

		//interval for the y-axis
		long yMin = Long.MAX_VALUE;
		long yMax = Long.MIN_VALUE;

		//fills them up by parsing
		while (parser.hasNext())
		{
			//yet another logged event:
			final Event event = parser.next();

			//TODO
			//introduce line wraps to event.msg based on this.msgWrap;
			//updates msgMaxLines accordingly

			//update y range:
			if (event.y < yMin) yMin = event.y;
			if (event.y > yMax) yMax = event.y;

			//stores the event
			HashMap<Long,String> xLog = logs.get(event.x);
			if (xLog == null)
			{
				//first occurence of the writer 'x', add to the logs
				xLog = new HashMap<>(1000);
				logs.put(event.x, xLog);
			}

			xLog.put(event.y, event.msg);
		}

		presenter.initialize(logs.size(), yMin,yMax, msgMaxLines);

		//iterate logged data in the correct order
		for (String x : logs.keySet())
		{
			HashMap<Long,String> xLog = logs.get(x);
			//xLog.sort(); //TODO -- enforce the proper order!
			for (Long y : xLog.keySet())
				presenter.show(x,y, xLog.get(y));
		}

		presenter.close();
	}
}

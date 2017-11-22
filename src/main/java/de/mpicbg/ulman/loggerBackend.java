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

import java.util.HashMap;
import java.util.TreeMap;

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
		HashMap<String, TreeMap<Long,String> > logs = new HashMap<>(100);
		//NB:     'x'            'y'  'msg'

		//how "large" will be the largest message
		long msgMaxLines = 1;

		//interval for the y-axis
		long yMin = Long.MAX_VALUE;
		long yMax = Long.MIN_VALUE;

		//fills all of them by parsing
		while (parser.hasNext())
		{
			//yet another logged event:
			final Event event = parser.next();

			if (event.msg.length() > msgWrap)
			{
				//must introduce line wraps to event.msg based on this.msgWrap;
				StringBuffer s = new StringBuffer(event.msg);
				int position = msgWrap; //cursor still without a wrapping character
				int lines = 1;
				while (position < s.length())
				{
					s.insert(position,'\n');
					position += msgWrap+1;
					++lines; //wrapping char made the msg one line longer
				}

				//updates with the "reformatted" string
				event.msg = s.toString();

				//updates msgMaxLines accordingly
				if (lines > msgMaxLines) msgMaxLines = lines;
			}

			//update y range:
			if (event.y < yMin) yMin = event.y;
			if (event.y > yMax) yMax = event.y;

			//stores the event
			TreeMap<Long,String> xLog = logs.get(event.x);
			if (xLog == null)
			{
				//first occurence of the writer 'x', add to the logs
				xLog = new TreeMap<>();
				logs.put(event.x, xLog);
			}

			xLog.put(event.y, event.msg);
		}

		//now, introduce a permutation that would prescribe the read out order
		//of the logs items ('x') such that the items are accessed in the order of
		//their smallest 'y' values
		TreeMap<Long,String> permutation = new TreeMap<>();
		//NB:    'y'  'x'
		for (String x : logs.keySet()) permutation.put(logs.get(x).firstKey(), x);


		//finally, start "presenting" the stored logs
		presenter.initialize(logs.size(), yMin,yMax, msgMaxLines);

		//iterate logged data in the correct order
		for (String x : permutation.values()) //TODO is the order okay? use iterators otherwise
		{
			TreeMap<Long,String> xLog = logs.get(x);
			//NB: Tree guarantees the 'y' values are accessed in the correct order
			for (Long y : xLog.keySet())
				presenter.show(x,y, xLog.get(y));
		}

		presenter.close();
	}
}

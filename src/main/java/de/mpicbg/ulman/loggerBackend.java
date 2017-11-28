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
import java.util.Vector;

import de.mpicbg.ulman.inputParsers.Parser;
import de.mpicbg.ulman.outputPresenters.Presenter;


class loggerBackend
{
	///no default/easy construction allowed
	@SuppressWarnings("unused")
	private loggerBackend()
	{
		parser = null; presenter = null;
		yTimeStep = 1;
	}

	///this is the main constructor; cannot switch parser/presenter during operation
	loggerBackend(final Parser _pa, final Presenter _pr, final long _yTimeStep)
	{
		parser = _pa; presenter = _pr;
		yTimeStep = _yTimeStep;
	}

	///the parser used in this story
	private final
	Parser parser;

	///the presenter used in this story
	private final
	Presenter presenter;

	/**
	 * y-axis grouping interval, such that events from different
	 * loggers/sources 'x' can be understood as happening "nearly
	 * at the same time" if their time stamps are not more than this
	 * value appart (the distance value takes, therefore, the same
	 * units as are used for 'y' axis in the original log file);
	 * the events that fall within this distance are ideally to be
	 * displayed at the same position on the 'y' axis of the 2D/flattish
	 * view
	 *
	 * this parameter is relevant to \e this.parser only
	 */
	final long yTimeStep;

	///the number of characters at which line-breaks are introduced to the input messages
	int msgWrap = 50;


	//the main worker: reads the whole input and feeds the presenter in the correct order (see Presenter)
	void process()
	{
		//create a data structure to hold all the logs
		HashMap<String, TreeMap<Long,Event> > logs = new HashMap<>(100);
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

			//"move" the message lines from the original msg container
			//and wrap them during that
			Vector<String> wrappedMsg = new Vector<>(3*event.msg.size() /2);
			for (String msg : event.msg)
			{
				//sweep the input msg with msgWrap-long consecutive intervals
				int posFrom = 0;
				int posTo = Math.min(msgWrap, msg.length());
				while (posFrom < posTo)
				{
					wrappedMsg.add(msg.substring(posFrom, posTo));
					posFrom = posTo;
					posTo = Math.min(posTo+msgWrap, msg.length());
				}

				//all copied, indicate to GC that this one is not needed anymore
				msg = null;
			}

			//Vector of messages: clear the old one and move the new one instead
			event.msg.clear();
			event.msg = wrappedMsg;

			//updates msgMaxLines accordingly
			if (wrappedMsg.size() > msgMaxLines) msgMaxLines = wrappedMsg.size();

			//update y range:
			if (event.y < yMin) yMin = event.y;
			if (event.y > yMax) yMax = event.y;

			//stores the event
			TreeMap<Long,Event> xLog = logs.get(event.x);
			if (xLog == null)
			{
				//first occurrence of the writer 'x', add to the logs
				xLog = new TreeMap<>();
				logs.put(event.x, xLog);
			}

			xLog.put(event.y, event);
		}

		//now, introduce a permutation that would prescribe the read out order
		//of the logs items ('x') such that the items are accessed in the order of
		//their smallest 'y' values
		TreeMap<Long,String> permutation = new TreeMap<>();
		//NB:    'y'  'x'
		for (String x : logs.keySet()) permutation.put(logs.get(x).firstKey(), x);

		//now, convert time stamps into (table) row numbers such that presenters can produce
		//dense/compact views (if time-stamps were too apart from each other, this manifested
		//itself as a long empty space between displays of the consecutive events) and allow
		//multiple readers to occupy the same (table) row (of course, in their respective columns)
		//
		//we gonna introduce a set of row markers on the y-axis, every marker binds a particular
		//row number with a time stamp; no event with earlier time stamp should be displayed
		//above (with smaller row number) this marker; the spacing between consecutive markers
		//is driven only by the amount of events that fall between the two markers -- so markers
		//row-spacing is not regular/fixed, but spacing of markers on the time stamp axis is
		//regular to ease the computation/programming
		//
		//a marker at index m gathers all events e for which it holds:
		//  ceil((e.y-yMin) / yTimeStep) = m
		//and these events must be displayed at y-coordinates between yMarkers[m] and yMarkers[m+1]
		long yMarkers[] = new long[(int)((yMax-yMin) / yTimeStep) +1];

		//plan:
		//first pass: for every marker index m, count maximum, over all sources 'x',
		//            number of events that fall into the index m
		//second pass: adjust yMarkers[] values accordingly

		//first pass: for every marker index m, count maximum, over all sources 'x',
		//            number of events that fall into the index m
		//TODO

		//second pass: adjust yMarkers[] values accordingly,
		//i.e. yMarkers[m] = sum_i=0..m-1 yMarkers[i] and yMarkers[0] = 0;
		long currentLength = yMarkers[0]; //max number of events that fall into 0-th marker
		yMarkers[0] = 0;                  //min y-row-coordinate of these events
		for (int i=1; i < yMarkers.length; ++i)
		{
			final long bckp = yMarkers[i];
			yMarkers[i] = yMarkers[i-1]+currentLength;
			currentLength = bckp;
		}

		//finally, start "presenting" the stored logs
		presenter.initialize(logs.size(), yMin,yMax, msgWrap,msgMaxLines);

		//iterate logged data in the correct order
		for (String x : permutation.values())
		{
			TreeMap<Long,Event> xLog = logs.get(x);
			//NB: Tree guarantees the 'y' values are accessed in the correct order
			for (Long y : xLog.keySet())
				presenter.show(xLog.get(y));
		}

		presenter.close();
	}
}
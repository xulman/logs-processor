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
package de.mpicbg.ulman;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.LinkedList;
import java.util.ListIterator;

import de.mpicbg.ulman.inputParsers.Parser;
import de.mpicbg.ulman.outputPresenters.Presenter;


public class ShowLogsCompacted
{
	/** no default/easy construction allowed */
	@SuppressWarnings("unused")
	private ShowLogsCompacted()
	{
		parser = null; presenter = null;
		yTimeStep = 1;
	}

	/** this is the main constructor; cannot switch parser/presenter during operation */
	public ShowLogsCompacted(final Parser _pa, final Presenter _pr, final long _yTimeStep)
	{
		parser = _pa; presenter = _pr;
		yTimeStep = _yTimeStep;
	}

	/** the parser used in this story */
	final Parser parser;

	/** the presenter used in this story */
	final Presenter presenter;

	/**
	 * y-axis grouping interval, such that events from different
	 * loggers/sources 'x' can be understood as happening "nearly
	 * at the same time" if their time stamps are not more than this
	 * value apart (the distance value takes, therefore, the same
	 * units as are used for 'y' axis in the original log file);
	 * the events that fall within this distance are ideally to be
	 * displayed at the same position on the 'y' axis of the 2D/flattish
	 * view
	 *
	 * this parameter is relevant to \e this.parser only
	 */
	final long yTimeStep;

	/** wrap messages after this number of characters; note that the Event messages can be multi-line */
	public int msgWrap = 50;


	/** the main worker: reads the whole input and feeds the presenter in the correct order (see Presenter) */
	public void process()
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
		//
		//over all sources 'x', construct relevant pairs (x,y),
		//and insert-sort them according to 'y' into this list
		class MyPair {
			public MyPair(final String _x, final long _y) { x = _x; y = _y; }
			public final String x;
			public final long y;
		};
		LinkedList<MyPair> permutation = new LinkedList<>();

		for (String x : logs.keySet())
		{
			//the pair (x,y) to be inserted
			final long y = logs.get(x).firstKey();

			//search "one-behind" the proper list element
			final ListIterator<MyPair> i = permutation.listIterator();
			boolean keepGoing = true;
			while (i.hasNext() && keepGoing) keepGoing = (i.next().y <= y);

			//move one back at the proper "index"
			if (!keepGoing) i.previous();
			i.add(new MyPair(x,y));
		}

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
		for (TreeMap<Long,Event> xLog : logs.values())
		{
			int currentMarker = 0;
			long orderWithinMarker = 0;

			for (Long y : xLog.keySet())
			{
				//convert "time stamp" y to a marker index m
				final int m = (int)((y-yMin) / yTimeStep);
				if (m == currentMarker)
				{
					//still in the same marker as before
					++orderWithinMarker;
				}
				else
				{
					//entered a new marker:
					//any further event shall not enter currentMarker anymore,
					//nor any of the previous markers... adjust the stats then
					if (orderWithinMarker > yMarkers[currentMarker])
						yMarkers[currentMarker] = orderWithinMarker;

					//and "enter" a new marker
					currentMarker = m;
					orderWithinMarker = 1;
				}
			}

			//update stats of the last marker too
			if (orderWithinMarker > yMarkers[currentMarker])
				yMarkers[currentMarker] = orderWithinMarker;
		}

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
		//on adjusted interval of 'y' (row) coordinates that will come
		presenter.initialize(logs.size(), 0,yMarkers[yMarkers.length-1]+currentLength-1, msgWrap,msgMaxLines);

		//iterate logged data in the correct order
		for (MyPair mp : permutation)
		{
			int currentMarker = 0;
			long orderWithinMarker = 0;

			TreeMap<Long,Event> xLog = logs.get(mp.x);
			//NB: Tree guarantees the 'y' values are accessed in the correct order
			for (Long y : xLog.keySet())
			{
				//convert "time stamp" y to a marker index m
				final int m = (int)((y-yMin) / yTimeStep);
				if (m == currentMarker)
				{
					//still in the same marker as before
					++orderWithinMarker;
				}
				else
				{
					//entered a new marker
					currentMarker = m;
					orderWithinMarker = 1;
				}

				final Event e = xLog.get(y);
				//convert "time stamp" y to a "row coordinate"
				e.y = yMarkers[currentMarker] + orderWithinMarker -1;

				presenter.show(e);
			}
		}

		presenter.close();
	}
}

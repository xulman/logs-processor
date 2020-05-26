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
import java.util.LinkedList;
import java.util.ListIterator;

import de.mpicbg.ulman.inputParsers.Parser;
import de.mpicbg.ulman.outputPresenters.Presenter;


public class ShowLogs
{
	/** no default/easy construction allowed */
	@SuppressWarnings("unused")
	private ShowLogs()
	{
		parser = null; presenter = null;
	}

	/** this is the main constructor; cannot switch parser/presenter during operation */
	public ShowLogs(final Parser _pa, final Presenter _pr)
	{
		parser = _pa; presenter = _pr;
	}

	/** the parser used in this story */
	final Parser parser;

	/** the presenter used in this story */
	final Presenter presenter;

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
		}
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


		//finally, start "presenting" the stored logs
		//on adjusted interval of 'y' (row) coordinates that will come
		presenter.initialize(logs.size(), yMin,yMax, msgWrap,msgMaxLines);

		//iterate logged data in the correct order
		for (MyPair mp : permutation)
		{
			TreeMap<Long,Event> xLog = logs.get(mp.x);
			//NB: Tree guarantees the 'y' values are accessed in the correct order
			for (Long y : xLog.keySet()) presenter.show(xLog.get(y));
		}

		presenter.close();
	}
}

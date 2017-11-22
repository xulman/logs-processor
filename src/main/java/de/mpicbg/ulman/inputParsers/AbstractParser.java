/*
 * CC BY-SA 4.0
 *
 * The code is licensed with "Attribution-ShareAlike 4.0 International license".
 * See the license details:
 *     https://creativecommons.org/licenses/by-sa/4.0/
 *
 * Copyright (C) 2017 Vladimír Ulman
 */
package de.mpicbg.ulman.inputParsers;

import de.mpicbg.ulman.Event;

/**
 * this class implements a minimal log readers
 * that provides readNextXYMsg() that returns
 * single fixed 'x', increasing values of 'y'
 * and some fake 'msg' with every call
 */
public class AbstractParser implements Parser
{
	///last extracted event
	protected Event currentEvent
		= new Event("wrong source",new Long(0),"should not see this one");

	/**
	 * a cached status of the input log "stream";
	 * this attribute should be updated after every read
	 * of the input "stream", that is in readNextXYMsg()
	 */
	protected boolean isThereNext = true;

	@Override
	public
	boolean hasNext()
	{ return isThereNext; }

	@Override
	public
	Event next()
	{ readNextXYMsg(); return get(); }

	@Override
	public
	Event get()
	{ return currentEvent; }

	/**
	 * this essentially an "abstract" (fake) log generator;
	 * it should update this.isThereNext (which, in this
	 * case, goes to 'false' after 20 issued events)
	 */
	protected
	void readNextXYMsg()
	{
		//basically a test event from 4 sources
		currentEvent.x = "source "+(counter % 4);
		currentEvent.y = 100 - (++counter);
		currentEvent.msg = "msg no #"+counter;

		if (counter == 20) isThereNext = false;
	}

	///internal counter of how many log events have been created so far
	private long counter = 0;
}

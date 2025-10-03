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
	 *
	 * It is assumed that any new parser would extend this class
	 * and override readNextXYMsg() to feed \e this.currentEvent,
	 * the overridden readNextXYMsg() MUST update \e this.isThereNext
	 * accordingly.
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
	{ return new Event(currentEvent); }

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
		currentEvent.msg.clear();
		currentEvent.msg.add("msg no #"+counter);

		if (counter == 20) isThereNext = false;
	}

	///internal counter of how many log events have been created so far
	private long counter = 0;

	@Override
	public
	long getTypicalTimeResolution()
	{ return 1; }
}

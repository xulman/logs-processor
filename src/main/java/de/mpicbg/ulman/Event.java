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

import java.util.Vector;

/**
 * description of one single logged event 'msg' already with the
 * necessary 2D annotations 'x' and 'y', see Presenter for explanation.
 *
 * The Event is essentially a "point" in the 2D/flattish view of the logs.
 */
public class Event
{
	public String x;
	public long   y;
	public Vector<String> msg;

	///a new-event constructor
	public Event(final String _x, final Long _y, final String _msg)
	{
		x = (_x != null)? _x : "empty";
		y = (_y != null)? _y : 0;
		msg = new Vector<>(20);

		//add first "line" of the message if there is some
		if (_msg != null)
			msg.add(_msg);
	}

	///a deep-copy constructor
	public Event(final Event e)
	{
		this.x = new String(e.x);
		this.y = e.y;
		this.msg = new Vector<>(e.msg);
		//NB: Vector's constructor creates a deep copy (in this case) -- good
	}

	@Override
	public String toString()
	{
		String str = msg.firstElement();
		if (str.length() > 50) str = str.substring(0,50);

		return "Event of >" +x+ "< at " +y+ ": " + str;
	}
}

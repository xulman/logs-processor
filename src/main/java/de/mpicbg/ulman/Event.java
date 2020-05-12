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

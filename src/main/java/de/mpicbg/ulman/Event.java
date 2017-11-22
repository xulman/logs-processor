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


/**
 * description of one single logged event 'msg' already with the
 * necessary 2D annotations 'x' and 'y', see Presenter for explanation.
 */
public class Event
{
	public final String x;
	public final Long   y;
	public final String msg;

	Event(final String _x, final Long _y, final String _msg)
	{
		x = (_x != null)? _x : "empty";
		y = (_y != null)? _y : 0;
		msg = (_msg != null)? _msg : "noMsg";
	}
}

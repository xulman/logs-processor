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
		//TODO: should test here for sensibility of the input data!

		x = _x;
		y = _y;
		msg = _msg;
	}
}

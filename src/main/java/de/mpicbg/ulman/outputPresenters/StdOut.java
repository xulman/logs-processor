/*
 * CC BY-SA 4.0
 *
 * The code is licensed with "Attribution-ShareAlike 4.0 International license".
 * See the license details:
 *     https://creativecommons.org/licenses/by-sa/4.0/
 *
 * Copyright (C) 2017 Vladimír Ulman
 */
package de.mpicbg.ulman.outputPresenters;

/**
 * this class just prints coordinates and messages on the standard output
 */
public class StdOut implements Presenter
{
	@Override
	public
	void initialize(final long xColumns,
	                final long yMin,
	                final long yMax,
	                final long msgMaxLines)
	{
		System.out.println("INIT: Cols: "+xColumns+", "
			+yMin+" <= y <= "+yMax+", msgMaxLines: "+msgMaxLines);
	}

	@Override
	public
	void show(final String x, final long y, final String msg)
	{
		System.out.println("MSG at: "+x+","+y+": "+msg);
	}

	@Override
	public
	void close()
	{
		System.out.println("CLOSE:");
	}
}

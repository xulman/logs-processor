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
 * interface that knows how to display the (x,y,msg) input triples
 *
 * the triples are fed in the guaranteed order: all from one 'x' is shipped
 * before any other 'x' is provided (since 'x' are considered labels, not
 * values/coordinates on the x-axis, no ordering is assumed here); within
 * the same 'x' the order of incoming triples is dictated by a natural ordering
 * of the 'y' values (from the smallest to the largest).
 */
public interface Presenter
{
	///lay out the 2D playground given the provided hints
	void initialize(final long xColumns,
	                final long yMin,
	                final long yMax,
	                final long msgMaxLines);

	///insert a "point" into the 2D world
	void show(final String x, final long y, final String msg);

	///to some final clean ups, e.g., annotation of axes
	void close();
}

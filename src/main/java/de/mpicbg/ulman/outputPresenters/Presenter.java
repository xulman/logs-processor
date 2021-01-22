/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2017, Vladimír Ulman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.mpicbg.ulman.outputPresenters;

import de.mpicbg.ulman.Event;

/**
 * interface that knows how to display the (x,y,msg) input triples
 *
 * the triples are fed in the guaranteed order: all from one 'x' is shipped
 * before any other 'x' is provided (since 'x' are considered labels, not
 * values/coordinates on the x-axis, no ordering is assumed here -- instead,
 * the order established from user of this class is used); within the same 'x'
 * the order of incoming triples is dictated by a natural ordering of the
 * 'y' values (from the smallest to the largest) - this order must be enforced
 * outside, the interface only draws stuff relying on this order
 */
public interface Presenter
{
	///lay out the 2D playground given the provided hints
	void initialize(final long xColumns,
	                final long yMin,
	                final long yMax,
	                final long msgWidthChars,
	                final long msgMaxLines);

	///insert a "point" into the 2D world
	void show(final Event e);

	///to some final clean ups, e.g., annotation of axes
	void close();
}

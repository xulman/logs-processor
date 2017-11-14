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
 * a 'mining interface' that provides a basic operational schema
 * hasNext(), next(), get(),   readNextXYMsg()
 */
public interface Parser
{
	///extracts and parses next event from the input log
	void readNextXYMsg();

	///indicates if there is some extractable event waiting in the input log
	boolean hasNext();

	///assumes hasNext() = true; it calls readNextXYMsg() and returns the extracted data
	Event next();
	//{ readNextXYMsg(); return get(); }

	///returns the last extracted data
	Event get();
}

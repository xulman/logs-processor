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
package de.mpicbg.ulman;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import org.scijava.widget.FileWidget;
import org.scijava.ItemVisibility;
import net.imagej.ImageJ;

import java.io.File;
import java.io.IOException;

import de.mpicbg.ulman.inputParsers.*;
import de.mpicbg.ulman.outputPresenters.*;

@Plugin(type = Command.class, menuPath = "Window>2D Log Composer",
        name = "Ulman_logger", headless = true,
		  description = "Processes multiple logs and displays them in a unified single view.")
public class loggerFrontend implements Command
{
	//------------- GUI stuff -------------
	//
	@Parameter
	private LogService log = null;

	//information....
	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false, required = false)
	private final String infoMsg
		= "Processes log from multiple loggers and display it in a unified single flattish/2D view.";

	@Parameter(label = "Path to input log file:",
		columns = 40, style = FileWidget.OPEN_STYLE,
		description = "The log file content should be understandable by the choosen log parser.")
	public File inLogFile;

	@Parameter(label = "Time resolution for grouping (-1 for default):",
		columns = 10, min = "-1",
		description = "How close in the time two logged events has to be, to be displayed at the same temporal level (y-axis) in the output.")
	public long timeResolution = -1;

	@Parameter(label = "Input log parser:",
		choices = {"Testing parser",
		           "DAIS parser",
		           "MitoGen parser"})
	public String inputParser;

	@Parameter(label = "Output log presenter:",
		choices = {"Simple console writer",
		           "Fiji table",
		           "HTML file",
		           "HTMLwithHeaders file"})
	public String outputPresenter;

	@Parameter(label = "Path to output file:",
		columns = 40, style = FileWidget.SAVE_STYLE,
		description = "Some log presenters will write to this file, others will just ignore it.")
	public File outputFile;


	//the GUI path entry function:
	@Override
	public void run()
	{
		try {
			System.out.println("read in file : "+inLogFile);
			System.out.println("parser       : "+inputParser);
			System.out.println("presenter    : "+outputPresenter);
			System.out.println("write to file: "+outputFile);
			System.out.println("time res in  : "+timeResolution);

			//initialize the appropriate parser and presenter
			Parser pa = null;
			Presenter pr = null;
			try {
				if (inputParser.startsWith("DAIS"))
					pa = new DAISwp13(inLogFile);
				else
				if (inputParser.startsWith("MitoGen"))
					pa = new MitoGen(inLogFile);
				else
					//default (debugging) parser
					pa = new AbstractParser();

				if (outputPresenter.startsWith("Fiji ta"))
					pr = new FijiTable(log != null? new ImageJ(log.context()) : null);
				else
				if (outputPresenter.startsWith("HTML "))
					pr = new HTML(outputFile,50);
				else
				if (outputPresenter.startsWith("HTMLw"))
					pr = new HTMLwithHeaders(outputFile,50);
				else
					//default (debugging) presenter
					pr = new AbstractPresenter();
			}
			catch (IOException e) {
				 System.err.format("IOException: %s%n", e);
			}

			//if default time res is requested, determine it...
			if (timeResolution == -1) timeResolution = pa.getTypicalTimeResolution();
			System.out.println("time res used: "+timeResolution);

			//create the loggerBackend, and use it
			ShowLogsCompacted lb = new ShowLogsCompacted(pa,pr,timeResolution);

			//don't wrap lines for the HTML outputs
			if (outputPresenter.startsWith("HTML")) lb.msgWrap = 200;
			//else, possibly adjustable from the GUI anyways
			else lb.msgWrap = 50;

			lb.process();
		}
		catch (RuntimeException e) {
			if (log != null) log.error("problem: "+e.getMessage());
		}
		catch (Exception e) {
			if (log != null) log.error("error: "+e.getMessage());
		}
	}


	//------------- command line stuff -------------
	//
	//the CLI path entry function:
	public static void main(final String... args)
	{
		/*
		//check the input parameters
		if (args.length != 2)
		{
			System.out.println("Incorrect number of parameters, expecting exactly two parameters.");
			System.out.println("Parameters: GTpath RESpath\n");
			System.out.println("GTpath should contain folder TRA and files: TRA/man_track???.tif and TRA/man_track.txt");
			System.out.println("RESpath should contain result files directly: mask???.tif and res_track.txt");
			System.out.println("Certain data format is assumed, please see\n"
				+"http://www.celltrackingchallenge.net/submission-of-results.html");
			return;
		}
		*/

		//create a new instance of ourselves
		final loggerFrontend miniMe = new loggerFrontend();

		//parse and store the arguments, if necessary
		miniMe.inLogFile = new File("/tmp/dais_log.txt");
		miniMe.inputParser = "DAIS parser";
		miniMe.timeResolution = -1;
		miniMe.outputPresenter = "HTMLw presenter";
		miniMe.outputFile = new File("/tmp/dais_log.html");

		miniMe.run();
	}
}

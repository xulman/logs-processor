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

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import org.scijava.widget.FileWidget;
import org.scijava.ItemVisibility;

import java.io.File;
import java.io.IOException;

import de.mpicbg.ulman.inputParsers.*;
import de.mpicbg.ulman.outputPresenters.*;


@Plugin(type = Command.class, menuPath = "Ulman>logger",
        name = "Ulman_logger", headless = true,
		  description = "Processes multiple logs and displays them in a unified single view.")
public class loggerFrontend implements Command
{
	//------------- GUI stuff -------------
	//
	@Parameter
	private LogService log = null;

	//information....
	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false, required = false, label = ":")
	private final String infoMsg
		= "some information?";

	@Parameter(label = "Path to input log file:",
		columns = 40, style = FileWidget.OPEN_STYLE,
		description = "The log file content should be understandable by the choosen log parser.")
	public File inLogFile;

	@Parameter(label = "Time resolution for grouping (-1 for default):",
		columns = 10,
		description = "How close in the time two logged events has to be, to be displayed at the same temporal level (y-axis) in the output.")
	public long timeResolution = -1;

	@Parameter(label = "Input log parser:",
		choices = {"Testing parser",
		           "DAIS parser",
		           "MitoGen parser"})
	public String inputParser;

	@Parameter(label = "Output log presenter:",
		choices = {"Simple console writer",
		           "HTML file",
		           "HTMLwithHeaders file",
		           "SVG image"})
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

				if (outputPresenter.startsWith("HTML "))
					pr = new HTML(outputFile,50);
				else
				if (outputPresenter.startsWith("HTMLw"))
					pr = new HTMLwithHeaders(outputFile,50);
				else
				if (outputPresenter.startsWith("SVG"))
					pr = null; //new SVG(outputFile);
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
			loggerBackend lb = new loggerBackend(pa,pr,timeResolution);

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

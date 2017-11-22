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

import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;

import org.scijava.widget.FileWidget;
import java.io.File;


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
		= "Cell tracking accuracy measurement based on comparison of acyclic";

	@Parameter(label = "Path to input log file:",
		columns = 40, style = FileWidget.OPEN_STYLE,
		description = "The log file content should be understandable by the choosen log parser.")
	public File inLogFile;

	@Parameter(label = "Input log parser:",
		choices = {"Simple parser",
		           "MitoGen parser"})
	public String inputParser;

	@Parameter(label = "Output log presenter:",
		choices = {"One writer per one file",
		           "One SVG image"})
	public String outputPresenter;


	//the GUI path entry function:
	@Override
	public void run()
	{
		try {
			System.out.println("read in file: "+inLogFile);
			System.out.println("parser      : "+inputParser);
			System.out.println("presenter   : "+outputPresenter);

			//should init the appropriate parsers and presenters
			//create the loggerBackend
			//and call it with the parser and presenter
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
		miniMe.inLogFile = new File("no file chosen");
		miniMe.inputParser = "Simple parser";
		miniMe.outputPresenter = "One writer per one file";

		miniMe.run();
	}
}

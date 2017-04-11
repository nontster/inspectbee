package co.nontster.perftest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliUtils {

	public static final Option S_OPTION = buildOneArgOption("s", "source name; name of probe source, e.g. ais-wifi, ais-fiber or ais-mobile", "String", true, "source");
	public static final Option U_OPTION = buildOneArgOption("u", "username for target web", "username", true, "username");
	public static final Option P_OPTION = buildOneArgOption("p", "password for target web", "password", true, "password");
	public static final Option ES_DISABLED = new Option("des", "disable elasticsearch store");
	public static final Option EU_OPTION = buildOneArgOption("eu", "username for elasticsearch", "username", false, "e_username");
	public static final Option EP_OPTION = buildOneArgOption("ep", "password for elasticsearch", "password", false, "e_password");	
	public static final Option HS_OPTION = buildOneArgOption("hs", "directory to store HAR files", "directory", false, "hsdir");
	private static final Option HELP_OPTION = new Option("help", "print usage");

	public static Option buildOneArgOption(String name, String description, String argName, boolean required,
			String longOpt) {
		return Option.builder(name).argName(argName).desc(description).longOpt(longOpt).hasArg(true).required(required)
				.build();
	}
	
	public static CommandLine parseAndHelp(String appName, Options options, String[] args) {
	    options.addOption(HELP_OPTION);	    
	    options.addOption(HS_OPTION);
	    options.addOption(S_OPTION);
	    options.addOption(U_OPTION);
	    options.addOption(P_OPTION);
	    options.addOption(ES_DISABLED);
	    options.addOption(EU_OPTION);
	    options.addOption(EP_OPTION);
	    
	    CommandLineParser parser = new DefaultParser();
	    CommandLine line = null;
	    
		try {
			line = parser.parse(options, args);
		} catch (MissingOptionException e) {
			new HelpFormatter().printHelp(appName, options, true);
			throw new IllegalArgumentException();
		} catch (MissingArgumentException e) {
			new HelpFormatter().printHelp(appName, options, true);
			throw new IllegalArgumentException();
		} catch (ParseException e) {
			System.err.println("Unexpected Exception: " + e);
			throw new IllegalArgumentException();
		}

		if (line.hasOption("help")) {
			new HelpFormatter().printHelp(appName, options, true);
			throw new IllegalArgumentException();
		}

	    return line;
	  }
}

package de.fraunhofer.iem;

import org.apache.commons.cli.*;

import java.io.File;

/**
 * Utility class for the Command Line argument parser
 *
 * @author Ranjith Krishnamurthy
 */
public class CommandLineOptionsUtility {
    /**
     * Below are the command line arguments options short and long names
     */
    protected static final String CLASS_PATH_SHORT = "scp";
    protected static final String CLASS_PATH_LONG = "suite-class-path";
    protected static final String OUTPUT_ROOT_DIR_SHORT = "od";
    protected static final String OUTPUT_ROOT_DIR_LONG = "out-dir";
    protected static final String CLASS_LIST_SHORT = "cl";
    protected static final String CLASS_LIST_LONG = "class-list";
    protected static final String BOOMERANG_PRE_TRANSFORMER_SHORT = "bpt";
    protected static final String BOOMERANG_PRE_TRANSFORMER_LONG = "boomerang-pre-transformer";
    protected static final String REPLACE_OLD_JIMPLE_SHORT = "rej";
    protected static final String REPLACE_OLD_JIMPLE_LONG = "replace-existing-jimple";

    /**
     * Initializes the command line options.
     * <p>
     * Note: In future, if needed to add new options, then add it here.
     *
     * @return Command line options
     */
    private Options initializeCommandLineOptions() {
        Options cmdOptions = new Options();

        Option classPathOption = new Option(
                CLASS_PATH_SHORT,
                CLASS_PATH_LONG,
                true,
                "Classpath containing the Java bytecode");
        classPathOption.setRequired(true);

        Option outDir = new Option(
                OUTPUT_ROOT_DIR_SHORT,
                OUTPUT_ROOT_DIR_LONG,
                true,
                "Jimple output root directory");
        outDir.setRequired(true);

        Option classList = new Option(
                CLASS_LIST_SHORT,
                CLASS_LIST_LONG,
                true,
                "List of classes to generate the Jimple code. If this option is not set, then it generates for " +
                        "all the classes available in the given classpath");
        classList.setRequired(false);

        Option bPT = new Option(
                BOOMERANG_PRE_TRANSFORMER_SHORT,
                BOOMERANG_PRE_TRANSFORMER_LONG,
                false,
                "Apply Boomerang pre-transformer");
        classList.setRequired(false);

        Option rEJ = new Option(
                REPLACE_OLD_JIMPLE_SHORT,
                REPLACE_OLD_JIMPLE_LONG,
                false,
                "Replace the existing Jimple code if present.");
        classList.setRequired(false);

        cmdOptions.addOption(classPathOption);
        cmdOptions.addOption(classList);
        cmdOptions.addOption(outDir);
        cmdOptions.addOption(bPT);
        cmdOptions.addOption(rEJ);

        return cmdOptions;
    }

    /**
     * Parses the given raw command line arguments and returns the parsed CommandLine
     *
     * @param args Raw command line arguments
     * @return parsed CommandLine
     */
    protected CommandLine parseCommandArguments(String[] args) {
        // Initialize the command line options
        Options cmdOptions = initializeCommandLineOptions();

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        CommandLine commandLine = null;

        try {
            // Parse the command line arguments
            commandLine = commandLineParser.parse(cmdOptions, args);
        } catch (ParseException ex) {
            helpFormatter.printHelp("JimpleProvider", cmdOptions);
            System.exit(-1);
        }

        // Check for validness of the classPath
        checkClassPath(commandLine.getOptionValue(CLASS_PATH_SHORT)
        );

        // Check for validness of the output root directory
        checkOutDir(commandLine.getOptionValue(OUTPUT_ROOT_DIR_SHORT)
        );


        return commandLine;
    }

    /**
     * Check for the validness of the given classpath.
     *
     * @param classPath Classpath
     */
    private void checkClassPath(String classPath) {
        if (!FilesUtils.isValidPath(classPath)) {
            System.err.println("Given classpath is not valid!!!");
            System.exit(-1);
        }

        File file = new File(classPath);

        if (file.exists() && !file.isDirectory()) {
            System.err.println("Given classpath is not a directory!!!");
            System.exit(-1);
        }
    }

    /**
     * Check for the validness of the given output root directory.
     *
     * @param outDir Output root directory
     */
    private void checkOutDir(String outDir) {
        if (!FilesUtils.isValidPath(outDir)) {
            System.err.println("Given output root directory is not valid!!!");
            System.exit(-1);
        }

        File file = new File(outDir);

        if (file.exists() && !file.isDirectory()) {
            System.err.println("Given output root directory is not a directory!!!");
            System.exit(-1);
        }
    }

    /**
     * Prints the stacktrace and exit the program
     *
     * @param e Exception
     */
    protected static void printStackTraceAndExit(Exception e) {
        System.err.println("Something went wrong!\nStacktrace: \n");
        e.printStackTrace();
        System.exit(-1);
    }
}

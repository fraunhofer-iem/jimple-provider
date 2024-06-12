package de.fraunhofer.iem;

import org.apache.commons.cli.CommandLine;
import de.fraunhofer.iem.FilesUtils;
import de.fraunhofer.iem.JimpleProvider;
import de.fraunhofer.iem.PreTransformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class of the JimpleProvider tool
 *
 * @author Ranjith Krishnamurthy
 */
public class Main {
    /**
     * Main method
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        String appClassPath;
        PreTransformer preTransformer;
        String outDir;
        boolean isReplaceOldJimple;
        FilesUtils filesUtils = new FilesUtils();

        CommandLine commandLine = new CommandLineOptionsUtility().parseCommandArguments(args);

        // Store for the app class path
        appClassPath = commandLine.getOptionValue(CommandLineOptionsUtility.CLASS_PATH_SHORT);

        // Store for the complete app classes
        List<String> completeAppClasses = new ArrayList<>();

        try {
            completeAppClasses.addAll(filesUtils.getClassesAsList(commandLine.getOptionValue(CommandLineOptionsUtility.CLASS_PATH_SHORT)));
        } catch (IOException ioException) {
            System.err.println("There was an exception!\n " + ioException.getMessage());
            System.exit(-1);
        }

        // Store for the pre-transformer options
        if (commandLine.hasOption(CommandLineOptionsUtility.BOOMERANG_PRE_TRANSFORMER_SHORT)) {
            preTransformer = PreTransformer.BOOMERANG;
        } else {
            preTransformer = PreTransformer.NONE;
        }

        // Store REPLACE_OLD_JIMPLE
        isReplaceOldJimple = commandLine.hasOption(CommandLineOptionsUtility.REPLACE_OLD_JIMPLE_SHORT);

        // Check for the app class list
        List<String> appClasses = new ArrayList<>();

        if (commandLine.hasOption(CommandLineOptionsUtility.CLASS_LIST_SHORT)) {
            String appClassesAsString = commandLine.getOptionValue(CommandLineOptionsUtility.CLASS_LIST_SHORT);

            appClasses.addAll(Arrays.asList(appClassesAsString.split(":")));

            // Check if the given classes are valid or not
            appClasses.forEach(s -> {
                if (!completeAppClasses.contains(s)) {
                    System.err.println(s + " is not present in the given class path!");
                    System.exit(-1);
                }
            });
        } else {
            appClasses.addAll(completeAppClasses);
        }

        // Store the output directory
        outDir = commandLine.getOptionValue(CommandLineOptionsUtility.OUTPUT_ROOT_DIR_SHORT);

        JimpleProvider jimpleProvider = new JimpleProvider(
                appClassPath,
                appClasses,
                preTransformer,
                outDir,
                isReplaceOldJimple
        );

        System.out.println("***********************************");
        System.out.println("App Classpath   \t:   " + appClassPath);
        System.out.println("Output directory\t:   " + outDir);
        System.out.println("Pre-Transformer \t:   " + preTransformer);
        System.out.println("Class list      \t:   " + appClasses);
        System.out.println("***********************************");

        try {
            jimpleProvider.generate();
        } catch (IOException ioException) {
            System.err.println("There was an exception!\n " + ioException.getMessage());
        }
    }
}

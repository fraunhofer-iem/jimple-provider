package de.fraunhofer.iem;

import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
        val filesUtils = new FilesUtils();

        val commandLine = new CommandLineOptionsUtility().parseCommandArguments(args);

        // Store for the app class path
        val appClassPath = commandLine.getOptionValue(CommandLineOptionsUtility.CLASS_PATH_SHORT);

        // Store for the pre-transformer options
        PreTransformer preTransformer;
        if (commandLine.hasOption(CommandLineOptionsUtility.BOOMERANG_PRE_TRANSFORMER_SHORT)) {
            preTransformer = PreTransformer.BOOMERANG;
        } else {
            preTransformer = PreTransformer.NONE;
        }

        // Store REPLACE_OLD_JIMPLE
        boolean isReplaceOldJimple;
        isReplaceOldJimple = commandLine.hasOption(CommandLineOptionsUtility.REPLACE_OLD_JIMPLE_SHORT);

        // Check for the app class list
        val appClasses = new ArrayList<String>();

        if (commandLine.hasOption(CommandLineOptionsUtility.CLASS_LIST_SHORT)) {
            val appClassesAsString = commandLine.getOptionValue(CommandLineOptionsUtility.CLASS_LIST_SHORT);

            appClasses.addAll(Arrays.asList(appClassesAsString.split(":")));

            // Check if the given classes are valid or not
            appClasses.forEach(s -> {
                try {
                    if (!filesUtils.getClassesAsList(appClassPath).contains(s)) {
                        System.err.println(s + " is not present in the given class path!");
                        System.exit(-1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Store the output directory
        val outDir = commandLine.getOptionValue(CommandLineOptionsUtility.OUTPUT_ROOT_DIR_SHORT);

        val jimpleProvider = new JimpleProvider(
                appClassPath,
                preTransformer
        );

        System.out.println("***********************************");
        System.out.println("App Classpath   \t:   " + appClassPath);
        System.out.println("Output directory\t:   " + outDir);
        System.out.println("Pre-Transformer \t:   " + preTransformer);
        System.out.println("Class list      \t:   " + appClasses);
        System.out.println("***********************************");

        try {
            if (!appClasses.isEmpty()) {
                jimpleProvider.generate(appClasses, outDir, isReplaceOldJimple);
            } else {
                jimpleProvider.generate(outDir, isReplaceOldJimple);
            }
        } catch (IOException ioException) {
            System.err.println("There was an exception!\n " + ioException.getMessage());
        }
    }
}

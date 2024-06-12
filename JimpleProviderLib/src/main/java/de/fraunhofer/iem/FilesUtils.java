package de.fraunhofer.iem;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility for File operations
 *
 * @author Ranjith Krishnamurthy
 */
public class FilesUtils {
    /**
     * Checks whether the given string can be a valid path or not
     *
     * @param path Path
     * @return Valid path or not
     */
    protected boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }

        return true;
    }

    /**
     * Process the given app path that contains the classes and returns the list of classes.
     *
     * @param appClassesPath App class path that contains the classes to be converted into Jimple
     * @return List of classes name
     */
    protected List<String> getClassesAsList(String appClassesPath) throws IOException {
        Path path = Paths.get(appClassesPath);

        List<String> appClasses = new ArrayList<>();

        try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (filePath, fileAttr) ->
                filePath.toString().endsWith(".class") && fileAttr.isRegularFile())
        ) {
            stream.forEach(p -> appClasses.add(p.toString()
                    .replace(path.toString(), "")
                    .replaceAll("\\\\", ".")
                    .replaceAll("/", ".")
                    .replaceAll("^\\.", "")
                    .replaceAll("\\.class$", "")));
        }

        return appClasses;
    }

    /**
     * This method recursively creates a directory for the given class name in the given baseDir
     *
     * @param baseDir   base directory
     * @param className Class name
     * @return True if successful otherwise false
     */
    protected boolean recursivelyCreateDirectory(String baseDir, String className) {
        ArrayList<String> stringArray = new ArrayList<>(Arrays.asList(className.split("\\.")));
        stringArray.remove(stringArray.size() - 1);

        StringBuilder completePath = new StringBuilder(baseDir);

        for (String str : stringArray) {
            completePath.append(File.separator).append(str);
        }

        File completePathFile = new File(completePath.toString());

        if (!completePathFile.exists())
            return completePathFile.mkdirs();

        return true;
    }

    /**
     * Deletes the given directory
     * @param outDir Directory to be deleted
     * @throws IOException If fails to delete the directory
     */
    protected void deleteDirectory(File outDir) throws IOException {
        if (outDir.isDirectory() && outDir.exists()) {
            FileUtils.deleteDirectory(outDir);
        } else if (!outDir.isDirectory() && outDir.exists()) {
            if (!outDir.delete()) {
                throw new IOException("Given out directory is not a directory and not able to delete it.");
            }
        }
    }

    /**
     * Flushes the given string to the given file
     *
     * @param file File
     * @param string String to be flushed to the given file
     * @throws FileNotFoundException If the given file does not exist
     */
    protected void flushStringToFile(File file, String string) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        writer.println(string);
        writer.flush();
        writer.close();
    }
}

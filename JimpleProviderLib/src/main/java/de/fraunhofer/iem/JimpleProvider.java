package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
import org.json.JSONObject;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the Jimple code and its metrics
 *
 * @author Ranjith Krishnamurthy
 */
public class JimpleProvider {
    private final String appClassPath;
    private final List<String> appClasses = new ArrayList<>();
    private final PreTransformer preTransformer;
    private final String outDirectory;
    private final boolean isReplaceOldJimple;
    private final static SootUtils sootUtils = new SootUtils();
    private final static FilesUtils filesUtils = new FilesUtils();

    /**
     * Constructor for JimpleProvider
     *
     * @param appClassPath       App classpath
     * @param appClasses         List of App classes
     * @param preTransformer     PreTransformer
     * @param outDirectory       Output directory
     * @param isReplaceOldJimple Replace the existing Jimple code or not
     */
    public JimpleProvider(String appClassPath, List<String> appClasses, PreTransformer preTransformer,
                         String outDirectory, boolean isReplaceOldJimple) {
        this.appClassPath = appClassPath;
        this.appClasses.addAll(appClasses);
        this.preTransformer = preTransformer;
        this.outDirectory = outDirectory;
        this.isReplaceOldJimple = isReplaceOldJimple;
    }

    /**
     * Generates the Jimple files and respective metrics file
     */
    public void generate() throws IOException {
        preTasks();

        // Generates the output file and generate Jimple
        File outDir = new File(outDirectory);

        if (isReplaceOldJimple) {
            filesUtils.deleteDirectory(outDir);
        }

        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Could not create " + outDir.getAbsolutePath());
        }

        for (String appClass : appClasses) {
            String outFileAsString = outDir.getAbsolutePath() + File.separator + appClass.replace(".", File.separator) + ".jimple";
            String metricFileAsString = outDir.getAbsolutePath() + File.separator + appClass.replace(".", File.separator) + ".json";

            File outFile = new File(outFileAsString);
            File metricFile = new File(metricFileAsString);

            if (!isReplaceOldJimple && outFile.exists()) {
                continue;
            }

            System.out.println("Generating Jimple for\t:   " + appClass);
            SootClass sootClass = Scene.v().getSootClass(appClass);


            if (isReplaceOldJimple && outFile.exists()) {
                if (!outFile.delete()) {
                    throw new IOException("File " + outFile.getAbsolutePath() + " already exist and could not delete it.");
                }
            }

            if (metricFile.exists()) {
                if (!metricFile.delete()) {
                    throw new IOException("File " + metricFile.getAbsolutePath() + " already exist and could not delete it.");
                }
            }

            if (!filesUtils.recursivelyCreateDirectory(outDir.getAbsolutePath(), appClass)) {
                throw new IOException("Could not create directory for " + outFileAsString);
            }

            if (!outFile.createNewFile()) {
                throw new IOException("Could not create file " + outFileAsString);
            }

            sootUtils.flushSootClassToFile(outFile, sootClass);

            JSONObject metric = JimpleMetricsGenerator.generateMetric(sootClass);
            filesUtils.flushStringToFile(metricFile, metric.toString(4));
        }

        postTasks();
    }

    public List<String> getAllInvokedMethodSignature(String appClass, String method) {
        preTasks();

        SootClass sootClass = Scene.v().getSootClass(appClass);
        List<String> allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, method);

        postTasks();

        return allInvokedMethodSignatures;
    }

    /**
     * Pre tasks such as initializing soot, applying the pre-transformer
     */
    private void preTasks() {
        sootUtils.initializeSoot(appClassPath, appClasses);

        // Set the pre-transformer
        // TODO: In future, if needed to extend the more transformer add the functionality here
        if (preTransformer == PreTransformer.BOOMERANG) {
            sootUtils.applyBoomerangTransformer();
        }
    }

    /**
     * Post tasks: resetting the pre-transformer
     */
    private void postTasks() {
        // Reset the pre-transformer
        // TODO: If needed, reset the pre-transformer for future extensions
        if (preTransformer == PreTransformer.BOOMERANG) {
            BoomerangPretransformer.v().reset();
        }
    }
}

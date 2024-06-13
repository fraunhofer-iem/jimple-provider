package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
import lombok.val;
import soot.Scene;
import soot.SootClass;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Generates the Jimple code and its metrics
 *
 * @author Ranjith Krishnamurthy
 */
public class JimpleProvider {
    private final String appClassPath;
    private final PreTransformer preTransformer;
    private final static SootUtils sootUtils = new SootUtils();
    private final static FilesUtils filesUtils = new FilesUtils();

    /**
     * Constructor for JimpleProvider
     *
     * @param appClassPath       App classpath
     * @param preTransformer     PreTransformer
     */
    public JimpleProvider(String appClassPath, PreTransformer preTransformer) {
        this.appClassPath = appClassPath;
        this.preTransformer = preTransformer;
    }

    /**
     * Generates the Jimple files and respective metrics file
     *
     * @param appClasses List of App classes
     * @param outDirectory Output directory
     * @param isReplaceOldJimple Replace the existing Jimple code or not
     *
     * @throws IOException If there is some problem with accessing the class files
     */
    public void generate(List<String> appClasses, String outDirectory, boolean isReplaceOldJimple) throws IOException {
        preTasks(appClasses);

        // Generates the output file and generate Jimple
        val outDir = new File(outDirectory);

        if (isReplaceOldJimple) {
            filesUtils.deleteDirectory(outDir);
        }

        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Could not create " + outDir.getAbsolutePath());
        }

        for (String appClass : appClasses) {
            val outFileAsString = outDir.getAbsolutePath() + File.separator + appClass.replace(".", File.separator) + ".jimple";
            val metricFileAsString = outDir.getAbsolutePath() + File.separator + appClass.replace(".", File.separator) + ".json";

            val outFile = new File(outFileAsString);
            val metricFile = new File(metricFileAsString);

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

            val metric = JimpleMetricsGenerator.generateMetric(sootClass);
            filesUtils.flushStringToFile(metricFile, metric.toString(4));
        }

        postTasks();
    }

    /**
     * Generates the Jimple files and respective metrics file
     *
     * @param outDirectory Output directory
     * @param isReplaceOldJimple Replace the existing Jimple code or not
     *
     * @throws IOException If there is some problem with accessing the class files
     */
    public void generate(String outDirectory, boolean isReplaceOldJimple) throws IOException {
        generate(filesUtils.getClassesAsList(appClassPath), outDirectory, isReplaceOldJimple);
    }

    public List<InvokeExpressionToLineNumber> getAllInvokedMethodSignature(String appClass, String method) throws IOException {
        preTasks(filesUtils.getClassesAsList(appClassPath));

        val sootClass = Scene.v().getSootClass(appClass);
        val allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, method);

        postTasks();

        return allInvokedMethodSignatures;
    }

    /**
     * Pre tasks such as initializing soot, applying the pre-transformer
     *
     * @param appClasses List of App classes
     */
    private void preTasks(List<String> appClasses) {
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

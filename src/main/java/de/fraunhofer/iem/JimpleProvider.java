package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Constructor for JimpleProvider
     *
     * @param appClassPath       App classpath
     * @param appClasses         List of App classes
     * @param preTransformer     PreTransformer
     * @param outDirectory       Output directory
     * @param isReplaceOldJimple Replace the existing Jimple code or not
     */
    public JimpleProvider(String appClassPath,
                         List<String> appClasses,
                         PreTransformer preTransformer,
                         String outDirectory,
                         boolean isReplaceOldJimple) {
        this.appClassPath = appClassPath;
        this.appClasses.addAll(appClasses);
        this.preTransformer = preTransformer;
        this.outDirectory = outDirectory;
        this.isReplaceOldJimple = isReplaceOldJimple;
    }

    /**
     * Initializes the soot
     */
    private void initializeSoot() {
        G.reset();
        Options.v().set_keep_line_number(true);
        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(appClassPath);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        Options.v().setPhaseOption("jb", "use-original-names:true");
        //Options.v().setPhaseOption("jb.lns", "enabled:false");
        Options.v().set_output_format(Options.output_format_none);

        List<SootMethod> entries = new ArrayList<SootMethod>();
        for (String appClass : appClasses) {
            SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
    }

    /**
     * Generates the Jimple and respective metrics
     */
    public void generate() {
        initializeSoot();

        // Set the pre-transformer
        // TODO: In future, if needed to extend the more transformer add the functionality here
        if (preTransformer == PreTransformer.BOOMERANG) {
            Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
            PackManager.v().getPack("wjtp").add(transform);
            PackManager.v().getPack("cg").apply();

            BoomerangPretransformer.v().apply();
            PackManager.v().getPack("wjtp").apply();
        }

        // Generates the output file and generate Jimple
        File outDir = new File(outDirectory);

        if (isReplaceOldJimple) {
            if (outDir.isDirectory() && outDir.exists()) {
                try {
                    FileUtils.deleteDirectory(outDir);
                } catch (IOException e) {
                    CommandLineOptionsUtility.printStackTraceAndExit(e);
                }
            } else if (!outDir.isDirectory() && outDir.exists()) {
                if (!outDir.delete()) {
                    System.err.println("Given out directory is not a directory and not able to delete it.");
                    System.exit(-1);
                }

            }
        }

        if (!outDir.exists() && !outDir.mkdirs()) {
            System.err.println("Could not create " + outDir.getAbsolutePath());
            System.exit(-1);
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
                    System.err.println("File " + outFile.getAbsolutePath() + " already exist and could not delete it.");
                    System.exit(-1);
                }
            }

            if (metricFile.exists()) {
                if (!metricFile.delete()) {
                    System.err.println("File " + metricFile.getAbsolutePath() + " already exist and could not delete it.");
                    System.exit(-1);
                }
            }

            if (!FilesUtils.recursivelyCreateDirectory(outDir.getAbsolutePath(), appClass)) {
                System.err.println("Could not create directory for " + outFileAsString);
                System.exit(-1);
            }

            try {
                if (!outFile.createNewFile()) {
                    System.err.println("Could not create file " + outFileAsString);
                    System.exit(-1);
                }
            } catch (IOException e) {
                CommandLineOptionsUtility.printStackTraceAndExit(e);
            }

            PrintWriter writer;
            try {
                writer = new PrintWriter(outFile);
                soot.Printer.v().printTo(sootClass, writer);
                writer.flush();
                writer.close();

                JSONObject metric = JimpleMetricsGenerator.generateMetric(sootClass);

                writer = new PrintWriter(metricFile);
                writer.println(metric.toString(4));
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                CommandLineOptionsUtility.printStackTraceAndExit(e);
            }
        }

        // Reset the pre-transformer
        // TODO: If needed, reset the pre-transformer for future extensions
        if (preTransformer == PreTransformer.BOOMERANG) {
            BoomerangPretransformer.v().reset();
        }
    }

    /**
     * Creates the dummy analysis transformer
     *
     * @return SceneTransformer
     */
    private SceneTransformer createAnalysisTransformer() {
        return new SceneTransformer() {
            protected void internalTransform(String phaseName, Map options) {
            }
        };
    }
}

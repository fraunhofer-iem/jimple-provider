package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
import lombok.val;
import soot.*;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private final List<String> appClasses;

    /**
     * Constructor for JimpleProvider
     *
     * @param appClassPath       App classpath
     * @param preTransformer     PreTransformer
     */
    private JimpleProvider(String appClassPath, PreTransformer preTransformer, List<String> appClasses) {
        this.appClassPath = appClassPath;
        this.preTransformer = preTransformer;
        this.appClasses = appClasses;
    }

    public static JimpleProvider getInstance(String appClassPath, PreTransformer preTransformer, List<String> appClasses) {
        return new JimpleProvider(appClassPath, preTransformer, appClasses);
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
        generate(appClasses, outDirectory, isReplaceOldJimple);
    }

    public List<InvokeExpressionToLineNumber> getAllInvokedMethodSignature(String appClass, String method) {
        val sootClass = Scene.v().getSootClass(appClass);

        val allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, method);

        return allInvokedMethodSignatures;
    }

    public List<SootClass> getAllApplicationClasses() {
        val appClassesAsSootClass = new ArrayList<SootClass>();

        for (val appClass : appClasses) {
            appClassesAsSootClass.add(
                    Scene.v().getSootClass(appClass)
            );
        }

        return appClassesAsSootClass;
    }

    private int getLastLineNumber(Chain<Unit> units, Unit unit) {
        if (unit.getJavaSourceStartLineNumber() != -1)
            return unit.getJavaSourceStartLineNumber();

        return getLastLineNumber(units, units.getPredOf(unit));
    }

    private static int getLastLineNumberOfMethod(SootMethod method) {
        if (!method.hasActiveBody()) {
            method.retrieveActiveBody();
        }

        Body body = method.getActiveBody();
        int lastLineNumber = -1;

        for (Unit unit : body.getUnits()) {
            LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
            if (tag != null) {
                int line = tag.getLineNumber();
                if (line > lastLineNumber) {
                    lastLineNumber = line;
                }
            }
        }

        return lastLineNumber;
    }

    public String findMethodContainingLineNumber(String fileName, int lineNumber) {
        val className = fileName
                .replace("src/main/java/", "")
                .replace("src\\main\\java\\", "")
                .replace("/", ".")
                .replace("\\", ".")
                .replace(".java", "");

        val sc = Scene.v().forceResolve(className, SootClass.BODIES);

        // Iterate over each method in the class
        for (SootMethod method : sc.getMethods()) {
            // Check if the method has an active body (non-abstract method)
            try {
                Body body = method.retrieveActiveBody();
                Chain<Unit> units = body.getUnits();

                val firstUnitLineNumber = method.getJavaSourceStartLineNumber();
                val lastUnitLineNumber = getLastLineNumberOfMethod(method); //getLastLineNumber(units, units.getLast());

                if (lineNumber >= firstUnitLineNumber && lineNumber <= lastUnitLineNumber)
                    return method.getSignature();
            } catch (RuntimeException ex) {
                System.err.println("Could not get active body: " + method);
                return null;
            }
        }

        return null; // No method contains the given line number
    }

    public HashMap<String, List<InvokeExpressionToLineNumber>> getAllInvokedMethodUsages(String rootPackageName) {
        HashMap<String, List<InvokeExpressionToLineNumber>> usages = new HashMap<>();

        for (val appClass : appClasses) {
            val sootClass = Scene.v().getSootClass(appClass);

            if (sootClass.getPackageName().startsWith(rootPackageName)) {
                for (val sootMethod : sootClass.getMethods()) {
                    val allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, sootMethod.getSignature());

                    for (val invokeExpr : allInvokedMethodSignatures) {
                        if (usages.containsKey(invokeExpr.invokedMethodSignature)) {
                            usages.get(invokeExpr.invokedMethodSignature).add(invokeExpr);
                        } else {
                            usages.put(invokeExpr.invokedMethodSignature, new ArrayList<>(Collections.singletonList(invokeExpr)));
                        }
                    }
                }
            }
        }

        return usages;
    }

    public Set<String> getAllInvokedMethodSignature(String rootPackageName) {
        val allMethodSignature = new HashSet<String>();

        for (val appClass : appClasses) {
            val sootClass = Scene.v().getSootClass(appClass);

            if (sootClass.getPackageName().startsWith(rootPackageName)) {
                for (val sootMethod : sootClass.getMethods()) {
                    val allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, sootMethod.getSignature());

                    allInvokedMethodSignatures.stream()
                            .map(InvokeExpressionToLineNumber::getInvokedMethodSignature)
                            .forEach(allMethodSignature::add);
                }
            }
        }

        return allMethodSignature;
    }

    public Set<String> getAllMethodSignature() {
        val allMethodSignature = new HashSet<String>();

        for (val appClass : appClasses) {
            val sootClass = Scene.v().getSootClass(appClass);

            for (val sootMethod : sootClass.getMethods()) {
                allMethodSignature.add(sootMethod.getSignature());

                val allInvokedMethodSignatures = sootUtils.getAllInvokedMethodSignatures(sootClass, sootMethod.getSignature());

                allInvokedMethodSignatures.stream()
                        .map(InvokeExpressionToLineNumber::getInvokedMethodSignature)
                        .forEach(allMethodSignature::add);
            }
        }

        return allMethodSignature;
    }

    /**
     * Pre tasks such as initializing soot, applying the pre-transformer
     *
     */
    public void preTasks(boolean isMinimalSoot) {
        if (isMinimalSoot) {
            System.out.println("Minimal Soot");
            sootUtils.initializeMinimalSoot(appClassPath, appClasses);
        } else {
            System.out.println("Full Soot");
            sootUtils.initializeSoot(appClassPath, appClasses);
        }

        // Set the pre-transformer
        // TODO: In future, if needed to extend the more transformer add the functionality here
        if (preTransformer == PreTransformer.BOOMERANG) {
            sootUtils.applyBoomerangTransformer();
        }
    }

    /**
     * Post tasks: resetting the pre-transformer
     */
    public void postTasks() {
        // Reset the pre-transformer
        // TODO: If needed, reset the pre-transformer for future extensions
        if (preTransformer == PreTransformer.BOOMERANG) {
            BoomerangPretransformer.v().reset();
        }
    }
}

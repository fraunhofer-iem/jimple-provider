package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
import lombok.val;
import soot.*;
import soot.jimple.Stmt;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class SootUtils {
    /**
     * Initializes the soot
     */
    protected void initializeSoot(String appClassPath, List<String> appClasses) {
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

        val entries = new ArrayList<SootMethod>();
        for (val appClass : appClasses) {
            val sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
    }

    /**
     * Applies the Boomerang pre-transformer to the soot instance
     */
    protected void applyBoomerangTransformer() {
        val transform = new Transform("wjtp.ifds", createAnalysisTransformer());
        PackManager.v().getPack("wjtp").add(transform);
        PackManager.v().getPack("cg").apply();

        BoomerangPretransformer.v().apply();
        PackManager.v().getPack("wjtp").apply();
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

    /**
     * Flushes the provided SootClass to the provided file
     *
     * @param outFile Output file
     * @param sootClass SootClass
     * @throws FileNotFoundException If the provided file does not exist
     */
    protected void flushSootClassToFile(File outFile, SootClass sootClass) throws FileNotFoundException {
        val writer = new PrintWriter(outFile);
        soot.Printer.v().printTo(sootClass, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Returns all the invoke-expression's method signature in the given method
     *
     * @param sootMethod Soot method
     * @return Returns List of invoke-expression's method signature in the given method
     */
    protected List<InvokeExpressionToLineNumber> getAllInvokedMethodSignatures(SootMethod sootMethod) {
        val invokeExpressionSignatures = new ArrayList<InvokeExpressionToLineNumber>();

        Body body;

        try {
            body = sootMethod.retrieveActiveBody();
        } catch (RuntimeException ex) {
            System.err.println("Could not get active body: " + sootMethod);
            return Collections.emptyList();
        }

        for (val unit : body.getUnits()) {
            val stmt = (Stmt) unit;

            if (stmt.containsInvokeExpr()) {
                // Invoke expression list
                invokeExpressionSignatures.add(
                        new InvokeExpressionToLineNumber(
                                stmt.getInvokeExpr().getMethod().getSignature(),
                                stmt.getJavaSourceStartLineNumber()
                        )
                );
            }
        }

        return invokeExpressionSignatures;
    }

    /**
     * Returns all the invoke-expression's method signature in the given method signature and the Soot class
     *
     * @param sootClass Soot class
     * @param method Method signature
     * @return Returns List of invoke-expression's method signature in the given method signature and the Soot class
     */
    protected List<InvokeExpressionToLineNumber> getAllInvokedMethodSignatures(SootClass sootClass, String method) {
        val methodSignature = new StringBuilder();

        if (!method.startsWith("<")) {
            methodSignature.append("<");
        }

        methodSignature.append(method);

        if (!method.endsWith(">")) {
            methodSignature.append(">");
        }

        for (val sootMethod : sootClass.getMethods()) {
            if (sootMethod.getSignature().contentEquals(methodSignature) || sootMethod.getSubSignature().equals(method)) {
                return getAllInvokedMethodSignatures(sootMethod);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Check if the variable is a stack variable
     * @param local Local
     * @return Returns true if the given Local is a stack variable
     */
    private boolean isStackVariable(Local local) {
        return local.getName().matches("^\\$.*") || local.getName().matches("^l\\d.*");
    }

    /**
     * Returns all the stack variable in the given Soot method
     *
     * @param sootMethod Soot method
     * @return Map of stack variables in the given Soot method and its type
     */
    protected Map<String, String> getStackVariablesIn(SootMethod sootMethod) {
        val stackVariables = new LinkedHashMap<String, String>();

        for (val local : sootMethod.retrieveActiveBody().getLocals()) {
            if (isStackVariable(local)) {
                // Stack variables list
                stackVariables.put(local.getName(), local.getType().toString());
            }
        }

        return stackVariables;
    }

    /**
     * Returns all the local variable in the given Soot method
     *
     * @param sootMethod Soot method
     * @return Map of local variables in the given Soot method and its type
     */
    protected Map<String, String> getLocalVariablesIn(SootMethod sootMethod) {
        val localVariables = new LinkedHashMap<String, String>();

        for (val local : sootMethod.retrieveActiveBody().getLocals()) {
            if (!isStackVariable(local)) {
                // Stack variables list
                localVariables.put(local.getName(), local.getType().toString());
            }
        }

        return localVariables;
    }

    /**
     * Returns all the implemented interface by the given Soot class
     *
     * @param sootClass Soot class
     * @return List of implemented interface by the given Soot class
     */
    protected List<String> getImplementedInterfacesBy(SootClass sootClass) {
        return sootClass.getInterfaces()
                .stream()
                .map(SootClass::getName)
                .collect(Collectors.toList());
    }
}

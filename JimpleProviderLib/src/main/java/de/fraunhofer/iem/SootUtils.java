package de.fraunhofer.iem;

import boomerang.scene.jimple.BoomerangPretransformer;
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
     * Applies the Boomerang pre-transformer to the soot instance
     */
    protected void applyBoomerangTransformer() {
        Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
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
        PrintWriter writer = new PrintWriter(outFile);
        soot.Printer.v().printTo(sootClass, writer);
        writer.flush();
        writer.close();
    }

    protected List<String> getAllInvokedMethodSignatures(SootMethod sootMethod) {
        List<String> invokeExpressionSignatures = new ArrayList<>();

        for (Unit unit : sootMethod.retrieveActiveBody().getUnits()) {
            Stmt stmt = (Stmt) unit;

            if (stmt.containsInvokeExpr()) {
                // Invoke expression list
                invokeExpressionSignatures.add(stmt.getInvokeExpr().getMethod().getSignature());
            }
        }

        return invokeExpressionSignatures;
    }

    protected List<String> getAllInvokedMethodSignatures(SootClass sootClass, String method) {
        for (SootMethod sootMethod : sootClass.getMethods()) {
            if (sootMethod.getSignature().equals(method) || sootMethod.getSubSignature().equals(method)) {
                return getAllInvokedMethodSignatures(sootMethod);
            }
        }

        return Collections.emptyList();
    }

    private boolean isStackVariable(Local local) {
        return local.getName().matches("^\\$.*") || local.getName().matches("^l\\d.*");
    }

    protected Map<String, String> getStackVariablesIn(SootMethod sootMethod) {
        Map<String, String> stackVariables = new LinkedHashMap<>();

        for (Local local : sootMethod.retrieveActiveBody().getLocals()) {
            if (isStackVariable(local)) {
                // Stack variables list
                stackVariables.put(local.getName(), local.getType().toString());
            }
        }

        return stackVariables;
    }

    protected Map<String, String> getLocalVariablesIn(SootMethod sootMethod) {
        Map<String, String> localVariables = new LinkedHashMap<>();

        for (Local local : sootMethod.retrieveActiveBody().getLocals()) {
            if (!isStackVariable(local)) {
                // Stack variables list
                localVariables.put(local.getName(), local.getType().toString());
            }
        }

        return localVariables;
    }

    protected List<String> getImplementedInterfacesBy(SootClass sootClass) {
        return sootClass.getInterfaces()
                .stream()
                .map(SootClass::getName)
                .collect(Collectors.toList());
    }
}

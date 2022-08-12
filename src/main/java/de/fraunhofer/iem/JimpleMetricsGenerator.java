package de.fraunhofer.iem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * Generates the Metrics for the given class
 *
 * @author Ranjith Krishnamurthy
 */
public class JimpleMetricsGenerator {
    /**
     * Overrides the JSONObject and change the field from HashMap to LinkedHashMap to maintain the insertion order.
     *
     * @return JSONObject with LinkedHashMap
     */
    private static JSONObject getJSONObject() {
        return new JSONObject() {
            @Override
            public JSONObject put(String key, Object value) throws JSONException {
                try {
                    Field jsonObjectMapField = JSONObject.class.getDeclaredField("map");
                    jsonObjectMapField.setAccessible(true);

                    Object jsonObjectMap = jsonObjectMapField.get(this);

                    // Do it only the first time otherwise it clears the map everytime
                    if (!(jsonObjectMap instanceof LinkedHashMap)) {
                        jsonObjectMapField.set(this, new LinkedHashMap<>());
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    System.err.println("Could not make JSON Object map as LinkedHashMap: ");
                    e.printStackTrace();
                    System.exit(-1);
                }

                return super.put(key, value);
            }
        };
    }

    /**
     * Generates the metrics and returns the JSONObject
     *
     * @param sootClass Soot Class
     * @return JSONObject metrics
     */
    public static JSONObject generateMetric(SootClass sootClass) {
        JSONObject jsonObject = getJSONObject();

        // 1. className
        jsonObject.put("className", sootClass.getName());

        // 2. SuperClass
        jsonObject.put("superClass", sootClass.getSuperclass().getName());

        // 3. Interface implements
        JSONArray implementedInterface = new JSONArray();

        for (SootClass interfaceBody : sootClass.getInterfaces()) {
            implementedInterface.put(interfaceBody.getName());
        }

        jsonObject.put("implementedInterface", implementedInterface);

        // 4. methodCount
        jsonObject.put("methodCount", sootClass.getMethodCount());

        JSONArray methodsSignature = new JSONArray();
        JSONObject methodsInformation = getJSONObject();

        for (SootMethod sootMethod : sootClass.getMethods()) {
            // Method signature list
            methodsSignature.put(sootMethod.getSignature());

            JSONObject methodInfo = getJSONObject();

            JSONObject localVariables = getJSONObject();
            JSONObject stackVariable = getJSONObject();
            JSONArray invokeExpression = new JSONArray();

            if (sootMethod.hasActiveBody()) {
                for (Local local : sootMethod.retrieveActiveBody().getLocals()) {
                    if (local.getName().matches("^\\$.*") || local.getName().matches("^l\\d.*")) {
                        // Stack variables list
                        stackVariable.put(local.getName(), local.getType().toString());
                    } else {
                        // Local variables list
                        localVariables.put(local.getName(), local.getType().toString());
                    }
                }


                for (Unit unit : sootMethod.retrieveActiveBody().getUnits()) {
                    Stmt stmt = (Stmt) unit;

                    if (stmt.containsInvokeExpr()) {
                        // Invoke expression list
                        invokeExpression.put(stmt.getInvokeExpr().getMethod().getSignature());
                    }
                }
            }

            methodInfo.put("localVariables", localVariables);
            methodInfo.put("stackVariables", stackVariable);
            methodInfo.put("invokeExpressions", invokeExpression);

            // Method information
            methodsInformation.put(sootMethod.getSubSignature(), methodInfo);
        }

        // 3. methodsSignature
        jsonObject.put("methodsSignature", methodsSignature);

        // 4. methodsInformation
        jsonObject.put("methodsInformation", methodsInformation);

        return jsonObject;
    }
}

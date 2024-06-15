package de.fraunhofer.iem;

import lombok.SneakyThrows;
import lombok.val;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import soot.SootClass;

import java.util.LinkedHashMap;

/**
 * Generates the Metrics for the given class
 *
 * @author Ranjith Krishnamurthy
 */
public class JimpleMetricsGenerator {
    private static final SootUtils sootUtils = new SootUtils();

    /**
     * Overrides the JSONObject and change the field from HashMap to LinkedHashMap to maintain the insertion order.
     *
     * @return JSONObject with LinkedHashMap
     */
    private static JSONObject getJSONObject() {
        return new JSONObject() {
            @SneakyThrows
            @Override
            public JSONObject put(String key, Object value) throws JSONException {
                val jsonObjectMapField = JSONObject.class.getDeclaredField("map");
                jsonObjectMapField.setAccessible(true);

                val jsonObjectMap = jsonObjectMapField.get(this);

                // Do it only the first time otherwise it clears the map everytime
                if (!(jsonObjectMap instanceof LinkedHashMap)) {
                    jsonObjectMapField.set(this, new LinkedHashMap<>());
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
        val jsonObject = getJSONObject();

        // 1. className
        jsonObject.put("className", sootClass.getName());

        // 2. SuperClass
        jsonObject.put("superClass", sootClass.getSuperclass().getName());

        // 3. Interface implements
        val implementedInterface = new JSONArray();
        sootUtils.getImplementedInterfacesBy(sootClass).forEach(implementedInterface::put);

        jsonObject.put("implementedInterface", implementedInterface);

        // 4. methodCount
        jsonObject.put("methodCount", sootClass.getMethodCount());

        val methodsSignature = new JSONArray();
        val methodsInformation = getJSONObject();

        for (val sootMethod : sootClass.getMethods()) {
            // Method signature list
            methodsSignature.put(sootMethod.getSignature());

            val methodInfo = getJSONObject();

            val localVariables = getJSONObject();
            val stackVariable = getJSONObject();
            val invokeExpression = new JSONArray();

            if (sootMethod.hasActiveBody()) {
                sootUtils.getStackVariablesIn(sootMethod).forEach(stackVariable::put);
                sootUtils.getLocalVariablesIn(sootMethod).forEach(localVariables::put);
                sootUtils.getAllInvokedMethodSignatures(sootMethod).stream().map(InvokeExpressionToLineNumber::getInvokedMethodSignature).forEach(invokeExpression::put);
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

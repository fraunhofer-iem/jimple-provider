package iem;

import de.fraunhofer.iem.JimpleProvider;
import de.fraunhofer.iem.PreTransformer;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestRetrieveInvokeMethodSignature {
    private String classPath;

    @Before
    public void setUp() {
        URL url = TestRetrieveInvokeMethodSignature.class.getClassLoader().getResource("dummy_project/classes");
        assert url != null;
        classPath = url.getPath();
    }

    @Test
    public void test() throws IOException {
        JimpleProvider jimpleProvider = new JimpleProvider(classPath, PreTransformer.NONE);
        val invokedMethods = jimpleProvider.getAllInvokedMethodSignature(
                "de.fraunhofer.iem.App", "de.fraunhofer.iem.App: void main(java.lang.String[])");


        assertEquals(3, invokedMethods.size());

        for (val invokedMethod : invokedMethods) {
            switch (invokedMethod.getInvokedMethodSignature()) {
                case "<de.fraunhofer.iem.App: de.fraunhofer.iem.HelloPrinter getPrinter()>":
                    assertEquals(13, invokedMethod.getLineNumber());
                    break;
                case "<de.fraunhofer.iem.HelloPrinter: void printHelloWorld(java.lang.String)>":
                    assertEquals(19, invokedMethod.getLineNumber());
                    break;
                case "<java.lang.System: void exit(int)>":
                    assertEquals(16, invokedMethod.getLineNumber());
                    break;
                default:
                    fail("Incorrect invoked method signature: " + invokedMethod.getInvokedMethodSignature());
            }
        }
    }
}

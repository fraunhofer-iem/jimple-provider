package iem;

import de.fraunhofer.iem.JimpleProvider;
import de.fraunhofer.iem.PreTransformer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        List<String> invokedMethods = jimpleProvider.getAllInvokedMethodSignature(
                "de.fraunhofer.iem.App", "de.fraunhofer.iem.App: void main(java.lang.String[])");

        assertEquals("[<de.fraunhofer.iem.App: de.fraunhofer.iem.HelloPrinter getPrinter()>," +
                " <java.lang.System: void exit(int)>," +
                " <de.fraunhofer.iem.HelloPrinter: void printHelloWorld(java.lang.String)>]",
                invokedMethods.toString());
    }
}

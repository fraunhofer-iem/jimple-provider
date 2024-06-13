package de.fraunhofer.iem;

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

    @Test
    public void test2() throws IOException {
        System.out.println(classPath);
        JimpleProvider jimpleProvider = new JimpleProvider(classPath, PreTransformer.NONE);
        val invokedMethods = jimpleProvider.getAllMethodSignature();

        assertEquals(21, invokedMethods.size());
        assertEquals("[<java.lang.Object: void <init>()>, " +
                "<java.lang.System: void exit(int)>, " +
                "<java.lang.String: boolean equals(java.lang.Object)>, " +
                "<de.fraunhofer.iem.App: void <init>()>, " +
                "<de.fraunhofer.iem.App: void main(java.lang.String[])>, " +
                "<java.util.Scanner: void <init>(java.io.InputStream)>, " +
                "<de.fraunhofer.iem.App: void <clinit>()>, " +
                "<java.lang.StringBuilder: void <init>()>, " +
                "<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>, " +
                "<de.fraunhofer.iem.App: de.fraunhofer.iem.HelloPrinter getPrinter()>, " +
                "<java.lang.String: int hashCode()>, " +
                "<de.fraunhofer.iem.HelloPrinterWithMessage: void <init>()>, " +
                "<java.lang.StringBuilder: java.lang.String toString()>, " +
                "<java.io.PrintStream: void println(java.lang.String)>, " +
                "<java.util.Scanner: java.lang.String nextLine()>, " +
                "<java.lang.String: java.lang.String[] split(java.lang.String)>, " +
                "<de.fraunhofer.iem.HelloPrinterWithMessage: void printHelloWorld(java.lang.String)>, " +
                "<de.fraunhofer.iem.HelloPrinterWithName: void <init>()>, " +
                "<de.fraunhofer.iem.HelloPrinter: void printHelloWorld(java.lang.String)>, " +
                "<java.lang.String: java.lang.String toLowerCase()>, " +
                "<de.fraunhofer.iem.HelloPrinterWithName: void printHelloWorld(java.lang.String)>]", invokedMethods.toString());
    }
}

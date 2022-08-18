# What is JimpleProvider?
A tool that uses Soot to generate the Jimple code for the Java bytecode available in the given 
classpath along with the metadata related to taint analysis.

# How to build JimpleProvider?
Clone the project
```.shell
git clone https://github.com/fraunhofer-iem/jimple-provider.git
```

Change the directory to the JimpleProvider project's root directory

````.shell
cd jimple-provider
````

Run the below command in the project root directory
```.shell
mvn clean install -DskipTests
```

# How to use JimpleProvider
Please use the generated JimpleProvider with dependencies jar.

Below are the command line options of the JimpleProvider. The arguments -scp
and the -od are the mandatory arguments for the tool.
```.shell
[ranjith@home:~]$ java -jar JimpleProvider-JW-1.0.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: JimpleProvider
 -bpt,--boomerang-pre-transformer   Apply Boomerang pre-transformer
 -cl,--class-list <arg>             List of classes to generate the Jimple
                                    code. If this option is not set, then
                                    it generates for all the classes
                                    available in the given classpath
 -od,--out-dir <arg>                Jimple output root directory
 -rej,--replace-existing-jimple     Replace the existing Jimple code if 
                                    present.
 -scp,--suite-class-path <arg>      Classpath containing the Java bytecode

```

To generate the soot Jimple code with out any pre-transformer
```.shell
java -jar JimpleProvider-JW-1.0.0-SNAPSHOT-jar-with-dependencies.jar -scp <classpath location> -od <output location>
```

To generate the soot Jimple code with Boomerang pre-transformer
```.shell
java -jar JimpleProvider-JW-1.0.0-SNAPSHOT-jar-with-dependencies.jar -scp <classpath location> -od <output location> -bpt
```

# Generated Metadata
An example of generated metadata is shown below.

```{.shell .numberLines}
{
  className: "de.fraunhofer.iem.basics.helloWorld.PrintHelloWithClass",
  superClass: "java.lang.Object",
  implementedInterface: [ ],
  methodCount: 2,
  methodsSignature: [
    "<de.fraunhofer.iem.basics.helloWorld.PrintHelloWithClass: void <init>()>", 
    "<de.fraunhofer.iem.basics.helloWorld.PrintHelloWithClass: void main(java.lang.String[])>"
  ],
  methodsInformation: {
    void <init>(): {
      localVariables: {
        this: "de.fraunhofer.iem.basics.helloWorld.PrintHelloWithClass"
      }, 
      stackVariables: { }, 
      invokeExpressions: ["<java.lang.Object: void <init>()>"]
    }, 
    void main(java.lang.String): {
      localVariables: {
        args: "java.lang.String[]"
      }, 
      stackVariables: {
        $stack1: "java.io.PrintStream"
      }, 
      invokeExpressions: ["<java.io.PrintStream: void println(java.lang.String)>"]
    }
  }
}
```

The generated metadata contains six properties on the root JSON object---“className”, “superClass”, “implementedInterface”, “methodCount”, “methodsSignature”, and “methodsInformation”.

- **className:** This property contains the fully qualified class name to which the metadata belongs. For example, in Listing 4.2 at Line 2, this property has the fully qualified class name of TwoInts. This property helps to quickly understand how a fully qualified name for a typical class or even a class for a top-level member appears in the Java bytecode.
- **superClass:** This property contains the superclass of the class to which the metadata belongs. For example, in Line 3, this property has the value java.lang.Object—a default super class in the Java bytecode. This property helps understand which class is extended due to some Kotlin feature in the Java bytecode, which is not visible in the source code.
- **implementedInterface:** This property contains the list of all the interfaces implemented by the class to which the metadata belongs. For example, the class TwoInts implements no interface. Therefore, in Line 4, this property has an empty list. Similar to the “superClass”, this property helps understand which interfaces are implemented due to some Kotlin feature in the Java bytecode, which is not visible in the source code.
- **methodCount:** This property contains the number of methods present in the class to which the metadata belongs. For example, the class TwoInts has only one method. Therefore, in Line 5, this property has the value 1. With the help of this property, we can quickly check whether there are any additional methods in the Java bytecode compared to the source code.
- **methodsSignature:** This property contains the list of method signatures of all the methods present in the class to which the metadata belongs. For example, the class TwoInts has only one method, add. Therefore, in Lines 6-8, this property has the list of one element with the fully qualified method signature of the method add. With the help of this property, we can quickly identify how the fully qualified method signature of a method varies in the Java bytecode compared to the Kotlin source code.
- **methodsInformation:** This property is a JSON object that contains the additional information of all the methods present in the class to which the metadata belongs. The sub-signatures of these methods are the sub-properties for the property “methodsInformation”. For example, since there is only one method in the TwoInts class, this property contains only one subproperty with the sub signature of the method add (Lines 10-21). Each of these sub-properties contains three more sub-properties—“localVariables” (Line 11), “stackVariables” (Line 16), and “invokeExpressions” (Line 20).

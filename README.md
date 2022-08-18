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

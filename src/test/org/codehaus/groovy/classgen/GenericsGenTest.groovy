package org.codehaus.groovy.classgen

import org.codehaus.groovy.tools.*
import org.codehaus.groovy.control.*

class GenericsGenTest extends GroovyTestCase{

    void testCompile () {
        try {
            Class.forName("java.lang.annotation.Annotation");
        }
        catch(Exception ex) {
            return
        }


        File dir = createTempDir("groovy-src-", "-src")
        assertNotNull dir

        def fileList =  [
          "JavaClass.java" : """
         import java.util.*;

         public abstract class JavaClass<T> implements GroovyInterface<T> {
         }
    """,

         "JavaInterface.java" : """
         public interface JavaInterface<X,Y> {
            public X getKey ();
            public Y getValue ();
         }
    """,

          "GroovyInterface.groovy" : """
         interface GroovyInterface<X> {
            X method ();
         }
    """,

            "GroovyClass.groovy" : """
           class GroovyClass extends JavaClass<String> implements JavaInterface<Long,Boolean> {
              String method() {}
			  Long getKey(){}
              Boolean getValue(){}
           }
      """
                ].collect {
            name, text ->
              File file = new File(dir, name)
              file.write text
              file
        }

        CompilerConfiguration config = new CompilerConfiguration()
         config.targetDirectory = createTempDir("groovy-target-", "-target")
         config.jointCompilationOptions = [
			"stubDir" : createTempDir("groovy-stub-", "-stub"),
			"namedValues" : ["target","1.5","source","1.5"] as String[]
		 ]
         config.classpath = "target/classes"
        FileSystemCompiler compiler = new FileSystemCompiler(config)
        compiler.compile (fileList.toArray(new File [fileList.size()]) )
    }

    private File createTempDir (prefix, suffix) {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.delete();
        tempFile.mkdirs();
        tempFile.deleteOnExit()
        return tempFile;
    }
}

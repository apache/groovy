package org.codehaus.groovy.classgen

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.FileSystemCompiler

class InterfaceTest extends GroovyTestCase{

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
          "GClass.groovy" : """
         package test;

         class GClass {}
    """,

         "GInterface.groovy" : """
         package test;

         interface GInterface {
            GClass [] get ();
         }
    """,

          "JClass.java" : """
         package test;

         public class JClass implements GInterface {
            public GClass [] get () { return new GClass [0]; };
         }
    """,
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
//			"namedValues" : ["target","1.5","source","1.5"] as String[]
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

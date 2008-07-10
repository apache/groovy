package groovy.bugs;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

public class Groovy1567_Bug extends TestCase {
   public void testGroovyScriptEngineVsGroovyShell() throws IOException, ResourceException, ScriptException {
      // @TODO refactor this path	   
      File currentDir = new File("./src/test/groovy/bugs");	   
      String file = "bug1567_script.groovy";
	   
      Binding binding = new Binding();
      GroovyShell shell = new GroovyShell(binding);
      String[] test = null;
      Object result = shell.run( new File(currentDir,file), test );

      String[] roots = new String[] { currentDir.getAbsolutePath() };
      GroovyScriptEngine gse = new GroovyScriptEngine(roots);
      binding = new Binding();
      // a MME was ensued here stating no 't.start()' was available
      // in the script
      gse.run( file, binding );
   }
}

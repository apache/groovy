/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Represents a groovy shell capable of running arbitrary groovy scripts
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @version $Revision$
 */
public class GroovyShell extends GroovyObjectSupport {
    public static final String[] EMPTY_ARGS = {
    };
    
    private GroovyClassLoader loader;
    private Binding context;

    public static void main(String args[]) {
        int length = args.length;
        if (length <= 0) {
            System.out.println("Usage: Groovy groovyScript [arguments]");
            return;
        }
        String scriptName = args[0];
        String[] newArgs = new String[length - 1];
        if (length > 1) {
            System.arraycopy(args, 1, newArgs, 0, length - 1);
        }

        try {
            GroovyShell groovy = new GroovyShell();
            groovy.run(new File(scriptName), newArgs);
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public GroovyShell() {
        this(null, new Binding());
    }

    public GroovyShell(Binding binding) {
      this(null, binding);
    }
    
    public GroovyShell(ClassLoader parent, Binding binding) {
      if (parent == null) {
        parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) parent = GroovyShell.class.getClassLoader();
      }
      this.loader = new GroovyClassLoader(parent);
      this.context = binding;
    }

    /**
     * Creates a child shell using a new ClassLoader which uses the parent shell's 
     * class loader as its parent
     * 
     * @param shell is the parent shell used for the variable bindings and the parent class loader
     */
    public GroovyShell(GroovyShell shell) {
        this.loader = new GroovyClassLoader(shell.loader);
        this.context = shell.context;
    }

    public Binding getContext() {
        return context;
    }
    
    public Object getProperty(String property) {
        Object answer = getVariable(property);
        if (answer == null) {
            answer = super.getProperty(property);
        }
        return answer;
    }

    public void setProperty(String property, Object newValue) {
        setVariable(property, newValue);
        try {
            super.setProperty(property, newValue);
        }
        catch (GroovyRuntimeException e) {
            // ignore, was probably a dynamic property
        }
    }

    /**
     * A helper method which runs the given script file with the given command line arguments
     * 
     * @param scriptFile the file of the script to run
     * @param list the command line arguments to pass in
     */
    public void run(File scriptFile, List list) throws ClassNotFoundException, SyntaxException, IOException {
        String[] args = new String[list.size()];
        list.toArray(args);
        run(scriptFile, args);
    }

    
    /**
     * Runs the given script file name with the given command line arguments
     * 
     * @param scriptFile the file name of the script to run
     * @param args the command line arguments to pass in
     */
    public void run(File scriptFile, String[] args) throws ClassNotFoundException, SyntaxException, IOException {
      // Get the current context classloader and save it on the stack
      Thread thread = Thread.currentThread();
      ClassLoader currentClassLoader = thread.getContextClassLoader();
      thread.setContextClassLoader(loader);

      // Parse the script, generate the class, and invoke the main method.  This is a little looser than
      // if you are compiling the script because the JVM isn't executing the main method.
      Class scriptClass = loader.parseClass(scriptFile);
      InvokerHelper.invokeMethod(scriptClass, "main", new Object[] { args });

      // Set the context classloader back to what it was.
      thread.setContextClassLoader(currentClassLoader);
    }

    /**
     * Runs the given script text with command line arguments
     * 
     * @param scriptText is the text content of the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param args the command line arguments to pass in
     */
    public void run(String scriptText, String fileName, String[] args) throws ClassNotFoundException, SyntaxException, IOException {
        run(new ByteArrayInputStream(scriptText.getBytes()), fileName, args);
    }

    /**
     * Runs the given script with command line arguments
     * 
     * @param in the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param args the command line arguments to pass in
     */
    public Object run(InputStream in, String fileName, String[] args) throws ClassNotFoundException, SyntaxException, IOException {
        Class scriptClass = loader.parseClass(in, fileName);
        return InvokerHelper.invokeMethod(scriptClass, "main", new Object[] { args });
    }

    public Object getVariable(String name) {
        return context.getVariable(name);
    }
    
    public void setVariable(String name, Object value) {
        context.setVariable(name, value);
    }

    /**
     * Evaluates some script against the current ScriptContext and returns the result
     * 
     * @param scriptText the text of the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(String scriptText, String fileName) throws SyntaxException, ClassNotFoundException, IOException {
        return evaluate(new ByteArrayInputStream(scriptText.getBytes()), fileName);
    }

    /**
     * Evaluates some script against the current ScriptContext and returns the result
     * 
     * @param file is the file of the script (which is used to create the class name of the script)
     */
    public Object evaluate(File file) throws SyntaxException, ClassNotFoundException, IOException {
        return evaluate(new FileInputStream(file), file.getName());
    }

    /**
     * Evaluates some script against the current ScriptContext and returns the result
     *
     * @param scriptText the text of the script
     */
    public Object evaluate(String scriptText) throws SyntaxException, ClassNotFoundException, IOException {
        return evaluate(new ByteArrayInputStream(scriptText.getBytes()), "script" + System.currentTimeMillis() + ".groovy");
    }

    /**
     * Evaluates some script against the current ScriptContext and returns the result
     *
     * @param in the stream reading the script
     */
    public Object evaluate(InputStream in) throws SyntaxException, ClassNotFoundException, IOException {
        return evaluate(in, "script" + System.currentTimeMillis() + ".groovy");
    }

    /**
     * Evaluates some script against the current ScriptContext and returns the result
     * 
     * @param in the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(InputStream in, String fileName) throws SyntaxException, ClassNotFoundException, IOException {
        Class scriptClass = loader.parseClass(in, fileName);
        Script script = InvokerHelper.createScript(scriptClass, context);
        return script.run();
    }
}

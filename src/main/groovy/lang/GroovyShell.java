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
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.groovy.classgen.GroovyClassLoader;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Represents a groovy shell capable of running arbitrary groovy scripts
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyShell {
    public static final String[] EMPTY_ARGS = {
    };
    
    private GroovyClassLoader loader;

    public static void main(String args[]) {
        int length = args.length;
        if (length <= 0) {
            System.out.println("Usage: Groovy groovyScript [arguments]");
            return;
        }
        String script = args[0];
        String[] newArgs = new String[length - 1];
        if (length > 1) {
            System.arraycopy(args, 1, newArgs, 0, length - 1);
        }

        try {
            GroovyShell groovy = new GroovyShell();
            groovy.run(script, newArgs);
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public GroovyShell() {
        this(GroovyShell.class.getClassLoader());
    }

    public GroovyShell(ClassLoader parent) {
        loader = new GroovyClassLoader(parent);
    }

    /**
     * Runs the given script file name with the given command line arguments
     * 
     * @param scriptFile the file name of the script to run
     * @param args the command line arguments to pass in
     */
    public Object run(String scriptFile, String[] args) throws ClassNotFoundException, SyntaxException, IOException {
        Class scriptClass = loader.parseClass(scriptFile);
        return InvokerHelper.invokeMethod(scriptClass, "main", new Object[] { args });
    }

    /**
     * Runs the given script text with command line arguments
     * 
     * @param scriptText is the text content of the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param args the command line arguments to pass in
     */
    public Object run(String scriptText, String fileName, String[] args) throws ClassNotFoundException, SyntaxException, IOException {
        return run(new ByteArrayInputStream(scriptText.getBytes()), fileName, args);
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

    public void setVariable(String name, Object value) {
    }

    /**
     * Evaluates some script
     * 
     * @param in the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(String scriptText, String fileName) throws SyntaxException, ClassNotFoundException, IOException {
        return run(scriptText, fileName, EMPTY_ARGS);
    }
}

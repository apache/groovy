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
package groovy.ui;

import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A simple interactive shell for evaluating groovy expressions
 * on the command line
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InteractiveShell {

    GroovyShell shell = new GroovyShell();
    BufferedReader reader;

    public static void main(String args[]) {
        try {
            InteractiveShell groovy = new InteractiveShell();
            groovy.run(args);
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public InteractiveShell() {
    }

    public void run(String[] args) throws Exception {
        reader = new BufferedReader(new InputStreamReader(System.in));

        String version = InvokerHelper.getVersion();
        
        System.out.println("Lets get Groovy!");
        System.out.println("================");
        System.out.println("Version: " + version + " JVM: " + System.getProperty("java.vm.version"));
        System.out.println("Hit carriage return twice to execute a command");
        System.out.println("The command 'quit' will terminate the shell");
        
        int counter = 1;
        while (true) {
            String command = readCommand();
            if (command == null || command.equals("quit")) {
                break;
            }
            try {
                Object answer = shell.evaluate(command, "CommandLine" + counter++ +".groovy");
                System.out.println(InvokerHelper.inspect(answer));
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    protected String readCommand() throws IOException {
        StringBuffer buffer = new StringBuffer();
        while (true) {
            System.out.print("groovy> ");
            String line = reader.readLine();
            if (line != null) {
                buffer.append(line);
                buffer.append('\n');
            }
            if (line == null || line.trim().length() == 0) {
                break;
            }
        }
        return buffer.toString().trim();
    }
}

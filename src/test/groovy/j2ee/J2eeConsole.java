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

package groovy.j2ee;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * A J2EE console
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class J2eeConsole {

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Usage: home [configuaration] [localcopy]");
            return;
        }

        String home = args[0];

        Properties p = new Properties();
        System.setProperty("openejb.home", home);
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");
        p.put("openejb.home", home);

        if (args.length > 1) {
            String conf = args[1];
            System.setProperty("openejb.configuration", conf);
            p.put("openejb.configuration", conf);
        }
        if (args.length > 2) {
            String copy = args[2];
            System.setProperty("openejb.localcopy", copy);
            p.put("openejb.localcopy", copy);
        }
        try {
            InitialContext ctx = new InitialContext(p);

            GroovyShell shell = new GroovyShell();
            shell.setVariable("context", ctx);
            //shell.evaluate("src/test/groovy/j2ee/CreateData.groovy");

            //shell.evaluate("src/main/groovy/ui/Console.groovy");
            GroovyObject console = (GroovyObject) InvokerHelper.invokeConstructorOf("groovy.ui.Console", null);
            console.setProperty("shell", shell);
            console.invokeMethod("run", null);
            /*
            */
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
        }
    }
}

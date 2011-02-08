/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

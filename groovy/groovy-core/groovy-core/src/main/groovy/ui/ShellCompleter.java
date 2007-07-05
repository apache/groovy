/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.ui;

import groovy.lang.GroovyShell;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.sandbox.ui.Completer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Readline completion for InteractiveShell.
 *
 * @author Yuri Schimke
 * @version $Revision$
 */
public class ShellCompleter implements Completer {
    // The shell being handled
    private GroovyShell shell;
    private List completions = new ArrayList();

    public ShellCompleter(GroovyShell shell) {
        this.shell = shell;
    }

    // @TODO add optimisations like check for . and rule out variables etc
    public List findCompletions(String token) {
        completions.clear();

        if (token.length() == 0) {
            return completions;
        }

        // completions of local variable names
        findLocalVariables(token);

        // completions of local fields.

        // completions of local methods
        findShellMethods(token);

        // completions of methods invoked on a target
        //findTargetCompletions(complete);

        // completion of keywords.

        return completions;
    }

    private void findShellMethods(String complete) {
        List methods = shell.getMetaClass().getMetaMethods();
        for (Iterator i = methods.iterator(); i.hasNext();) {
            MetaMethod method = (MetaMethod) i.next();
            if (method.getName().startsWith(complete)) {
                if (method.getParameterTypes().length > 0) {
                    completions.add(method.getName() + "(");
                }
                else {
                    completions.add(method.getName() + "()");
                }
            }
        }
    }

    private void findLocalVariables(String complete) {
        Set names = shell.getContext().getVariables().keySet();

        for (Iterator i = names.iterator(); i.hasNext();) {
            String name = (String) i.next();
            if (name.startsWith(complete)) {
                completions.add(name);
            }
        }
    }
}

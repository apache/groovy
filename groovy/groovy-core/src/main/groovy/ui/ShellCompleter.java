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
import groovy.lang.MetaMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.sandbox.ui.Completer;

/**
 *  Readline completion for InteractiveShell.
 * 
 * @author Yuri Schimke
 * @version $Revision$
 */
public class ShellCompleter implements Completer {
  // The shell being handled
  private GroovyShell shell;
  private ArrayList completions;

  public ShellCompleter(GroovyShell shell) {
    this.shell = shell;
  }
  
  // @TODO add optimisations like check for . and rule out variables etc
  public List findCompletions(String token) {  
    completions.clear();
      
    if (token.length() == 0)
      return completions;
    
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
        if (method.getParameterTypes().length > 0)
          completions.add(method.getName() + "(");
        else
          completions.add(method.getName() + "()");
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

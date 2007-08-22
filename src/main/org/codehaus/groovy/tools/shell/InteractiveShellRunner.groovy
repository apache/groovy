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

package org.codehaus.groovy.tools.shell

import jline.ConsoleReader
import jline.MultiCompletor

/**
 * Support for running a {@link Shell} interactivly.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InteractiveShellRunner
    implements Runnable
{
    protected final ShellLog log = new ShellLog(this.class)
    
    final Shell shell
    
    final ConsoleReader reader
    
    Closure prompt = { '> ' }
    
    boolean breakOnNull = true
    
    InteractiveShellRunner(final Shell shell) {
        assert shell
        
        this.shell = shell
        
        this.reader = new ConsoleReader(shell.io.inputStream, shell.io.output)
        
        init()
    }
    
    protected void init() {
        def completors = []
        
        shell.registry.commands().each {
            def tmp = it.completor
            
            if (tmp) {
                completors << tmp
            }
        }
        
        reader.addCompletor(new MultiCompletor(completors))
    }
    
    protected String readLine() {
        return reader.readLine(prompt())
    }
    
    void run() {
        log.debug('Running')
        
        while (true) {
            def line = readLine()
            
            log.debug("Read line: $line")
            
            // Stop on null (maybe)
            if (line == null && breakOnNull) {
                break
            }
            
            // Ingore empty lines
            if (line.trim().size() == 0) {
                continue
            }
            
            shell << line
        }
        
        log.debug('Finished')
    }
}


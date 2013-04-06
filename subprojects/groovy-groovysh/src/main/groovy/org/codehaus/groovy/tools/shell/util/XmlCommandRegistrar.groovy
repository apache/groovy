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

package org.codehaus.groovy.tools.shell.util

import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.Command

/**
 * Registers {@link Command} instances from and XML file.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class XmlCommandRegistrar
{
    private final Logger log = Logger.create(this.class)
    
    private final Shell shell
    
    private final ClassLoader classLoader
    
    XmlCommandRegistrar(final Shell shell, final ClassLoader classLoader) {
        assert shell != null
        assert classLoader != null
        
        this.shell = shell
        this.classLoader = classLoader
    }
    
    void register(final URL url) {
        assert url
        
        if (log.debugEnabled) {
            log.debug("Registering commands from: $url")
        }
        
        url.withReader {Reader reader ->
            groovy.util.Node doc = new groovy.util.XmlParser().parse(reader)

            doc.children().each {groovy.util.Node element ->
                String classname = element.text()
                
                Class type = classLoader.loadClass(classname)
                
                Command command = type.newInstance(shell) as Command
                
                if (log.debugEnabled) {
                    log.debug("Created command '${command.name}': $command")
                }
                
                shell << command
            }
        }
    }
}

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

/**
 * Provides basic debug logging access for the Shell bits.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ShellLog
{
    static boolean debug = false
    
    static PrintStream out = System.out
    
    final String name
    
    ShellLog(final Class type) {
        this(type.name)
    }

    ShellLog(final Class type, final String suffix) {
        this("${type.name}.$suffix".toString()) // HACK: Must toString() or get a CCE for GStringImpl :-(
    }
    
    ShellLog(final String name) {
        assert name
        
        this.name = name
    }
    
    def invokeMethod(final String name, final Object args) {
        assert name
        assert args
        
        if (debug) {
            out.println("${name.toUpperCase()} [${this.name}] ${args.join(',')}")
        }
    }
}
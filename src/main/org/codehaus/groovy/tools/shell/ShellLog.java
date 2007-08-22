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

package org.codehaus.groovy.tools.shell;

import java.io.PrintWriter;

import groovy.lang.GroovyObjectSupport;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Provides basic debug logging access for the Shell bits.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ShellLog
    extends GroovyObjectSupport
{
    public static boolean debug = false;
    
    public static PrintWriter out;
    
    public final String name;
    
    public ShellLog(final Class type) {
        this(type.getName());
    }

    public ShellLog(final Class type, final String suffix) {
        this(type.getName() + "." + suffix);
    }
    
    public ShellLog(final String name) {
        assert name != null;
        
        this.name = name;
    }
    
    public Object invokeMethod(final String name, Object args) {
        assert name != null;
        assert args != null;
        
        if (debug) {
            if (args != null && args.getClass().isArray()) {
                args = DefaultGroovyMethods.join((Object[])args, ",");
            }
            
            if (out == null) {
                out = new PrintWriter(System.out, true);
            }
            
            out.println(name.toUpperCase() + " [" + this.name + "] " + args);
            out.flush();
        }
        
        return null;
    }
}

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

package org.codehaus.groovy.tools.shell.util;

import org.codehaus.groovy.tools.shell.IO;

/**
 * Provides a very, very basic logging API.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class Logger
{
    public static IO io;

    public final String name;

    private Logger(final String name) {
        assert name != null;

        this.name = name;
    }
    
    private void log(final String level, final Object msg, final Throwable cause) throws Exception {
        assert level != null;
        assert msg != null;
        
        if (io == null) {
            io = new IO();
        }
        
        io.out.print(ANSI.Renderer.encode(level, ANSI.Code.BOLD));
        io.out.print(" [");
        io.out.print(name);
        io.out.print("] ");
        io.out.println(msg);
        
        if (cause != null) {
            cause.printStackTrace(io.out);
        }
        
        io.flush();
    }
    
    //
    // Level helpers
    //
    
    private static final String DEBUG = "DEBUG";
    
    public static boolean debug = false;
    
    public boolean isDebugEnabled() {
        return debug;
    }
    
    public void debug(final Object msg) throws Exception {
        if (debug) {
            if (msg instanceof Throwable) {
                Throwable cause = (Throwable) msg;
                log(DEBUG, cause.getMessage(), cause);
            }
            else {
                log(DEBUG, msg, null);
            }
        }
    }
    
    public void debug(final Object msg, final Throwable cause) throws Exception {
        if (debug) {
            log(DEBUG, msg, cause);
        }
    }
    
    //
    // Factory access
    //
    
    public static Logger create(final Class type) {
        return new Logger(type.getName());
    }

    public static Logger create(final Class type, final String suffix) {
        return new Logger(type.getName() + "." + suffix);
    }
}

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.PrintWriter;

import java.lang.reflect.Method;

// import org.codehaus.groovy.tools.shell.util.AnsiString;

/**
 * Automatically renders printed strings for {@link AnsiString} formated expressions.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class AnsiPrintWriter
    extends PrintWriter
{
    public AnsiPrintWriter(final OutputStream out) {
        super(out);
    }
    
    public AnsiPrintWriter(final OutputStream out, final boolean autoFlush) {
        super(out, autoFlush);
    }
    
    public AnsiPrintWriter(final Writer out) {
        super(out);
    }
    
    public AnsiPrintWriter(final Writer out, final boolean autoFlush) {
        super(out, autoFlush);
    }
    
    public void write(String text) {
        if (text != null && text.indexOf("@|") >=0) {
            text = render(text);
        }
        
        super.write(text);
    }
    
    //
    // HACK: Invoke the AnsiString.render() method using reflection to avoid problems building...
    //       come on ubercompile already folks :-P
    //
    
    private Method renderMethod;
    
    private String render(final String text) {
        try {
            if (renderMethod == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class type = cl.loadClass("org.codehaus.groovy.tools.shell.util.AnsiString");
                renderMethod = type.getMethod("render", new Class[]{ String.class });
            }
            
            return (String)renderMethod.invoke(null, new Object[]{ text });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

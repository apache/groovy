/*
 * Copyright 2009 the original author or authors.
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
package groovy.io;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A PrintWriter that outputs objects in Groovy style.
 * That means print(Object) uses InvokerHelper.toString(Object)
 * to produce the same results as Writer.print(Object).
 *
 * @author Jim White
 */
public class GroovyPrintWriter extends PrintWriter 
{
    public GroovyPrintWriter(File file) throws FileNotFoundException
    {
        super(file);
    }

    public GroovyPrintWriter(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        super(file, csn);
    }

    public GroovyPrintWriter(Writer out) 
    {
        super(out);
    }

    public GroovyPrintWriter(Writer out, boolean autoflush) 
    {
        super(out, autoflush);
    }

    public GroovyPrintWriter(OutputStream out) 
    {
        super(out);
    }

    public GroovyPrintWriter(OutputStream out, boolean autoflush) 
    {
        super(out, autoflush);
    }

    public GroovyPrintWriter(String filename) throws FileNotFoundException 
    {
        super(filename);
    }

    public GroovyPrintWriter(String filename, String csn)
        throws FileNotFoundException, UnsupportedEncodingException 
    {
        super(filename, csn);
    }
        
// Don't need to do this if Groovy is going to print char[] like a string.
//    public void print(char[] x) 
//    {
//        super.write(InvokerHelper.toString(x));
//    }

    public void print(Object x) 
    {
        super.write(InvokerHelper.toString(x));
    }
}

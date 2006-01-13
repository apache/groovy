/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.pages;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * A PrintWriter used in the generation of GSP pages that allows printing to the target output stream and
 * maintains a record of the current line number during usage.
 *
 * @author Graeme Rocher
 * @since 13-Jan-2006
 */
public class GSPWriter extends PrintWriter {
    private int lineNumber;

    public GSPWriter(Writer out) {
        super(out);
    }

    public void write(char buf[], int off, int len) {
        super.write(buf, off, len);
    }

    public void write(String s, int off, int len) {
        super.write(s, off, len);
    }

    public void write(int c) {
        super.write(c);
    }

    public void printlnToOut(String s) {
        if(s == null) s = "''";
        super.print("out.print(");
        super.print(s);
        super.print(")");
        super.println();
    }

    public void println() {
        this.lineNumber++;
        super.println();
    }

    public int getCurrentLineNumber() {
        return this.lineNumber;
    }
}

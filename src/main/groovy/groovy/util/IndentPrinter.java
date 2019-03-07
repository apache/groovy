/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util;

import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A helper class for printing indented text. This can be used stand-alone or, more commonly, from Builders.
 * <p>
 * By default, a PrintWriter to System.out is used as the Writer, but it is possible
 * to change the Writer by passing a new one as a constructor argument.
 * <p>
 * Indention by default is 2 characters but can be changed by passing a
 * different value as a constructor argument.
 * <p>
 * The following is an example usage. Note that within a "with" block you need to
 * specify a parameter name so that this.println is not called instead of IndentPrinter.println:
 * <pre>
 * new IndentPrinter(new PrintWriter(out)).with { p ->
 *     p.printIndent()
 *     p.println('parent1')
 *     p.incrementIndent()
 *     p.printIndent()
 *     p.println('child 1')
 *     p.printIndent()
 *     p.println('child 2')
 *     p.decrementIndent()
 *     p.printIndent()
 *     p.println('parent2')
 *     p.flush()
 * }
 * </pre>
 * The above example prints this to standard output:
 * <pre>
 * parent1
 *   child 1
 *   child 2
 * parent2
 * </pre>
 */
public class IndentPrinter {

    private int indentLevel;
    private final String indent;
    private final Writer out;
    private final boolean addNewlines;
    private boolean autoIndent;

    /**
     * Creates an IndentPrinter backed by a PrintWriter pointing to System.out, with an indent of two spaces.
     *
     * @see #IndentPrinter(Writer, String)
     */
    public IndentPrinter() {
        this(new PrintWriter(System.out), "  ");
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer, with an indent of two spaces.
     *
     * @param out Writer to output to
     * @see #IndentPrinter(Writer, String)
     */
    public IndentPrinter(Writer out) {
        this(out, "  ");
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer,
     * with a user-supplied String to be used for indenting.
     *
     * @param out Writer to output to
     * @param indent character(s) used to indent each line
     */
    public IndentPrinter(Writer out, String indent) {
        this(out, indent, true);
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer,
     * with a user-supplied String to be used for indenting
     * and the ability to override newline handling.
     *
     * @param out Writer to output to
     * @param indent character(s) used to indent each line
     * @param addNewlines set to false to gobble all new lines (default true)
     */
    public IndentPrinter(Writer out, String indent, boolean addNewlines) {
       this(out, indent, addNewlines, false);
    }

    /**
     * Create an IndentPrinter to the given PrintWriter
     * @param out Writer to output to
     * @param indent character(s) used to indent each line
     * @param addNewlines set to false to gobble all new lines (default true)
     * @param autoIndent set to true to make println() prepend the indent automatically (default false)
     */
    public IndentPrinter(Writer out, String indent, boolean addNewlines, boolean autoIndent) {
        this.addNewlines = addNewlines;
        if (out == null) {
            throw new IllegalArgumentException("Must specify a Writer");
        }
        this.out = out;
        this.indent = indent;
        this.autoIndent = autoIndent;
    }

    /**
     * Prints a string followed by an end of line character.
     *
     * @param  text String to be written
     */
    public void println(String text) {
        try {
            if(autoIndent) printIndent();
            out.write(text);
            println();
        } catch(IOException ioe) {
            throw new GroovyRuntimeException(ioe);
        }
    }

    /**
     * Prints a string.
     *
     * @param  text String to be written
     */
    public void print(String text) {
        try {
            out.write(text);
        } catch(IOException ioe) {
            throw new GroovyRuntimeException(ioe);
        }
    }

    /**
     * Prints a character.
     *
     * @param  c char to be written
     */
    public void print(char c) {
        try {
            out.write(c);
        } catch(IOException ioe) {
            throw new GroovyRuntimeException(ioe);
        }
    }

    /**
     * Prints the current indent level.
     */
    public void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            try {
                out.write(indent);
            } catch(IOException ioe) {
                throw new GroovyRuntimeException(ioe);
            }
        }
    }

    /**
     * Prints an end-of-line character (if enabled via addNewLines property).
     * Defaults to outputting a single '\n' character but by using a custom
     * Writer, e.g. PlatformLineWriter, you can get platform-specific
     * end-of-line characters.
     *
     * @see #IndentPrinter(Writer, String, boolean)
     */
    public void println() {
        if (addNewlines) {
            try {
                out.write("\n");
            } catch(IOException ioe) {
                throw new GroovyRuntimeException(ioe);
            }
        }
    }

    public void incrementIndent() {
        ++indentLevel;
    }

    public void decrementIndent() {
        --indentLevel;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public boolean getAutoIndent(){
        return this.autoIndent;
    }

    public void setAutoIndent(boolean autoIndent){
        this.autoIndent = autoIndent;
    }

    public void flush() {
        try {
            out.flush();
        } catch(IOException ioe) {
            throw new GroovyRuntimeException(ioe);
        }
    }
}

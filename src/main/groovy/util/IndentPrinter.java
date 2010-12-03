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
package groovy.util;

import java.io.PrintWriter;

/**
 * <p>A helper class for printing indented text. This can be used stand-alone or, more commonly, from Builders. </p>
 *
 * <p>By default, System.out is used as the PrintWriter, but it is possible
 * to change the PrintWriter by passing a new one as a constructor argument. </p>
 *
 * <p>Indention by default is 2 characters but can be changed by passing a
 * different value as a constructor argument. </p>
 *
 * <p>The following is an example usage. Note that within a "with" block you need to
 * specify a parameter name so that this.println is not called instead of IndentPrinter.println: </p>
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
 * <p>The above example prints this to standard output: </p>
 * <pre>
 * parent1
 *   child 1
 *   child 2
 * parent2
 * </pre>
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IndentPrinter {

    private int indentLevel;
    private String indent;
    private PrintWriter out;
    private final boolean addNewlines;

    /**
     * Creates a PrintWriter pointing to System.out, with an indent of two
     * spaces.
     * @see #IndentPrinter(PrintWriter, String)
     */
    public IndentPrinter() {
        this(new PrintWriter(System.out), "  ");
    }

    /**
     * Create an IndentPrinter to the given PrintWriter, with an indent of two
     * spaces.
     * @param out PrintWriter to output to
     * @see #IndentPrinter(PrintWriter, String)
     */
    public IndentPrinter(PrintWriter out) {
        this(out, "  ");
    }

    /**
     * Create an IndentPrinter to the given PrintWriter
     * @param out PrintWriter to output to
     * @param indent character(s) used to indent each line
     */
    public IndentPrinter(PrintWriter out, String indent) {
        this(out, indent, true);
    }

    public IndentPrinter(PrintWriter out, String indent, boolean addNewlines) {
        this.addNewlines = addNewlines;
        if (out == null) {
            /** @todo temporary hack */
            out = new PrintWriter(System.out);
            //throw new IllegalArgumentException("Must specify a PrintWriter");
        }
        this.out = out;
        this.indent = indent;
    }

    public void println(String text) {
        out.print(text);
        println();
    }

    public void print(String text) {
        out.print(text);
    }

    public void print(char c) {
        out.print(c);
    }

    /**
     * Prints the current indent level.  
     */
    public void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            out.print(indent);
        }
    }

    public void println() {
        if (addNewlines) out.print("\n");
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

    public void flush() {
        out.flush();
    }
}
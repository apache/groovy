/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A helper class for creating nested trees of data
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class NodePrinter {

    private int indentLevel;
    private String indent = "  ";
    private PrintWriter out;

    public NodePrinter(PrintWriter out) {
        if (out == null) {
            /** @todo temporary hack */
            out = new PrintWriter(System.out);
            // throw new IllegalArgumentException("Must specify a PrintWriter");
        }
        this.out = out;
    }

    public void print(Node node) {
        printIndent();
        printName(node);
        Map attributes = node.attributes();
        boolean hasAttributes = attributes != null && !attributes.isEmpty();
        if (hasAttributes) {
            printAttributes(attributes);
        }
        Object value = node.value();
        if (value instanceof List) {
            if (! hasAttributes) {
                print("()");
            }
            printList((List) value);
        }
        else {
            if (value instanceof String) {
                print("('");
                print((String) value);
                println("')");
            }
            else {
                println("()");
            }
        }
        out.flush();
    }

    protected void printName(Node node) {
        Object name = node.name();
        if (name != null) {
            print(name.toString());
        }
        else {
            print("null");
        }
    }

    protected void printList(List list) {
        if (list.isEmpty()) {
            println("");
        }
        else {
            println(" {");
            ++indentLevel;
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object value = iter.next();
                if (value instanceof Node) {
                    print((Node) value);
                }
                else {
                    printIndent();
                    print("builder.append(");
                    print(InvokerHelper.toString(value));
                    println(")");
                }
            }
            --indentLevel;
            printIndent();
            println("}");
        }
    }

    protected void printAttributes(Map attributes) {
        print("(");
        boolean first = true;
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (first) {
                first = false;
            }
            else {
                print(", ");
            }
            print(entry.getKey().toString());
            print(":");
            print(InvokerHelper.toString(entry.getValue()));
        }
        print(")");
    }

    protected void println(String text) {
        out.print(text);
        out.println();
    }

    protected void print(String text) {
        out.print(text);
    }

    protected void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            out.print(indent);
        }
    }
}

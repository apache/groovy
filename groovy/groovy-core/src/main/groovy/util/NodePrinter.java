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
package groovy.util;



import java.io.OutputStreamWriter;
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

    private IndentPrinter out;

    public NodePrinter() {
        this(new IndentPrinter(new PrintWriter(new OutputStreamWriter(System.out))));
    }

    public NodePrinter(PrintWriter out) {
        this(new IndentPrinter(out));
    }

    public NodePrinter(IndentPrinter out) {
        this.out = out;
    }

    public void print(Node node) {
        out.printIndent();
        printName(node);
        Map attributes = node.attributes();
        boolean hasAttributes = attributes != null && !attributes.isEmpty();
        if (hasAttributes) {
            printAttributes(attributes);
        }
        Object value = node.value();
        if (value instanceof List) {
            if (!hasAttributes) {
                out.print("()");
            }
            printList((List) value);
        }
        else {
            if (value instanceof String) {
                out.print("('");
                out.print((String) value);
                out.println("')");
            }
            else {
                out.println("()");
            }
        }
        out.flush();
    }

    protected void printName(Node node) {
        Object name = node.name();
        if (name != null) {
            out.print(name.toString());
        }
        else {
            out.print("null");
        }
    }

    protected void printList(List list) {
        if (list.isEmpty()) {
            out.println("");
        }
        else {
            out.println(" {");
            out.incrementIndent();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object value = iter.next();
                if (value instanceof Node) {
                    print((Node) value);
                }
                else {
                    out.printIndent();
                    out.print("builder.append(");
                    out.print(InvokerHelper.toString(value));
                    out.println(")");
                }
            }
            out.decrementIndent();
            out.printIndent();
            out.println("}");
        }
    }

    protected void printAttributes(Map attributes) {
        out.print("(");
        boolean first = true;
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (first) {
                first = false;
            }
            else {
                out.print(", ");
            }
            out.print(entry.getKey().toString());
            out.print(":");
            out.print(InvokerHelper.toString(entry.getValue()));
        }
        out.print(")");
    }
}

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
package groovy.xml;

import groovy.util.BuilderSupport;
import groovy.util.IndentPrinter;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class for creating XML or HTML markup
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MarkupBuilder extends BuilderSupport {

    private IndentPrinter out;
    private boolean newline;

    public MarkupBuilder() {
        this(new IndentPrinter());
    }

    public MarkupBuilder(PrintWriter writer) {
        this(new IndentPrinter(writer));
    }

    public MarkupBuilder(IndentPrinter out) {
        this.out = out;
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object createNode(Object name) {
        out.printIndent();
        out.print("<");
        print(name);
        out.println(">");
        out.incrementIndent();
        newline = true;
        return name;
    }

    protected Object createNode(Object name, Object value) {
        out.printIndent();
        out.print("<");
        print(name);
        out.print(">");
        print(value);
        newline = false;
        return name;
    }

    protected Object createNode(Object name, Map attributes) {
        out.printIndent();
        out.print("<");
        out.print(name.toString());
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.print(" ");
            print(entry.getKey());
            out.print("='");
            print(entry.getValue());
            out.print("'");
        }
        out.println(">");
        out.incrementIndent();
        newline = true;
        return name;
    }

    protected void nodeCompleted(Object node) {
        if (newline) {
            out.decrementIndent();
            out.printIndent();
        }
        out.print("</");
        print(node);
        out.println(">");
        out.flush();
    }

    protected void print(Object node) {
        if (node != null) {
            out.print(node.toString());
        }
        else {
            out.print("null");
        }
    }
}

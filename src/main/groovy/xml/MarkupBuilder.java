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
 * @author Stefan Matthias Aust
 * @version $Revision$
 */
public class MarkupBuilder extends BuilderSupport {

    private IndentPrinter out;
    private boolean nospace;
    private int state;

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

    public Object getProperty(String property) {
        if (property.equals("_")) {
            nospace = true;
            return null;
        } else {
            Object node = createNode(property);
            nodeCompleted(getCurrent(), node);
            return node;
        }
    }

    protected Object createNode(Object name) {
        toState(1, name);
        return name;
    }

    protected Object createNode(Object name, Object value) {
        toState(2, name);
        out.print(">");
        print(value);
        return name;
    }

    protected Object createNode(Object name, Map attributes) {
        toState(1, name);
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.print(" ");
            print(transformName(entry.getKey().toString()));
            out.print("='");
            print(transformValue(entry.getValue().toString()));
            out.print("'");
        }
        return name;
    }

    protected void nodeCompleted(Object parent, Object node) {
        toState(3, node);
        out.flush();
    }

    protected void print(Object node) {
        out.print(node == null ? "null" : node.toString());
    }

    protected Object getName(String methodName) {
		return super.getName(transformName(methodName));
	}

    protected String transformName(String name) {
    	if (name.startsWith("_")) name = name.substring(1);
    	return name.replace('_', '-');
    }

    protected String transformValue(String value) {
        return value.replaceAll("\\'", "&quot;");
    }

    private void toState(int next, Object name) {
        switch (state) {
        case 0:
            switch (next) {
            case 1:
            case 2:
                out.print("<");
                print(name);
                break;
            case 3:
                throw new Error();
            }
            break;
        case 1:
            switch (next) {
            case 1:
            case 2:
                out.print(">");
                if (nospace) {
                    nospace = false;
                } else {
                    out.println();
                    out.incrementIndent();
                    out.printIndent();
                }
                out.print("<");
                print(name);
                break;
            case 3:
                out.print(" />");
                break;
            }
            break;
        case 2:
            switch (next) {
            case 1:
            case 2:
                throw new Error();
            case 3:
                out.print("</");
                print(name);
                out.print(">");
                break;
            }
            break;
        case 3:
            switch (next) {
            case 1:
            case 2:
                if (nospace) {
                    nospace = false;
                } else {
                    out.println();
	                out.printIndent();
                }
                out.print("<");
                print(name);
                break;
            case 3:
                if (nospace) {
                    nospace = false;
                } else {
                    out.println();
                    out.decrementIndent();
                    out.printIndent();
                }
                out.print("</");
                print(name);
                out.print(">");
                break;
            }
            break;
        }
        state = next;
    }

}

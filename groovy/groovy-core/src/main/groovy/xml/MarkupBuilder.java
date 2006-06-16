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
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * A helper class for creating XML or HTML markup
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Stefan Matthias Aust
 * @author <a href="mailto:scottstirling@rcn.com">Scott Stirling</a>
 * @version $Revision$
 */
public class MarkupBuilder extends BuilderSupport {

    private IndentPrinter out;
    private boolean nospace;
    private int state;
    private boolean nodeIsEmpty = true;

    public MarkupBuilder() {
        this(new IndentPrinter());
    }

    public MarkupBuilder(PrintWriter writer) {
        this(new IndentPrinter(writer));
    }

    public MarkupBuilder(Writer writer) {
        this(new IndentPrinter(new PrintWriter(writer)));
    }

    public MarkupBuilder(IndentPrinter out) {
        this.out = out;
    }

    protected IndentPrinter getPrinter() {
        return this.out;
    }

    protected void setParent(Object parent, Object child) {
    }

    /*
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
    */

    protected Object createNode(Object name) {
        toState(1, name);
        return name;
    }

    protected Object createNode(Object name, Object value) {
        toState(2, name);
        out.print(">");
        out.print(escapeElementContent(value.toString()));
        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        toState(1, name);
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.print(" ");
            print(transformName(entry.getKey().toString()));
            out.print("='");
            print(escapeAttributeValue(entry.getValue().toString()));
            out.print("'");
        }
        if (value != null)
        {
            nodeIsEmpty = false;
            out.print(">" + escapeElementContent(value.toString()) + "</" + name + ">");
        }
        return name;
    }

    protected Object createNode(Object name, Map attributes) {
        return createNode(name, attributes, null);
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

    /**
     * Returns a String with special XML characters escaped as entities so that
     * output XML is valid. Escapes the following characters as corresponding 
     * entities:
     * <ul>
     *   <li>\' as &amp;apos;</li>
     *   <li>&amp; as &amp;amp;</li>
     *   <li>&lt; as &amp;lt;</li>
     *   <li>&gt; as &amp;gt;</li>
     * </ul>
     * 
     * @param value to be searched and replaced for XML special characters.
     * @return value with XML characters escaped
     */
    protected String transformValue(String value) {
        // & has to be checked and replaced before others
        if (value.matches(".*&.*")) {
            value = value.replaceAll("&", "&amp;");
        }
        if (value.matches(".*\\'.*")) {
            value = value.replaceAll("\\'", "&apos;");
        }
        if (value.matches(".*<.*")) {
            value = value.replaceAll("<", "&lt;");
        }
        if (value.matches(".*>.*")) {
            value = value.replaceAll(">", "&gt;");
        }
        return value;
    }

    /**
     * Escapes a string so that it can be used directly as an XML
     * attribute value.
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     * @see #escapeXmlValue(String, boolean)
     */
    private String escapeAttributeValue(String value) {
        return escapeXmlValue(value, true);
    }

    /**
     * Escapes a string so that it can be used directly in XML element
     * content.
     * @param value The string to escape.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     * @see #escapeXmlValue(String, boolean)
     */
    private String escapeElementContent(String value) {
        return escapeXmlValue(value, false);
    }

    /**
     * Escapes a string so that it can be used in XML text successfully.
     * It replaces the following characters with the corresponding XML
     * entities:
     * <ul>
     *   <li>&amp; as &amp;amp;</li>
     *   <li>&lt; as &amp;lt;</li>
     *   <li>&gt; as &amp;gt;</li>
     * </ul>
     * If the string is to be added as an attribute value, these
     * characters are also escaped:
     * <ul>
     *   <li>' as &amp;apos;</li>
     * </ul>
     * @param value The string to escape.
     * @param isAttrValue <code>true</code> if the string is to be used
     * as an attribute value, otherwise <code>false</code>.
     * @return A new string in which all characters that require escaping
     * have been replaced with the corresponding XML entities.
     */
    private String escapeXmlValue(String value, boolean isAttrValue){
        // & has to be checked and replaced before others
        if (value.matches(".*&.*")) {
            value = value.replaceAll("&", "&amp;");
        }
        if (value.matches(".*<.*")) {
            value = value.replaceAll("<", "&lt;");
        }
        if (value.matches(".*>.*")) {
            value = value.replaceAll(">", "&gt;");
        }
        if (isAttrValue){
            // The apostrophe is only escaped if the value is for an
            // attribute, as opposed to element content.
            if (value.matches(".*\\'.*")) {
                value = value.replaceAll("\\'", "&apos;");
            }
        }
        return value;
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
                        if (nodeIsEmpty) {
                            out.print(" />");
                        }
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

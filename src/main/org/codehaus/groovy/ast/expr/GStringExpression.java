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
package org.codehaus.groovy.ast.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a String expression which contains embedded values inside
 * it such as "hello there ${user} how are you" which is expanded lazily
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GStringExpression extends Expression {

    private String verbatimText;
    private List strings = new ArrayList();
    private List values = new ArrayList();

    public GStringExpression(String verbatimText) {
        this.verbatimText = verbatimText;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitGStringExpression(this);
    }

    public String toString() {
        return super.toString() + "[strings: " + strings + " values: " + values + "]";
    }

    public List getStrings() {
        return strings;
    }

    public List getValues() {
        return values;
    }

    public void addString(ConstantExpression text) {
        if (text == null) {
            throw new NullPointerException("Cannot add a null text expression");
        }
        strings.add(text);
    }

    public void addValue(Expression value) {
        values.add(value);
    }

    public Expression getValue(int idx) {
        return (Expression) values.get(idx);
    }

    public boolean isConstantString() {
        return values.isEmpty();
    }

    public Expression asConstantString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iter = strings.iterator(); iter.hasNext();) {
            ConstantExpression expression = (ConstantExpression) iter.next();
            Object value = expression.getValue();
            if (value != null) {
                buffer.append(value);
            }
        }
        return new ConstantExpression(buffer.toString());
    }
}

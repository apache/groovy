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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents an array object construction 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ArrayExpression extends Expression {
    private String type;
    private List expressions;

    public ArrayExpression() {
        this(new ArrayList());
    }

    public ArrayExpression(List expressions) {
        this("java/lang/Object", expressions);
    }

    public ArrayExpression(String type, List expressions) {
        this.type = type;
        this.expressions = expressions;

        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (!(item instanceof Expression)) {
                throw new ClassCastException("Item: " + item + " is not an Expression");
            }
        }
    }

    public ArrayExpression(Expression[] expressionArray) {
        this();
        expressions.addAll(Arrays.asList(expressionArray));
    }

    public void addExpression(Expression expression) {
        expressions.add(expression);
    }

    public List getExpressions() {
        return expressions;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitArrayExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return new ArrayExpression(type, transformExpressions(expressions, transformer));
    }

    public Expression getExpression(int i) {
        Object object = expressions.get(i);
        return (Expression) object;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        StringBuffer buffer = new StringBuffer("[");
        boolean first = true;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }

            buffer.append(((Expression) iter.next()).getText());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String toString() {
        return super.toString() + expressions;
    }
}

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

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeHelper;


/**
 * Represents a local variable name, the simplest form of expression. e.g. "foo".
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VariableExpression extends Expression {

    public static final VariableExpression THIS_EXPRESSION = new VariableExpression("this", null);
    public static final VariableExpression SUPER_EXPRESSION = new VariableExpression("super", null);

    private String variable;

    public VariableExpression(String variable, String type) {
        this.variable = variable;
        if (type == null || type.length() == 0) {
            isDynamic = true;
        }
        else {
            String boxedType = BytecodeHelper.getObjectTypeForPrimitive(type);

            super.setType(boxedType);  // todo delay setting until resolve()
            isDynamic = false;
        }
    }

    public VariableExpression(String variable) {
        this.variable = variable;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitVariableExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    protected void resolveType(AsmClassGenerator resolver) {
        resolver.resolve(this);
    }

    public String getVariable() {
        return variable;
    }

    public String getText() {
        return variable;
    }

    public String getType() {
        if (type == null) {
            return "java.lang.Object";
        }
        return type;
    }

    boolean isDynamic = true;
    public boolean isDynamic() {
        return isDynamic;
    }

    /**
     * @return true if this variable is dynamically typed
     */

    public String toString() {
        return super.toString() + "[variable: " + variable + ((isDynamic()) ? "" : " type: " + type) + "]";
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }
}

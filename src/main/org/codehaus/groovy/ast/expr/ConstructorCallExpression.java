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

import java.lang.reflect.Constructor;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.classgen.AsmClassGenerator;

/**
 * A constructor call
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ConstructorCallExpression extends Expression {

    private Expression arguments;

    public String getTypeToSet() {
        return typeToSet;
    }

    private String typeToSet; // type to be set on the type field. delayed until classgen time to avoid resolving to the current class.

    public Constructor getConstructor() {
        return constructor;
    }

    private Constructor constructor = null;

    protected boolean shouldContinue() {
        if (super.isResolveFailed())
            return false;
        if (constructor == null)
            return true;
        return false;
    }

    public ConstructorCallExpression(String type, Expression arguments) {
        //super.setType(type);
        this.typeToSet = type;
        setTypeResolved(false); // to set it when arguments is resolved
        this.arguments = arguments;
    }
    
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitConstructorCallExpression(this);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        return new ConstructorCallExpression(type, transformer.transform(arguments)); 
    }

    protected void resolveType(AsmClassGenerator resolver) {
        arguments.resolve(resolver);
        if (constructor == null) {
            resolver.resolve(this);
        }
//        if (getTypeClass() != null)
//            setTypeResolved(true);
//        else
//            resolver.resolve(this);
    }

    public String getType() {
        if (type == null) {
            if (typeToSet != null && typeToSet.length() > 0) {
                return typeToSet;
            }
            else {
                return "java.lang.Object";
            }
        }
        return type;
    }

    public Expression getArguments() {
        return arguments;
    }

    public String getText() {
        return "new " + type + "(" + arguments.getText() + ")";
    }

    public String toString() {
        return super.toString() + "[type: " + type + " arguments: " + arguments + "]";
    }

    public void setConstructor(Constructor ctor) {
        constructor = ctor;
        super.setTypeClass(ctor.getDeclaringClass());
    }
}

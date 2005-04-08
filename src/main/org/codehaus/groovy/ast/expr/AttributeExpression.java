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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.classgen.AsmClassGenerator;

/**
 * Represents an attribute access (accessing the field of a class) such as the expression "foo.@bar".
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class AttributeExpression extends Expression {

    private Expression objectExpression;
    private String property;
    private boolean spreadSafe = false;
    private boolean safe = false;
    private boolean isStatic = false;

    private Method getter = null;
    private Method setter = null;

    private Field field = null;
    private int access = -1;

    public boolean isStatic() {
        return isStatic;
    }

    public AttributeExpression(Expression objectExpression, String property) {
        this(objectExpression, property, false);
    }

    public AttributeExpression(Expression objectExpression, String property, boolean safe) {
        this.objectExpression = objectExpression;
        this.property = property;
        this.safe = safe;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitAttributeExpression(this);
    }

    public boolean isDynamic() {
        return true;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    protected void resolveType(AsmClassGenerator resolver) {
        objectExpression.resolve(resolver);
        resolver.resolve(this);
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public String getProperty() {
        return property;
    }

    public String getText() {
        return objectExpression.getText() + "." + property;
    }

    /**
     * @return is this a safe navigation, i.e. if true then if the source object is null
     * then this navigation will return null
     */
    public boolean isSafe() {
        return safe;
    }

    public boolean isSpreadSafe() {
        return spreadSafe;
    }

    public void setSpreadSafe(boolean value) {
        spreadSafe = value;
    }

    public String toString() {
        return super.toString() + "[object: " + objectExpression + " property: " + property + "]";
    }

    public void setStatic(boolean aStatic) {
        this.isStatic = aStatic;
    }

    public void setGetter(Method meth) {
        Class returntype = meth.getReturnType();
        Class oldType = getTypeClass();
        if (oldType != null && oldType != Object.class && oldType != returntype) {
            // something is wrong
// in this rare case the getter is discarded. Field access takes over
//            String msg = "PropertyExpression.setSetter(): type mismatch: was " + getTypeClass() +
//                    ". now " + returntype;
//            System.err.println(msg);
//            setResolveFailed(true);
//            setFailure(msg);
        }
        else {
            getter = meth;
            setTypeClass(returntype);
            setTypeResolved(true);
        }
    }

    public Method getGetter() {
        return getter;
    }

    public void setSetter(Method method) {
        Class paramType = method.getParameterTypes()[0];
        Class wasType = getTypeClass();
        if (wasType != null && wasType != Object.class && wasType != paramType) {
//            // something is wrong
// in this rare case the getter is discarded. Field access takes over
//            String msg = "PropertyExpression.setSetter(): type mismatch: was " + getTypeClass() +
//                    ". now " + paramType;
//            System.err.println(msg);
//            setResolveFailed(true);
//            setFailure(msg);
        }
        else {
            setter = method;
            setTypeClass(paramType);
            setTypeResolved(true);
        }
    }
    public Method getSetter() {
        return setter;
    }

    public void setField(Field fld) {
        field = fld;
        setStatic(Modifier.isStatic(fld.getModifiers()));
        setTypeClass(fld.getType());
    }
    public Field getField() {
        return field;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getAccess() {
        return access;
    }
}

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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a property access such as the expression "foo.bar".
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class PropertyExpression extends Expression {

    private Expression objectExpression;
    private Expression property;
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

    public PropertyExpression(Expression objectExpression, String property) {
        this(objectExpression, new ConstantExpression(property), false);
    }
    
    public PropertyExpression(Expression objectExpression, Expression property) {
        this(objectExpression, property, false);
    }

    public PropertyExpression(Expression objectExpression, Expression property, boolean safe) {
        this.objectExpression = objectExpression;
        this.property = property;
        this.safe = safe;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitPropertyExpression(this);
    }

    public boolean isDynamic() {
        return true;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }

    public Expression getObjectExpression() {
        return objectExpression;
    }

    public void setObjectExpression(Expression exp) {
        objectExpression=exp;
    }    
    
    public Expression getProperty() {
        return property;
    }
    
    public String getPropertyAsString() {
        if (property==null) return null;
        if (! (property instanceof ConstantExpression)) return null;
        ConstantExpression constant = (ConstantExpression) property;
        return constant.getText();
    }

    public String getText() {
        return objectExpression.getText() + "." + property.getText();
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

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setField(Field fld) {
        field = fld;
        setStatic(Modifier.isStatic(fld.getModifiers()));
        setType(ClassHelper.make(fld.getType()));
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

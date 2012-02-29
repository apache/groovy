/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast;

import java.lang.reflect.Field;

import org.codehaus.groovy.ast.expr.Expression;
import org.objectweb.asm.Opcodes;

/**
 * Represents a field (member variable)
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class FieldNode extends AnnotatedNode implements Opcodes, Variable {

    private String name;
    private int modifiers;
    private ClassNode type;
    private ClassNode owner;
    private Expression initialValueExpression;
    private boolean dynamicTyped;
    private boolean holder;
    private ClassNode originType = ClassHelper.DYNAMIC_TYPE;

    public static FieldNode newStatic(Class theClass, String name) throws SecurityException, NoSuchFieldException {
        Field field = theClass.getField(name);
        ClassNode fldType = ClassHelper.make(field.getType());
        return new FieldNode(name, ACC_PUBLIC | ACC_STATIC, fldType, ClassHelper.make(theClass), null);
    }

    public FieldNode(String name, int modifiers, ClassNode type, ClassNode owner, Expression initialValueExpression) {
        this.name = name;
        this.modifiers = modifiers;
        this.type = type;
        if (this.type == ClassHelper.DYNAMIC_TYPE && initialValueExpression != null)
            this.setType(initialValueExpression.getType());
        this.setType(type);
        this.originType = type;
        this.owner = owner;
        this.initialValueExpression = initialValueExpression;
    }

    public Expression getInitialExpression() {
        return initialValueExpression;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
        this.originType = type;
        dynamicTyped |= type == ClassHelper.DYNAMIC_TYPE;
    }

    public ClassNode getOwner() {
        return owner;
    }

    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean holder) {
        this.holder = holder;
    }

    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * @return true if the field is static
     */
    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    /**
     * @return true if the field is an enum
     */
    public boolean isEnum() {
        return (modifiers & ACC_ENUM) != 0;
    }

    /**
     * @return true if the field is final
     */
    public boolean isFinal() {
        return (modifiers & ACC_FINAL) != 0;
    }

    /**
     * @return true if the field is volatile
     */
    public boolean isVolatile() {
        return (modifiers & ACC_VOLATILE) != 0;
    }

    /**
     * @return true if the field is public
     */
    public boolean isPublic() {
        return (modifiers & ACC_PUBLIC) != 0;
    }

   /**
     * @return true if the field is protected
     */
    public boolean isProtected() {
        return (modifiers & ACC_PROTECTED) != 0;
    }

    /**
     * @param owner The owner to set.
     */
    public void setOwner(ClassNode owner) {
        this.owner = owner;
    }

    public boolean hasInitialExpression() {
        return initialValueExpression != null;
    }

    public boolean isInStaticContext() {
        return isStatic();
    }

    public Expression getInitialValueExpression() {
        return initialValueExpression;
    }

    public void setInitialValueExpression(Expression initialValueExpression) {
        this.initialValueExpression = initialValueExpression;
    }

    /**
     * @deprecated
     */
    public boolean isClosureSharedVariable() {
        return false;
    }
    /**
     * @deprecated
     */
    public void setClosureSharedVariable(boolean inClosure) {                
    }

    public ClassNode getOriginType() {
        return originType;
    }
    
    public void setOriginType(ClassNode cn) {
        originType = cn;
    }

    public void rename(String name) {
        declaringClass.renameField(this.name, name);
        this.name = name;
    }
}

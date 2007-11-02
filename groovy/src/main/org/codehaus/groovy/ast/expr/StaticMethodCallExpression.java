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
package org.codehaus.groovy.ast.expr;

import groovy.lang.MetaMethod;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * A static method call on a class
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class StaticMethodCallExpression extends Expression {

    private ClassNode ownerType;
    private String method;
    private Expression arguments;
    private MetaMethod metaMethod = null;

    public StaticMethodCallExpression(ClassNode type, String method, Expression arguments) {
        ownerType = type;
        this.method = method;
        this.arguments = arguments;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitStaticMethodCallExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new StaticMethodCallExpression(getOwnerType(), method, transformer.transform(arguments));
        ret.setSourcePosition(this);
        return ret;
    }

    public Expression getArguments() {
        return arguments;
    }

    public String getMethod() {
        return method;
    }

    public String getText() {
        return getOwnerType().getName() + "." + method + arguments.getText();
    }

    public String toString() {
        return super.toString() + "[" + getOwnerType().getName() + "#" + method + " arguments: " + arguments + "]";
    }

    public ClassNode getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(ClassNode ownerType) {
        this.ownerType = ownerType;
    }

    public void setMetaMethod(MetaMethod metaMethod) {
        this.metaMethod = metaMethod;
    }

    public MetaMethod getMetaMethod() {
        return metaMethod;
    }

}

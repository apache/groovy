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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import groovy.lang.GroovyRuntimeException;

/**
 * Represents a base class for expressions which evaluate as an object
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class Expression extends ASTNode {
    protected boolean typeResolved = false;

    public boolean isResolveFailed() {
        return resolveFailed;
    }

    public void setResolveFailed(boolean resolveFailed) {
        this.resolveFailed = resolveFailed;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    String failure = "";
    private boolean resolveFailed = false;

    public Class getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(Class typeClass) {
        if (typeClass != null) {
            this.typeClass = typeClass;
            type = typeClass.getName();
            setTypeResolved(true);
        }
    }

    public Class typeClass = null;

    public String getType() {
        if (type != null){
            return type;
        } else {
            Class cls = getTypeClass();
            return cls != null? cls.getName() : null;//"java.lang.Object";
        }
    }

    /**
     * true if the datatype can be changed, false otherwise.
     * @return
     */
    public boolean isDynamic() {
        return true;
    }

    protected String type = null;
    /**
     * Return a copy of the expression calling the transformer on any nested expressions 
     * @param transformer
     * @return
     */
    public abstract Expression transformExpression(ExpressionTransformer transformer);
    
    /**
     * Transforms the list of expressions
     * @return a new list of transformed expressions
     */
    protected List transformExpressions(List expressions, ExpressionTransformer transformer) {
        List list = new ArrayList(expressions.size());
        for (Iterator iter = expressions.iterator(); iter.hasNext(); ) {
            list.add(transformer.transform((Expression) iter.next()));
        }
        return list;
    }

    public void setType(String name) {
        if (name == null)
            throw new GroovyRuntimeException("cannot set null on type");
        // handle primitives first
        if (name.equals("int")) {
            setTypeClass(Integer.TYPE);
            return;
        }
        else if (name.equals("long")) {
            setTypeClass(Long.TYPE);
            return;
        }
        else if (name.equals("short")) {
            setTypeClass(Short.TYPE);
            return;
        }
        else if (name.equals("float")) {
            setTypeClass(Float.TYPE);
            return;
        }
        else if (name.equals("double")) {
            setTypeClass(Double.TYPE);
            return;
        }
        else if (name.equals("byte")) {
            setTypeClass(Byte.TYPE);
            return;
        }
        else if (name.equals("char")) {
            setTypeClass(Character.TYPE);
            return;
        }
        else if (name.equals("boolean")) {
            setTypeClass(Boolean.TYPE);
            return;
        }

        if (name.endsWith("[]")) {
            String prefix = "[";
            name = name.substring(0, name.length() - 2);

            if (name.equals("int")) {
                type = prefix + "I";
            }
            else if (name.equals("long")) {
                type = prefix + "J";
            }
            else if (name.equals("short")) {
                type = prefix + "S";
            }
            else if (name.equals("float")) {
                type = prefix + "F";
            }
            else if (name.equals("double")) {
                type = prefix + "D";
            }
            else if (name.equals("byte")) {
                type = prefix + "B";
            }
            else if (name.equals("char")) {
                type = prefix + "C";
            }
            else if (name.equals("boolean")) {
                type = prefix + "Z";
            } else {
                type = prefix + "L" + name + ";";
            }
        }
        else {
            type = name;
        }
        if (type == null) {
            System.out.println("Expression.setType(): null");
            System.out.println("name = " + name);
        }
        try {
            this.setTypeClass(Class.forName(type, false, this.getClass().getClassLoader()));
        } catch (Throwable e) {
            this.typeResolved = false;
        }
    }

    public boolean isTypeResolved() {
        return typeResolved;
    }

    public void setTypeResolved(boolean b) {
        this.typeResolved = b;
        this.resolveFailed = false;
    }

    public void resolve(AsmClassGenerator cg) {
        if (shouldContinue()) {
            resolveType(cg);
        }
    }

    protected abstract void resolveType(AsmClassGenerator resolver);

    protected boolean shouldContinue() {
        return !isResolveFailed() && !isTypeResolved();
    }

}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;

/**
 * class used to verify correct usage of generics in
 * class header (class and superclass declaration)
 *
 * @author Jochen Theodorou
 */
public class GenericsVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private final boolean prelimCheckOnly;

    public GenericsVisitor(SourceUnit source) {
        this(source, false);
    }

    public GenericsVisitor(SourceUnit source, boolean prelimCheckOnly) {
        this.source = source;
        this.prelimCheckOnly = prelimCheckOnly;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    @Override
    public void visitClass(ClassNode node) {
        boolean error = checkWildcard(node);
        if (error) return;
        ClassNode superClass = node.getUnresolvedSuperClass(false);
        checkGenericsUsage(node.getUnresolvedSuperClass(false), node.getSuperClass(), prelimCheckOnly);
        ClassNode[] interfaces = node.getInterfaces();
        for (ClassNode anInterface : interfaces) {
            checkGenericsUsage(anInterface, anInterface.redirect());
        }
        node.visitContents(this);
    }

    @Override
    public void visitField(FieldNode node) {
        ClassNode type = node.getType();
        checkGenericsUsage(type, type.redirect());
        super.visitField(node);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        ClassNode type = call.getType();
        checkGenericsUsage(type, type.redirect(), true);
    }

    @Override
    public void visitMethod(MethodNode node) {
        Parameter[] parameters = node.getParameters();
        for (Parameter param : parameters) {
            ClassNode paramType = param.getType();
            checkGenericsUsage(paramType, paramType.redirect());
        }
        ClassNode returnType = node.getReturnType();
        checkGenericsUsage(returnType, returnType.redirect());
        super.visitMethod(node);
    }

    private boolean checkWildcard(ClassNode cn) {
        ClassNode sn = cn.getUnresolvedSuperClass(false);
        if (sn == null) return false;
        GenericsType[] generics = sn.getGenericsTypes();
        if (generics == null) return false;
        boolean error = false;
        for (GenericsType generic : generics) {
            if (generic.isWildcard()) {
                addError("A supertype may not specify a wildcard type", sn);
                error = true;
            }
        }
        return error;
    }

    private void checkGenericsUsage(ClassNode n, ClassNode cn) {
        checkGenericsUsage(n, cn, false);
    }

    private void checkGenericsUsage(ClassNode n, ClassNode cn, boolean skipDiamondErrors) {
        if (n.isGenericsPlaceHolder()) return;
        GenericsType[] nTypes = n.getGenericsTypes();
        GenericsType[] cnTypes = cn.getGenericsTypes();
        // raw type usage is always allowed
        if (nTypes == null) return;
        // parameterize a type by using all of the parameters only
        if (cnTypes == null) {
            addError("The class " + n.getName() + " refers to the class " + cn.getName() +
                    " and uses " + nTypes.length + " parameters, but the referred class takes no parameters", n);
            return;
        }
        if (skipDiamondErrors && nTypes.length == 0) {
            return;
        }
        if (nTypes.length != cnTypes.length) {
            addError("The class " + n.getName() + " refers to the class " + cn.getName() + " and uses " +
                    nTypes.length + " parameters, but the referred class needs " + cnTypes.length, n);
            return;
        }
        // check bounds
        for (int i = 0; i < nTypes.length; i++) {
            ClassNode nType = nTypes[i].getType();
            ClassNode cnType = cnTypes[i].getType();
            if (!nType.isDerivedFrom(cnType)) {
                if (cnType.isInterface() && nType.implementsInterface(cnType)) continue;
                addError("The type " + nTypes[i].getName() +
                        " is not a valid substitute for the bounded parameter <" +
                        getPrintName(cnTypes[i]) + ">", n);
            }
        }
    }

    private static String getPrintName(GenericsType gt) {
        String ret = gt.getName();
        ClassNode[] upperBounds = gt.getUpperBounds();
        ClassNode lowerBound = gt.getLowerBound();
        if (upperBounds != null) {
            ret += " extends ";
            for (int i = 0; i < upperBounds.length; i++) {
                ret += getPrintName(upperBounds[i]);
                if (i + 1 < upperBounds.length) ret += " & ";
            }
        } else if (lowerBound != null) {
            ret += " super " + getPrintName(lowerBound);
        }
        return ret;

    }

    private static String getPrintName(ClassNode cn) {
        String ret = cn.getName();
        GenericsType[] gts = cn.getGenericsTypes();
        if (gts != null) {
            ret += "<";
            for (int i = 0; i < gts.length; i++) {
                if (i != 0) ret += ",";
                ret += getPrintName(gts[i]);
            }
            ret += ">";
        }
        return ret;
    }
}

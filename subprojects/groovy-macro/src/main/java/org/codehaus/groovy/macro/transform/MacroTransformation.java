/*
 * Copyright 2003-2013 the original author or authors.
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
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class MacroTransformation extends ClassCodeVisitorSupport implements ASTTransformation {

    public static final String DOLLAR_VALUE = "$v";
    public static final String MACRO_METHOD = "macro";

    SourceUnit source;
    
    MacroInvocationTrap transformer;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.source = source;

        transformer = new MacroInvocationTrap(source.getSource(), source);

        for(ASTNode node : nodes) {
            if(node instanceof ClassNode) {
                visitClass((ClassNode) node);
            } else if(node instanceof ModuleNode) {
                ModuleNode moduleNode = (ModuleNode) node;
                for (ClassNode classNode : moduleNode.getClasses()) {
                    visitClass(classNode);
                }
            }
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        transformer.visitMethodCallExpression(call);
        super.visitMethodCallExpression(call);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }
}


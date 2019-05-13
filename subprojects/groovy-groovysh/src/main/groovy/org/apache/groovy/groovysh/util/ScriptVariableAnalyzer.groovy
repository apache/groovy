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
package org.apache.groovy.groovysh.util

import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.GroovyClassVisitor
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit

import java.security.CodeSource

/**
 * Class to Class parsing a script to detect all bound and unbound variables.
 * Based on http://glaforge.appspot.com/article/knowing-which-variables-are-bound-or-not-in-a-groovy-script
 */
@TypeChecked
class ScriptVariableAnalyzer {

    /**
     * define a visitor that visits all variable expressions
     */
    static class VariableVisitor extends ClassCodeVisitorSupport implements GroovyClassVisitor {
        Set<String> bound = new HashSet<String>()
        Set<String> unbound = new HashSet<String>()

        @Override
        void visitVariableExpression(VariableExpression expression) {
            // we're not interested in some special implicit variables
            if (!(expression.variable in ['args', 'context', 'this', 'super'])) {
                // thanks to this instanceof
                // we know if the variable is bound or not
                if (expression.accessedVariable instanceof DynamicVariable) {
                    unbound << expression.variable
                } else {
                    bound << expression.variable
                }
            }
            super.visitVariableExpression(expression)
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null
        }
    }

    /**
     * custom PrimaryClassNodeOperation
     * to be able to hook our code visitor
     */
    static class VisitorSourceOperation extends CompilationUnit.PrimaryClassNodeOperation {

        final GroovyClassVisitor visitor

        VisitorSourceOperation(final GroovyClassVisitor visitor) {
            this.visitor = visitor
        }

        @Override
        void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode)
                throws CompilationFailedException {
            classNode.visitContents(visitor)
        }
    }

    /**
     * class loader to add our phase operation
     */
    static class VisitorClassLoader extends GroovyClassLoader {
        final GroovyClassVisitor visitor

        VisitorClassLoader(final GroovyClassVisitor visitor, ClassLoader parent) {
            super(parent == null ?  Thread.currentThread().getContextClassLoader() : parent)
            this.visitor = visitor
        }

        @Override
        protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
            CompilationUnit cu = super.createCompilationUnit(config, source)
            cu.addPhaseOperation(new VisitorSourceOperation(visitor), Phases.CLASS_GENERATION)
            return cu
        }
    }

    static Set<String> getBoundVars(final String scriptText, ClassLoader parent) {
        assert scriptText != null
        GroovyClassVisitor visitor = new VariableVisitor()
        VisitorClassLoader myCL = new VisitorClassLoader(visitor, parent)
        // simply by parsing the script with our classloader
        // our visitor will be called and will visit all the variables
        myCL.parseClass(scriptText)
        return visitor.bound
    }

}

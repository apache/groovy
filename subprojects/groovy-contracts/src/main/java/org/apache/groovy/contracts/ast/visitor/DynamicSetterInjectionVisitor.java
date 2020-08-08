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
package org.apache.groovy.contracts.ast.visitor;

import org.apache.groovy.contracts.generation.BaseGenerator;
import org.apache.groovy.contracts.generation.CandidateChecks;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.apache.groovy.util.BeanUtils;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * <p>
 * Implements contract support for setter methods and default constructors of POGOs.
 * </p>
 *
 * @see BaseVisitor
 */
public class DynamicSetterInjectionVisitor extends BaseVisitor {

    private static final String SPRING_STEREOTYPE_PACKAGE = "org.springframework.stereotype";

    private BlockStatement invariantAssertionBlockStatement;

    public DynamicSetterInjectionVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    protected Statement createSetterBlock(final ClassNode classNode, final FieldNode field, final Parameter parameter) {
        return block(
                invariantAssertionBlockStatement, // check invariant before assignment
                assignS(fieldX(field), varX(parameter)), // do assignment
                invariantAssertionBlockStatement  // check invariant after assignment
        );
    }

    @Override
    public void visitProperty(PropertyNode node) {
        final ClassNode classNode = node.getDeclaringClass();
        final String setterName = "set" + BeanUtils.capitalize(node.getName());

        final Statement setterBlock = node.getSetterBlock();
        final Parameter parameter = param(node.getType(), "value");

        if (CandidateChecks.isClassInvariantCandidate(node) && (setterBlock == null && classNode.getMethod(setterName, new Parameter[]{parameter}) == null)) {
            final Statement setterBlockStatement = createSetterBlock(classNode, node.getField(), parameter);
            node.setSetterBlock(setterBlockStatement);
        }
    }

    @Override
    public void visitClass(ClassNode classNode) {
        // if a class invariant is available visit all property nodes else skip this class
        final MethodNode invariantMethodNode = BaseGenerator.getInvariantMethodNode(classNode);
        if (invariantMethodNode == null || AnnotationUtils.hasAnnotationOfType(classNode, SPRING_STEREOTYPE_PACKAGE))
            return;

        invariantAssertionBlockStatement = block(stmt(callThisX(invariantMethodNode.getName())));

        List<ConstructorNode> declaredConstructors = classNode.getDeclaredConstructors();
        if (declaredConstructors == null || declaredConstructors.isEmpty()) {
            // create default constructor with class invariant check
            ConstructorNode constructor = new ConstructorNode(Opcodes.ACC_PUBLIC, invariantAssertionBlockStatement);
            constructor.setSynthetic(true);
            classNode.addConstructor(constructor);
        }

        super.visitClass(classNode);
    }
}

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
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodCallTransformation;
import org.codehaus.groovy.ast.TransformingCodeVisitor;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.methods.MacroGroovyMethods;
import org.codehaus.groovy.macro.runtime.MacroBuilder;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;

/**
 * Transforms {@link MacroClass} calls into its ClassNode
 *
 * @since 2.5.0
 */

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class MacroClassTransformation extends MethodCallTransformation {

    private static final String MACRO_METHOD = "macro";
    private static final ClassNode MACROCLASS_TYPE = ClassHelper.make(MacroClass.class);

    @Override
    protected GroovyCodeVisitor getTransformer(final ASTNode[] nodes, final SourceUnit sourceUnit) {
        ClassCodeExpressionTransformer transformer = new MacroClassTransformer(sourceUnit);

        return new MacroClassTransformingCodeVisitor(transformer, sourceUnit);
    }

    private static class MacroClassTransformer extends ClassCodeExpressionTransformer {

        private final SourceUnit sourceUnit;

        MacroClassTransformer(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp instanceof ConstructorCallExpression) {
                MethodCallExpression call = exp.getNodeMetaData(MacroTransformation.class);
                if (call != null) {
                    return call;
                }
            }
            return super.transform(exp);
        }
    }

    private static class MacroClassTransformingCodeVisitor extends TransformingCodeVisitor {

        private final SourceUnit sourceUnit;

        MacroClassTransformingCodeVisitor(ClassCodeExpressionTransformer transformer, SourceUnit sourceUnit) {
            super(transformer);
            this.sourceUnit = sourceUnit;
        }

        @Override
        public void visitConstructorCallExpression(final ConstructorCallExpression call) {
            ClassNode type = call.getType();
            if (type instanceof InnerClassNode) {
                if (((InnerClassNode) type).isAnonymous() &&
                        MACROCLASS_TYPE.getNameWithoutPackage().equals(type.getSuperClass().getNameWithoutPackage())) {
                    try {
                        String source = convertInnerClassToSource(type);

                        MethodCallExpression macroCall = callX(
                                propX(classX(ClassHelper.makeWithoutCaching(MacroBuilder.class, false)), "INSTANCE"),
                                MACRO_METHOD,
                                args(
                                        constX(source),
                                        MacroGroovyMethods.buildSubstitutions(sourceUnit, type),
                                        classX(ClassHelper.make(ClassNode.class))
                                )
                        );

                        macroCall.setSpreadSafe(false);
                        macroCall.setSafe(false);
                        macroCall.setImplicitThis(false);
                        call.putNodeMetaData(MacroTransformation.class, macroCall);
                        List<ClassNode> classes = sourceUnit.getAST().getClasses();
                        for (Iterator<ClassNode> iterator = classes.iterator(); iterator.hasNext(); ) {
                            final ClassNode aClass = iterator.next();
                            if (aClass == type || type == aClass.getOuterClass()) {
                                iterator.remove();
                            }
                        }
                    } catch (Exception e) {
                        // FIXME
                        e.printStackTrace();
                    }
                    return;
                }
            }
            super.visitConstructorCallExpression(call);

        }

        private String convertInnerClassToSource(final ClassNode type) throws Exception {
            String source = GeneralUtils.convertASTToSource(sourceUnit.getSource(), type);
            // we need to remove the leading "{" and trailing "}"
            source = source.substring(source.indexOf('{') + 1, source.lastIndexOf('}') - 1);
            return source;
        }
    }
}

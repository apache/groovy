/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.transform.sc;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesWriterControllerFactoryImpl;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.OptimizerVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.stc.*;

import java.util.List;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;

/**
 * Handles the implementation of the {@link groovy.transform.CompileStatic} transformation.
 *
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class StaticCompileTransformation extends StaticTypesTransformation {

    public static final ClassNode COMPILE_STATIC_ANNOTATION = ClassHelper.make(CompileStatic.class);
    private final StaticTypesWriterControllerFactoryImpl factory = new StaticTypesWriterControllerFactoryImpl();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        BinaryExpressionTransformer transformer = new BinaryExpressionTransformer(source);
        List<ClassNode> classes = source.getAST().getClasses();
        for (ClassNode classNode : classes) {
            classNode.putNodeMetaData(WriterControllerFactory.class, factory);
        }
        super.visit(nodes, source);
        for (ClassNode classNode : classes) {
            transformer.visitClass(classNode);
        }
    }

    @Override
    protected StaticTypeCheckingVisitor newVisitor(final SourceUnit unit, final ClassNode node, final TypeCheckerPluginFactory pluginFactory) {
        return new StaticCompilationVisitor(unit, node, pluginFactory);
    }

    /**
     * Some expressions use symbols as aliases to method calls (<<, +=, ...). In static compilation,
     * if such a method call is found, we transform the original binary expression into a method
     * call expression so that the call gets statically compiled.
     *
     * @author Cedric Champeau
     */
    private static class BinaryExpressionTransformer extends ClassCodeExpressionTransformer {
        private final SourceUnit unit;

        private BinaryExpressionTransformer(final SourceUnit unit) {
            this.unit = unit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return unit;
        }

        @Override
        public Expression transform(Expression expr) {
            if (expr instanceof StaticMethodCallExpression) {
                StaticMethodCallExpression orig = (StaticMethodCallExpression) expr;
                MethodNode target = (MethodNode) orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                if (target!=null) {
                    MethodCallExpression call = new MethodCallExpression(
                            new ClassExpression(orig.getOwnerType()),
                            orig.getMethod(),
                            orig.getArguments()
                    );
                    call.setMethodTarget(target);
                    return call;
                }
            }
            if (expr instanceof BinaryExpression) {
                Object[] list = (Object[]) expr.getNodeMetaData(BINARY_EXP_TARGET);
                if (list!=null) {
                    BinaryExpression bin = (BinaryExpression) expr;
                    Token operation = bin.getOperation();
                    boolean isAssignment = StaticTypeCheckingSupport.isAssignment(operation.getType());

                    MethodNode node = (MethodNode) list[0];
                    String name = (String) list[1];
                    Expression left = transform(bin.getLeftExpression());
                    Expression right = transform(bin.getRightExpression());
                    MethodCallExpression call = new MethodCallExpression(
                            left,
                            name,
                            new ArgumentListExpression(right)
                    );
                    call.setMethodTarget(node);
                    if (!isAssignment) return call;
                    // case of +=, -=, /=, ...
                    // the method represents the operation type only, and we must add an assignment
                    return new BinaryExpression(left, Token.newSymbol("=", operation.getStartLine(), operation.getStartColumn()), call);
                }
            }
            return super.transform(expr);
        }                
    }

}

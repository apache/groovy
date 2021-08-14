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
package org.codehaus.groovy.transform.tailrec;

import groovy.lang.Closure;
import groovy.transform.Memoized;
import groovy.transform.TailRecursive;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles generation of code for the @TailRecursive annotation.
 * <p>
 * It's doing its work in the earliest possible compile phase
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class TailRecursiveASTTransformation extends AbstractASTTransformation {
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);

        MethodNode method = DefaultGroovyMethods.asType(nodes[1], MethodNode.class);

        if (method.isAbstract()) {
            addError("Annotation " + TailRecursiveASTTransformation.getMY_TYPE_NAME() + " cannot be used for abstract methods.", method);
            return;

        }


        if (hasAnnotation(method, ClassHelper.make(Memoized.class))) {
            ClassNode memoizedClassNode = ClassHelper.make(Memoized.class);
            for (AnnotationNode annotationNode : method.getAnnotations()) {
                if (annotationNode.getClassNode().equals(MY_TYPE)) break;
                if (annotationNode.getClassNode().equals(memoizedClassNode)) {
                    addError("Annotation " + TailRecursiveASTTransformation.getMY_TYPE_NAME() + " must be placed before annotation @Memoized.", annotationNode);
                    return;

                }

            }

        }


        if (!hasRecursiveMethodCalls(method)) {
            AnnotationNode annotationNode = method.getAnnotations(ClassHelper.make(TailRecursive.class)).get(0);
            addError("No recursive calls detected. You must remove annotation " + TailRecursiveASTTransformation.getMY_TYPE_NAME() + ".", annotationNode);
            return;

        }


        transformToIteration(method, source);
        ensureAllRecursiveCallsHaveBeenTransformed(method);
    }

    private boolean hasAnnotation(MethodNode methodNode, ClassNode annotation) {
        List annots = methodNode.getAnnotations(annotation);
        return annots != null && annots.size() > 0;
    }

    private void transformToIteration(MethodNode method, SourceUnit source) {
        if (method.isVoidMethod()) {
            transformVoidMethodToIteration(method);
        } else {
            transformNonVoidMethodToIteration(method, source);
        }

    }

    private void transformVoidMethodToIteration(MethodNode method) {
        addError("Void methods are not supported by @TailRecursive yet.", method);
    }

    private void transformNonVoidMethodToIteration(MethodNode method, SourceUnit source) {
        addMissingDefaultReturnStatement(method);
        replaceReturnsWithTernariesToIfStatements(method);
        wrapMethodBodyWithWhileLoop(method);

        Map<String, Map> nameAndTypeMapping = name2VariableMappingFor(method);
        replaceAllAccessToParams(method, nameAndTypeMapping);
        addLocalVariablesForAllParameters(method, nameAndTypeMapping);//must happen after replacing access to params

        Map<Integer, Map> positionMapping = position2VariableMappingFor(method);
        replaceAllRecursiveReturnsWithIteration(method, positionMapping);
        repairVariableScopes(source, method);
    }

    private void repairVariableScopes(SourceUnit source, MethodNode method) {
        new VariableScopeVisitor(source).visitClass(method.getDeclaringClass());
    }

    @SuppressWarnings("Instanceof")
    private void replaceReturnsWithTernariesToIfStatements(MethodNode method) {
        Closure<Boolean> whenReturnWithTernary = new Closure<Boolean>(this, this) {
            public Boolean doCall(ASTNode node) {
                if (!(node instanceof ReturnStatement)) {
                    return false;
                }

                return ((ReturnStatement) node).getExpression() instanceof TernaryExpression;
            }

        };
        Closure<Statement> replaceWithIfStatement = new Closure<Statement>(this, this) {
            public Statement doCall(ReturnStatement statement) {
                return ternaryToIfStatement.convert(statement);
            }

        };
        StatementReplacer replacer = new StatementReplacer(whenReturnWithTernary, replaceWithIfStatement);
        replacer.replaceIn(method.getCode());

    }

    private void addLocalVariablesForAllParameters(MethodNode method, Map<String, Map> nameAndTypeMapping) {
        final BlockStatement code = DefaultGroovyMethods.asType(method.getCode(), BlockStatement.class);
        DefaultGroovyMethods.each(nameAndTypeMapping, new Closure<Void>(this, this) {
            public void doCall(String paramName, Map localNameAndType) {
                code.getStatements().add(0, AstHelper.createVariableDefinition((String) localNameAndType.get("name"), (ClassNode) localNameAndType.get("type"), new VariableExpression(paramName, (ClassNode) localNameAndType.get("type"))));
            }

        });
    }

    private void replaceAllAccessToParams(MethodNode method, Map<String, Map> nameAndTypeMapping) {
        new VariableAccessReplacer(nameAndTypeMapping).replaceIn(method.getCode());
    }

    public Map<String, Map> name2VariableMappingFor(MethodNode method) {
        final Map<String, Map> nameAndTypeMapping = new LinkedHashMap<String, Map>();
        DefaultGroovyMethods.each(method.getParameters(), new Closure<LinkedHashMap<String, Object>>(this, this) {
            public LinkedHashMap<String, Object> doCall(Parameter param) {
                String paramName = param.getName();
                ClassNode paramType = DefaultGroovyMethods.asType(param.getType(), ClassNode.class);
                String iterationVariableName = iterationVariableName(paramName);
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(2);
                map.put("name", iterationVariableName);
                map.put("type", paramType);
                return putAt0(nameAndTypeMapping, paramName, map);
            }

        });
        return nameAndTypeMapping;
    }

    public Map<Integer, Map> position2VariableMappingFor(MethodNode method) {
        final Map<Integer, Map> positionMapping = new LinkedHashMap<Integer, Map>();
        DefaultGroovyMethods.eachWithIndex(method.getParameters(), new Closure<LinkedHashMap<String, Object>>(this, this) {
            public LinkedHashMap<String, Object> doCall(Parameter param, int index) {
                String paramName = param.getName();
                ClassNode paramType = DefaultGroovyMethods.asType(param.getType(), ClassNode.class);
                String iterationVariableName = TailRecursiveASTTransformation.this.iterationVariableName(paramName);
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(2);
                map.put("name", iterationVariableName);
                map.put("type", paramType);
                return putAt0(positionMapping, index, map);
            }

        });
        return positionMapping;
    }

    private String iterationVariableName(String paramName) {
        return "_" + paramName + "_";
    }

    private void replaceAllRecursiveReturnsWithIteration(MethodNode method, Map positionMapping) {
        replaceRecursiveReturnsOutsideClosures(method, positionMapping);
        replaceRecursiveReturnsInsideClosures(method, positionMapping);
    }

    @SuppressWarnings("Instanceof")
    private void replaceRecursiveReturnsOutsideClosures(final MethodNode method, final Map<Integer, Map> positionMapping) {
        Closure<Boolean> whenRecursiveReturn = new Closure<Boolean>(this, this) {
            public Boolean doCall(Statement statement, boolean inClosure) {
                if (inClosure) return false;
                if (!(statement instanceof ReturnStatement)) {
                    return false;
                }

                Expression inner = ((ReturnStatement) statement).getExpression();
                if (!(inner instanceof MethodCallExpression) && !(inner instanceof StaticMethodCallExpression)) {
                    return false;
                }

                return isRecursiveIn(inner, method);
            }

        };
        Closure<Statement> replaceWithContinueBlock = new Closure<Statement>(this, this) {
            public Statement doCall(ReturnStatement statement) {
                return new ReturnStatementToIterationConverter().convert(statement, positionMapping);
            }

        };
        StatementReplacer replacer = new StatementReplacer(whenRecursiveReturn, replaceWithContinueBlock);
        replacer.replaceIn(method.getCode());
    }

    @SuppressWarnings("Instanceof")
    private void replaceRecursiveReturnsInsideClosures(final MethodNode method, final Map<Integer, Map> positionMapping) {
        Closure<Boolean> whenRecursiveReturn = new Closure<Boolean>(this, this) {
            public Boolean doCall(Statement statement, boolean inClosure) {
                if (!inClosure) return false;
                if (!(statement instanceof ReturnStatement)) {
                    return false;
                }

                Expression inner = ((ReturnStatement) statement).getExpression();
                if (!(inner instanceof MethodCallExpression) && !(inner instanceof StaticMethodCallExpression)) {
                    return false;
                }

                return isRecursiveIn(inner, method);
            }

        };
        Closure<Statement> replaceWithThrowLoopException = new Closure<Statement>(this, this) {
            public Statement doCall(ReturnStatement statement) {
                return new ReturnStatementToIterationConverter(AstHelper.recurByThrowStatement()).convert(statement, positionMapping);
            }

        };
        StatementReplacer replacer = new StatementReplacer(whenRecursiveReturn, replaceWithThrowLoopException);
        replacer.replaceIn(method.getCode());
    }

    private void wrapMethodBodyWithWhileLoop(MethodNode method) {
        new InWhileLoopWrapper().wrap(method);
    }

    private void addMissingDefaultReturnStatement(MethodNode method) {
        new ReturnAdder().visitMethod(method);
        new ReturnAdderForClosures().visitMethod(method);
    }

    private void ensureAllRecursiveCallsHaveBeenTransformed(MethodNode method) {
        List<Expression> remainingRecursiveCalls = new CollectRecursiveCalls().collect(method);
        for (Expression expression : remainingRecursiveCalls) {
            addError("Recursive call could not be transformed by @TailRecursive. Maybe it's not a tail call.", expression);
        }

    }

    private boolean hasRecursiveMethodCalls(MethodNode method) {
        return hasRecursiveCalls.test(method);
    }

    @SuppressWarnings("Instanceof")
    private boolean isRecursiveIn(Expression methodCall, MethodNode method) {
        if (methodCall instanceof MethodCallExpression)
            return new RecursivenessTester().isRecursive(method, (MethodCallExpression) methodCall);
        if (methodCall instanceof StaticMethodCallExpression)
            return new RecursivenessTester().isRecursive(method, (StaticMethodCallExpression) methodCall);
        return false;
    }

    public static String getMY_TYPE_NAME() {
        return MY_TYPE_NAME;
    }

    private static final Class MY_CLASS = TailRecursive.class;
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private final HasRecursiveCalls hasRecursiveCalls = new HasRecursiveCalls();
    private final TernaryToIfStatementConverter ternaryToIfStatement = new TernaryToIfStatementConverter();

    private static <K, V, Value extends V> Value putAt0(Map<K, V> propOwner, K key, Value value) {
        propOwner.put(key, value);
        return value;
    }
}

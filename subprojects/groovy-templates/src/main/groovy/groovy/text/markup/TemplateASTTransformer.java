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
package groovy.text.markup;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>An AST transformation which adapts the AST generated from a script as a class extending {@link groovy.text.markup.BaseTemplate}.</p>
 *
 * <p>This transformation:</p>
 *
 * <ul>
 *     <li>Sets the super class of the script to the selected {@link TemplateConfiguration#getBaseTemplateClass() template class}</li>
 *     <li>Calls the {@link groovy.text.markup.MarkupBuilderCodeTransformer} on the "run" method</li>
 *     <li>Creates the appropriate constructor</li>
 * </ul>
 */
class TemplateASTTransformer extends CompilationCustomizer {

    private static final ClassNode TEMPLATECONFIG_CLASSNODE = ClassHelper.make(TemplateConfiguration.class);
    private final TemplateConfiguration config;

    public TemplateASTTransformer(TemplateConfiguration config) {
        super(CompilePhase.SEMANTIC_ANALYSIS);
        this.config = config;
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        if (classNode.isScriptBody()) {
            classNode.setSuperClass(ClassHelper.make(config.getBaseTemplateClass()));
            createConstructor(classNode);
            transformRunMethod(classNode, source);
            VariableScopeVisitor visitor = new VariableScopeVisitor(source);
            visitor.visitClass(classNode);
        }
    }

    private void transformRunMethod(final ClassNode classNode, final SourceUnit source) {
        MethodNode runMethod = classNode.getDeclaredMethod("run", Parameter.EMPTY_ARRAY);
        Statement code = runMethod.getCode();
        MarkupBuilderCodeTransformer transformer = new MarkupBuilderCodeTransformer(source, classNode, config.isAutoEscape());
        code.accept(transformer);
    }

    private void createConstructor(final ClassNode classNode) {
        Parameter[] params = new Parameter[]{
                new Parameter(MarkupTemplateEngine.MARKUPTEMPLATEENGINE_CLASSNODE, "engine"),
                new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), "model"),
                new Parameter(ClassHelper.MAP_TYPE.getPlainNodeReference(), "modelTypes"),
                new Parameter(TEMPLATECONFIG_CLASSNODE, "tplConfig")
        };
        List<Expression> vars = new LinkedList<Expression>();
        for (Parameter param : params) {
            vars.add(new VariableExpression(param));
        }
        ExpressionStatement body = new ExpressionStatement(
                new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(vars)));
        ConstructorNode ctor = new ConstructorNode(Opcodes.ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, body);
        classNode.addConstructor(ctor);
    }
}

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
package groovy.transform.builder;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StubberSupport;

import java.util.Iterator;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Joint-compilation stubber for {@link Builder}, covering the two most
 * common shapes:
 * <ul>
 *   <li>{@link DefaultStrategy} on a class-level annotation: emits the
 *       inner builder class with fluent setters and a {@code build()}
 *       method, plus a static {@code Foo.builder()} factory on the
 *       buildee.</li>
 *   <li>{@link SimpleStrategy} with a non-default {@code prefix}: emits
 *       chained {@code Foo prefixName(T value)} setters on the buildee
 *       itself. Skipped silently for the default {@code prefix = "set"}
 *       because the stubber's {@code Foo setX(value)} would clash with
 *       the void {@code setX(value)} that the stub generator's Verifier
 *       sub-pass also emits for properties — Java doesn't allow two
 *       methods with the same name and parameters but different return
 *       types.</li>
 * </ul>
 *
 * <p>Other strategies are out of scope for this spike pass:
 * <ul>
 *   <li>{@link InitializerStrategy} — typed-builder pattern with generic
 *       parameters; structurally larger than the spike's scope.</li>
 *   <li>{@link ExternalStrategy} — annotates an explicit builder class
 *       rather than the buildee; the stubber would fire on the explicit
 *       builder, with no impact on the buildee's stub.</li>
 * </ul>
 *
 * <p>Method/constructor targets, the {@code forClass} attribute, and
 * {@code includeSuperProperties} are also out of scope. The stubber walks
 * directly-declared properties only, mirroring the same Tier 1 limitation
 * other property-walking stubbers carry.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class BuilderASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Builder.class);
    private static final String DEFAULT_STRATEGY = DefaultStrategy.class.getName();
    private static final String SIMPLE_STRATEGY = SimpleStrategy.class.getName();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode buildee) || buildee.isInterface()) return;

        // forClass = ExternalStrategy-style usage; out of scope.
        if (annotation.getMember("forClass") != null) return;

        Expression strategyExpr = annotation.getMember("builderStrategy");
        String strategyName = DEFAULT_STRATEGY;
        if (strategyExpr instanceof ClassExpression) {
            strategyName = ((ClassExpression) strategyExpr).getType().getName();
        }

        if (DEFAULT_STRATEGY.equals(strategyName)) {
            stubDefaultStrategy(buildee, annotation);
        } else if (SIMPLE_STRATEGY.equals(strategyName)) {
            stubSimpleStrategy(buildee, annotation);
        }
        // Other strategies: silent skip.
    }

    private void stubDefaultStrategy(ClassNode buildee, AnnotationNode annotation) {
        String prefix = getMemberStringValue(annotation, "prefix", "");
        String builderClassName = getMemberStringValue(annotation, "builderClassName",
                buildee.getNameWithoutPackage() + "Builder");
        String buildMethodName = getMemberStringValue(annotation, "buildMethodName", "build");
        String builderMethodName = getMemberStringValue(annotation, "builderMethodName", "builder");

        String fullBuilderName = buildee.getName() + "$" + builderClassName;

        // Skip if a user-declared inner class with that name already exists.
        for (Iterator<InnerClassNode> it = buildee.getInnerClasses(); it.hasNext(); ) {
            if (fullBuilderName.equals(it.next().getName())) return;
        }

        // Inner builder class. The InnerClassNode constructor auto-registers
        // it in {buildee.innerClasses} — needed for the stub generator to
        // emit nested classes inline. We add to {module.getClasses()} and
        // set the module link manually rather than calling
        // {module.addClass(...)}: the latter also registers in the
        // {CompileUnit}'s global by-name map, which would clash when the
        // full transform later calls {addGeneratedInnerClass} for its own
        // (untagged) replacement.
        InnerClassNode builder = new InnerClassNode(buildee, fullBuilderName,
                ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE);
        StubberSupport.tagAsStub(builder);
        buildee.getModule().getClasses().add(builder);
        builder.setModule(buildee.getModule());

        // Fluent setters on the builder, one per directly-declared property.
        for (PropertyNode property : buildee.getProperties()) {
            if (property.isStatic()) continue;
            String setterName = setterNameFor(prefix, property.getName());
            Parameter[] paramArr = params(param(newClass(property.getType()), property.getName()));
            StubberSupport.addStubMethod(builder, setterName, ACC_PUBLIC,
                    builder, paramArr, ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }

        // Foo build() on the builder.
        StubberSupport.addStubMethod(builder, buildMethodName, ACC_PUBLIC,
                newClass(buildee), Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, returnS(constX(null)));

        // static Builder builder() on the buildee.
        if (buildee.getDeclaredMethod(builderMethodName, Parameter.EMPTY_ARRAY) == null) {
            StubberSupport.addStubMethod(buildee, builderMethodName, ACC_PUBLIC | ACC_STATIC,
                    builder, Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY, returnS(constX(null)));
        }
    }

    private void stubSimpleStrategy(ClassNode buildee, AnnotationNode annotation) {
        // SimpleStrategy default prefix is "set", which would collide with
        // the void setX(value) auto-generated for properties. Skip silently;
        // a non-default prefix sidesteps the conflict.
        String prefix = getMemberStringValue(annotation, "prefix", "set");
        if ("set".equals(prefix)) return;

        for (PropertyNode property : buildee.getProperties()) {
            if (property.isStatic()) continue;
            String setterName = setterNameFor(prefix, property.getName());
            Parameter[] paramArr = params(param(newClass(property.getType()), property.getName()));
            if (buildee.getDeclaredMethod(setterName, paramArr) == null) {
                StubberSupport.addStubMethod(buildee, setterName, ACC_PUBLIC,
                        newClass(buildee), paramArr,
                        ClassNode.EMPTY_ARRAY, returnS(constX(null)));
            }
        }
    }

    private static String setterNameFor(String prefix, String fieldName) {
        return prefix.isEmpty() ? fieldName
                : prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}

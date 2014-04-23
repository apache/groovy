/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.builder;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.BuilderASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.codehaus.groovy.transform.BuilderASTTransformation.NO_EXCEPTIONS;
import static org.codehaus.groovy.transform.BuilderASTTransformation.NO_PARAMS;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class DefaultStrategy extends BuilderASTTransformation.AbstractBuilderStrategy {
    private static final Expression DEFAULT_INITIAL_VALUE = null;

    public void build(BuilderASTTransformation transform, ClassNode buildee, AnnotationNode anno, List<String> excludes, List<String> includes) {
        String prefix = transform.getMemberStringValue(anno, "prefix", "");
        if (unsupportedAttribute(transform, anno, "forClass")) return;
        final int visibility = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC;
        String builderClassName = transform.getMemberStringValue(anno, "builderClassName", buildee.getName() + "Builder");
        final String fullName = buildee.getName() + "$" + builderClassName;
        ClassNode builder = new InnerClassNode(buildee, fullName, visibility, ClassHelper.OBJECT_TYPE);
        buildee.getModule().addClass(builder);
        buildee.addMethod(createBuilderMethod(transform, anno, builder));
        List<FieldNode> fields = getInstancePropertyFields(buildee);
        List<FieldNode> filteredFields = selectFieldsFromExistingClass(fields, includes, excludes);
        for (FieldNode fieldNode : filteredFields) {
            builder.addField(createFieldCopy(buildee, fieldNode));
            builder.addMethod(createBuilderMethodForField(builder, fieldNode, prefix));
        }
        builder.addMethod(createBuildMethod(transform, anno, buildee, filteredFields));
    }

    private static MethodNode createBuilderMethod(BuilderASTTransformation transform, AnnotationNode anno, ClassNode builder) {
        String builderMethodName = transform.getMemberStringValue(anno, "builderMethodName", "builder");
        final BlockStatement body = new BlockStatement();
        body.addStatement(returnS(ctorX(builder)));
        final int visibility = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC;
        return new MethodNode(builderMethodName, visibility, builder, NO_PARAMS, NO_EXCEPTIONS, body);
    }

    private static MethodNode createBuildMethod(BuilderASTTransformation transform, AnnotationNode anno, ClassNode buildee, List<FieldNode> fields) {
        String buildMethodName = transform.getMemberStringValue(anno, "buildMethodName", "build");
        final BlockStatement body = new BlockStatement();
        body.addStatement(returnS(initializeInstance(buildee, fields, body)));
        return new MethodNode(buildMethodName, ACC_PUBLIC, buildee, NO_PARAMS, NO_EXCEPTIONS, body);
    }

    private MethodNode createBuilderMethodForField(ClassNode builder, FieldNode fieldNode, String prefix) {
        String fieldName = fieldNode.getName();
        String setterName = getSetterName(prefix, fieldName);
        return new MethodNode(setterName, ACC_PUBLIC, newClass(builder), params(param(fieldNode.getType(), fieldName)), NO_EXCEPTIONS, block(
                stmt(assignX(propX(varX("this"), constX(fieldName)), varX(fieldName))),
                returnS(varX("this", builder))
        ));
    }

    private static FieldNode createFieldCopy(ClassNode buildee, FieldNode fNode) {
        return new FieldNode(fNode.getName(), fNode.getModifiers(), ClassHelper.make(fNode.getType().getName()), buildee, DEFAULT_INITIAL_VALUE);
    }

    private static List<FieldNode> selectFieldsFromExistingClass(List<FieldNode> fieldNodes, List<String> includes, List<String> excludes) {
        List<FieldNode> fields = new ArrayList<FieldNode>();
        for (FieldNode fNode : fieldNodes) {
            if (AbstractASTTransformation.shouldSkip(fNode.getName(), excludes, includes)) continue;
            fields.add(fNode);
        }
        return fields;
    }

    private static Expression initializeInstance(ClassNode buildee, List<FieldNode> fields, BlockStatement body) {
        Expression instance = varX("_the" + buildee.getNameWithoutPackage(), buildee);
        body.addStatement(declS(instance, ctorX(buildee)));
        for (FieldNode field : fields) {
            body.addStatement(stmt(assignX(propX(instance, field.getName()), varX(field))));
        }
        return instance;
    }
}

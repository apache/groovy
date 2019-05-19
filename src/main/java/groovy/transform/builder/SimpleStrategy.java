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

import groovy.transform.Undefined;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.BuilderASTTransformation;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.codehaus.groovy.transform.AbstractASTTransformation.getMemberStringValue;
import static org.codehaus.groovy.transform.BuilderASTTransformation.NO_EXCEPTIONS;

/**
 * This strategy is used with the {@link Builder} AST transform to modify your Groovy objects so that the
 * setter methods for properties return the original object, thus allowing chained usage of the setters.
 *
 * You use it as follows:
 * <pre class="groovyTestCase">
 * import groovy.transform.builder.*
 *
 * {@code @Builder}(builderStrategy=SimpleStrategy)
 * class Person {
 *     String firstName
 *     String lastName
 *     int age
 * }
 * def person = new Person().setFirstName("Robert").setLastName("Lewandowski").setAge(21)
 * assert person.firstName == "Robert"
 * assert person.lastName == "Lewandowski"
 * assert person.age == 21
 * </pre>
 * The {@code prefix} annotation attribute can be used to create setters with a different naming convention, e.g. with the prefix set to the empty String, you would use your setters as follows:
 * <pre>
 * def p1 = new Person().firstName("Robert").lastName("Lewandowski").age(21)
 * </pre>
 * or using a prefix of 'with':
 * <pre>
 * def p2 = new Person().withFirstName("Robert").withLastName("Lewandowski").withAge(21)
 * </pre>
 * When using the default prefix of "set", Groovy's normal setters will be replaced by the chained versions. When using
 * a custom prefix, Groovy's unchained setters will still be available for use in the normal unchained fashion.
 *
 * The 'useSetters' annotation attribute can be used for writable properties as per the {@code Builder} transform documentation.
 * The other annotation attributes for the {@code @Builder} transform for configuring the building process aren't applicable for this strategy.
 */
public class SimpleStrategy extends BuilderASTTransformation.AbstractBuilderStrategy {
    public void build(BuilderASTTransformation transform, AnnotatedNode annotatedNode, AnnotationNode anno) {
        if (!(annotatedNode instanceof ClassNode)) {
            transform.addError("Error during " + BuilderASTTransformation.MY_TYPE_NAME + " processing: building for " +
                    annotatedNode.getClass().getSimpleName() + " not supported by " + getClass().getSimpleName(), annotatedNode);
            return;
        }
        ClassNode buildee = (ClassNode) annotatedNode;
        if (unsupportedAttribute(transform, anno, "builderClassName")) return;
        if (unsupportedAttribute(transform, anno, "buildMethodName")) return;
        if (unsupportedAttribute(transform, anno, "builderMethodName")) return;
        if (unsupportedAttribute(transform, anno, "forClass")) return;
        if (unsupportedAttribute(transform, anno, "includeSuperProperties")) return;
        if (unsupportedAttribute(transform, anno, "allProperties")) return;
        if (unsupportedAttribute(transform, anno, "force")) return;
        boolean useSetters = transform.memberHasValue(anno, "useSetters", true);
        boolean allNames = transform.memberHasValue(anno, "allNames", true);

        List<String> excludes = new ArrayList<String>();
        List<String> includes = new ArrayList<String>();
        includes.add(Undefined.STRING);
        if (!getIncludeExclude(transform, anno, buildee, excludes, includes)) return;
        if (includes.size() == 1 && Undefined.isUndefined(includes.get(0))) includes = null;
        String prefix = getMemberStringValue(anno, "prefix", "set");
        List<FieldNode> fields = getFields(transform, anno, buildee);
        if (includes != null) {
            for (String name : includes) {
                checkKnownField(transform, anno, name, fields);
            }
        }
        for (FieldNode field : fields) {
            String fieldName = field.getName();
            if (!AbstractASTTransformation.shouldSkipUndefinedAware(fieldName, excludes, includes, allNames)) {
                String methodName = getSetterName(prefix, fieldName);
                Parameter parameter = param(field.getType(), fieldName);
                addGeneratedMethod(buildee, methodName, Opcodes.ACC_PUBLIC, newClass(buildee), params(parameter), NO_EXCEPTIONS, block(
                        stmt(useSetters && !field.isFinal()
                                ? callThisX(getSetterName("set", fieldName), varX(parameter))
                                : assignX(fieldX(field), varX(parameter))
                        ),
                        returnS(varX("this")))
                );
            }
        }
    }

    @Override
    protected List<FieldNode> getFields(BuilderASTTransformation transform, AnnotationNode anno, ClassNode buildee) {
        return getInstancePropertyFields(buildee);
    }
}

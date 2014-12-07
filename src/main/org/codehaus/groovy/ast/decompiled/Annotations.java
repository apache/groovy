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

package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.*;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Gromov
 */
class Annotations {
    static AnnotationNode createAnnotationNode(AnnotationStub annotation, AsmReferenceResolver resolver) {
        AnnotationNode node = new AnnotationNode(resolver.resolveType(Type.getType(annotation.className)));
        for (Map.Entry<String, Object> entry : annotation.members.entrySet()) {
            node.addMember(entry.getKey(), annotationValueToExpression(entry.getValue(), resolver));
        }
        return node;
    }

    private static Expression annotationValueToExpression(Object value, AsmReferenceResolver resolver) {
        if (value instanceof TypeWrapper) {
            return new ClassExpression(resolver.resolveType(Type.getType(((TypeWrapper) value).desc)));
        }

        if (value instanceof EnumConstantWrapper) {
            EnumConstantWrapper wrapper = (EnumConstantWrapper) value;
            return new PropertyExpression(new ClassExpression(resolver.resolveType(Type.getType(wrapper.enumDesc))), wrapper.constant);
        }

        if (value instanceof AnnotationStub) {
            return new AnnotationConstantExpression(createAnnotationNode((AnnotationStub) value, resolver));
        }

        if (value != null && value.getClass().isArray()) {
            ListExpression elementExprs = new ListExpression();
            int len = Array.getLength(value);
            for (int i = 0; i != len; ++i) {
                elementExprs.addExpression(annotationValueToExpression(Array.get(value, i), resolver));
            }
            return elementExprs;
        }

        if (value instanceof List) {
            ListExpression elementExprs = new ListExpression();
            for (Object o : (List) value) {
                elementExprs.addExpression(annotationValueToExpression(o, resolver));
            }
            return elementExprs;
        }

        return new ConstantExpression(value);
    }
}

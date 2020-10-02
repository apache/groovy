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
package groovy.transform.options;

import groovy.lang.GroovyClassLoader;
import groovy.transform.PropertyOptions;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;

/**
 * Used to provide custom property handling when getting, setting or initializing properties.
 *
 * @since 2.5.0
 */
@Incubating
public abstract class PropertyHandler {
    private static final Class<? extends Annotation> PROPERTY_OPTIONS_CLASS = PropertyOptions.class;
    public static final ClassNode PROPERTY_OPTIONS_TYPE = makeWithoutCaching(PROPERTY_OPTIONS_CLASS, false);

    public abstract boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno);

    public boolean validateProperties(final AbstractASTTransformation xform, final BlockStatement body, final ClassNode cNode, final List<PropertyNode> props) {
        return true;
    }

    /**
     * Create a statement that will initialize the property including any defensive copying. Null if no statement should be added.
     *
     * @param xform the transform being processed
     * @param anno the '@ImmutableBase' annotation node
     * @param cNode the classnode containing the property
     * @param pNode the property node to initialize
     * @param namedArgMap an "args" Map if the property value should come from a named arg map or null if not
     */
    public abstract Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyNode pNode, Parameter namedArgMap);

    /**
     * Create the getter block used when reading the property including any defensive copying.
     *
     *  @param pNode the property node
     */
    public Statement createPropGetter(final PropertyNode pNode) {
        return pNode.getGetterBlock();
    }

    /**
     * Create the setter block used when setting the property. Can be null for read-only properties.
     *
     *  @param pNode the property node
     */
    public Statement createPropSetter(final PropertyNode pNode) {
        return pNode.getSetterBlock();
    }

    protected boolean isValidAttribute(final AbstractASTTransformation xform, final AnnotationNode anno, final String memberName) {
        if (xform.getMemberValue(anno, memberName) != null) {
            xform.addError("Error during " + xform.getAnnotationName() + " processing: Annotation attribute '" + memberName +
                    "' not supported for property handler " + getClass().getSimpleName(), anno);
            return false;
        }
        return true;
    }

    public static PropertyHandler createPropertyHandler(final AbstractASTTransformation xform, final GroovyClassLoader loader, final ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(PROPERTY_OPTIONS_TYPE);
        AnnotationNode anno = annotations.isEmpty() ? null : annotations.get(0);
        if (anno == null) return new DefaultPropertyHandler();

        ClassNode handlerClass = xform.getMemberClassValue(anno, "propertyHandler", ClassHelper.make(DefaultPropertyHandler.class));
        if (handlerClass == null) {
            xform.addError("Couldn't determine propertyHandler class", anno);
            return null;
        }

        String className = handlerClass.getName();
        try {
            Object instance = loader.loadClass(className).getDeclaredConstructor().newInstance();
            if (!PropertyHandler.class.isAssignableFrom(instance.getClass())) {
                xform.addError("The propertyHandler class '" + handlerClass.getName() + "' on " + xform.getAnnotationName() + " is not a propertyHandler", anno);
                return null;
            }
            return (PropertyHandler) instance;
        } catch (Exception e) {
            xform.addError("Can't load propertyHandler '" + className + "' " + e, anno);
            return null;
        }
    }
}

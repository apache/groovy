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
 * <p>
 * Subclasses are plugged in via the {@code propertyHandler} attribute on the
 * {@link PropertyOptions} annotation. Implementations must declare a public no-argument
 * constructor; the handler is instantiated via {@link #createPropertyHandler} using
 * reflection at compile time.
 * <p>
 * During each transform, methods are invoked in a fixed order:
 * {@link #validateAttributes} first; if it returns {@code true}, then
 * {@link #validateProperties}; if that also returns {@code true}, then
 * {@link #createPropInit}, {@link #createPropGetter}, and {@link #createPropSetter}
 * are called per property as required by the host transform. Returning {@code false}
 * from a validation method short-circuits processing.
 *
 * @since 2.5.0
 */
public abstract class PropertyHandler {
    private static final Class<? extends Annotation> PROPERTY_OPTIONS_CLASS = PropertyOptions.class;

    /**
     * Class node for {@link PropertyOptions}.
     */
    public static final ClassNode PROPERTY_OPTIONS_TYPE = makeWithoutCaching(PROPERTY_OPTIONS_CLASS, false);

    /**
     * Validates annotation attributes supported by this handler.
     *
     * @param xform the active transform
     * @param anno the property options annotation
     * @return {@code true} if validation succeeds
     */
    public abstract boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno);

    /**
     * Validates the properties selected for processing.
     *
     * @param xform the active transform
     * @param body the statement block being generated
     * @param cNode the owning class
     * @param props the candidate properties
     * @return {@code true} if validation succeeds
     */
    public boolean validateProperties(final AbstractASTTransformation xform, final BlockStatement body, final ClassNode cNode, final List<PropertyNode> props) {
        return true;
    }

    /**
     * Create a statement that will initialize the property including any defensive copying.
     * Return {@code null} to indicate that no initialization statement should be emitted
     * for this property; this is distinct from returning an empty block and skips the
     * corresponding entry in the generated constructor body altogether.
     *
     * @param xform the transform being processed
     * @param anno the annotation node
     * @param cNode the classnode containing the property
     * @param pNode the property node to initialize
     * @param namedArgsMap an "args" Map if the property value should come from a named arg map or null if not
     */
    public abstract Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap);

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

    /**
     * Helper for use from {@link #validateAttributes}: confirms that the named attribute is
     * <em>absent</em> from the host annotation. Despite the positive name, this returns
     * {@code false} <em>and</em> records a compile error when the attribute is set, so
     * subclasses can chain calls in the style {@code return isValidAttribute(xform, anno, "useSuper");}
     * to reject attributes that the host transform supports but this handler does not.
     *
     * @param xform the active transform
     * @param anno the annotation being processed
     * @param memberName the attribute name expected to be absent
     * @return {@code true} if the attribute is absent (i.e. valid for this handler);
     *         {@code false} if present (a compile error is added as a side effect)
     */
    protected boolean isValidAttribute(final AbstractASTTransformation xform, final AnnotationNode anno, final String memberName) {
        if (anno.getMember(memberName) != null) {
            xform.addError("Error during " + xform.getAnnotationName() + " processing: Annotation attribute '" + memberName +
                    "' not supported for property handler " + getClass().getSimpleName(), anno);
            return false;
        }
        return true;
    }

    /**
     * Creates the property handler configured for the supplied class. If the class carries
     * a {@link PropertyOptions} annotation, the {@code propertyHandler} attribute is read
     * and the named handler class is instantiated via its public no-argument constructor;
     * otherwise a {@link DefaultPropertyHandler} is returned. A compile error is recorded
     * (and {@code null} returned) if the handler cannot be loaded, lacks a no-arg
     * constructor, or is not a {@code PropertyHandler} subtype.
     *
     * @param xform the active transform
     * @param loader the class loader used to instantiate custom handlers
     * @param cNode the class being transformed
     * @return the configured property handler, or {@code null} if one could not be created
     */
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

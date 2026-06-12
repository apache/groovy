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
package groovy.transform.options

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.plusX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Locks in the public contract of {@link PropertyHandler}: that user-supplied
 * handlers wired in via {@code @PropertyOptions(propertyHandler = …)} are
 * loaded, validated and consulted at compile time.
 */
final class PropertyHandlerTest {

    @Test
    void testCustomHandlerControlsPropertyInitialisation() {
        assertScript '''
            import groovy.transform.PropertyOptions
            import groovy.transform.TupleConstructor
            import groovy.transform.options.PropertyHandlerTest.PrefixingPropertyHandler

            @PropertyOptions(propertyHandler = PrefixingPropertyHandler)
            @TupleConstructor
            class Foo {
                String name
            }

            assert new Foo('hello').name == 'X_hello'
        '''
    }

    @Test
    void testHandlerValidateAttributesCanRejectAnAttribute() {
        // Exercises a list-valued attribute (`includes = [...]`)
        def err = shouldFail '''
            import groovy.transform.PropertyOptions
            import groovy.transform.TupleConstructor
            import groovy.transform.options.PropertyHandlerTest.RejectsIncludesPropertyHandler

            @PropertyOptions(propertyHandler = RejectsIncludesPropertyHandler)
            @TupleConstructor(includes = ['name'])
            class Bar {
                String name
                String tag
            }
        '''
        assert err.message.contains("Annotation attribute 'includes' not supported for property handler RejectsIncludesPropertyHandler")
    }

    @Test
    void testNoPropertyOptionsFallsBackToDefaultPropertyHandler() {
        assertScript '''
            import groovy.transform.TupleConstructor

            @TupleConstructor
            class Baz {
                String name
            }

            // unchanged value confirms DefaultPropertyHandler was used, not PrefixingPropertyHandler
            assert new Baz('plain').name == 'plain'
        '''
    }

    /**
     * Custom handler that rewrites every property initialisation to prefix the
     * incoming constructor argument with {@code 'X_'}, demonstrating that
     * {@link PropertyHandler#createPropInit} fully controls the emitted code.
     */
    static class PrefixingPropertyHandler extends PropertyHandler {
        @Override
        boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno) {
            true
        }

        @Override
        Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno,
                                 ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
            def name = pNode.name
            // Generated: this.<name> = 'X_' + <name>
            return assignS(propX(varX('this'), name), plusX(constX('X_'), varX(name)))
        }
    }

    /**
     * Custom handler that demonstrates rejecting a host-transform attribute via
     * {@link PropertyHandler#isValidAttribute}; combining this handler with the
     * {@code includes} attribute on {@code @TupleConstructor} produces a compile
     * error rather than silently ignoring the attribute. The list-valued shape
     * also exercises that detection is not restricted to scalar constant attributes.
     */
    static class RejectsIncludesPropertyHandler extends PropertyHandler {
        @Override
        boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno) {
            isValidAttribute(xform, anno, 'includes')
        }

        @Override
        Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno,
                                 ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
            null // never reached: validateAttributes returns false above
        }
    }
}

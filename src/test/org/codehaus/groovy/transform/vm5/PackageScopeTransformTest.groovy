/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

import java.lang.reflect.Modifier

/**
 * @author Paul King
 */
class PackageScopeTransformTest extends GroovyShellTestCase {

    void testImmutable() {
        def objects = evaluate("""
            class Control {
                String x
            }
            @PackageScope class Foo {
                String x
            }
            class Bar {
                @PackageScope String x
            }
            [new Control(), new Foo(), new Bar()]
        """)
        objects*.class.each { c ->
            def methodNames = c.methods.name
            if (c.name == 'Control') {
                assert methodNames.contains('getX')
                assert methodNames.contains('setX')
            } else {
                assert !methodNames.contains('getX')
                assert !methodNames.contains('setX')
            }
            def xField = c.declaredFields.find{ it.name == 'x' }
            assert xField
            if (c.name == 'Control') {
                assert Modifier.isPrivate(xField.modifiers)
            } else {
                assert !Modifier.isPrivate(xField.modifiers)
                assert !Modifier.isPublic(xField.modifiers)
                assert !Modifier.isProtected(xField.modifiers)
            }
        }
    }
}
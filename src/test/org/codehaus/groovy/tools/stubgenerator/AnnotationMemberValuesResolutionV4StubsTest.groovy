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
package org.codehaus.groovy.tools.stubgenerator

/**
 * Test that FQN appears in generated stub when an annotation node
 *  is used as an annotation member value.
 */
class AnnotationMemberValuesResolutionV4StubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'foo/MainAnno4768.java': '''
                package foo;
                
                import java.lang.annotation.*;
                import static java.lang.annotation.ElementType.*;
                import static java.lang.annotation.RetentionPolicy.*;

                @Retention(RUNTIME) @Target({TYPE})
                public @interface MainAnno4768 {
                    OtherAnno4768 other();
                }
            ''',
            'foo/OtherAnno4768.java': '''
                package foo;

                import java.lang.annotation.*;
                import static java.lang.annotation.ElementType.*;
                import static java.lang.annotation.RetentionPolicy.*;

                @Retention(RUNTIME) @Target({METHOD, FIELD})
                public @interface OtherAnno4768 {
                    String name() default "";
                }
            ''',
            'Main4768.groovy': '''
                import foo.*

                @MainAnno4768(other = @OtherAnno4768(name='baz'))
                class Main4768 {}
            '''
        ]
    }

    void verifyStubs() {
        classes['Main4768'].with {
            assert annotations[0].getProperty('other').getProperty('name').value == 'baz'
        }
    }
}

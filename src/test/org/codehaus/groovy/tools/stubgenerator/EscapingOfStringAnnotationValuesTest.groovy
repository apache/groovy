/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.tools.stubgenerator

/**
 * Test case for GROOVY-4470, GROOVY-4604 and GROOVY-4601
 *
 * @author Peter Niederwieser
 * @author Guillaume Laforge
 */
class EscapingOfStringAnnotationValuesTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'StringAnn.java': '''
                    import java.lang.annotation.*;
                    @Retention(RetentionPolicy.RUNTIME)
                    public @interface StringAnn {
                        String str();
                    }
                ''',

                'StringAnnUsage.groovy': '''
                    |@StringAnn(str = """Now that's what
                    |I
                    |\tcall an
                    |  "unescaped"
                    |String!""")
                    |class StringAnnUsage {}
                '''.stripMargin('|'),

                'StringAnnoUser.groovy': '''
                    @StringAnn(str = 'single quote string with "double quote string"')
                    class StringAnnoUser {}
                '''
        ]
    }

    void verifyStubs() {
        def ann1 = classes['StringAnnUsage'].annotations[0]
        assert "\"Now that's what\\nI\\n\tcall an\\n  \\\"unescaped\\\"\\nString!\"" == ann1.getNamedParameter("str")

        def ann2 = classes['StringAnnoUser'].annotations[0]
        assert '"single quote string with \\"double quote string\\""' == ann2.getNamedParameter("str")
    }
}
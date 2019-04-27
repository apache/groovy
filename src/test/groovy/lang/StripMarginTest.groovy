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
package groovy.lang

class StripMarginTest extends GroovyTestCase {
    void testStripMarginOnSingleLineString() {
        def expected = "the quick brown fox jumps over the lazy dog"
        def actual = "     |the quick brown fox jumps over the lazy dog".stripMargin()
        assert expected == actual

        actual = "     ||the quick brown fox jumps over the lazy dog".stripMargin()
        assert "|" + expected == actual

        actual = "     #the quick brown fox jumps over the lazy dog".stripMargin('#')
        assert expected == actual
    }

    void testStripMarginOnMultiLineString() {
        def expected = "the quick brown fox\njumps over the lazy dog"
        def actual = """     |the quick brown fox
     |jumps over the lazy dog""".stripMargin()
        assert expected == actual

        actual = """     #the quick brown fox
     #jumps over the lazy dog""".stripMargin('#')
        assert expected == actual

        expected = "the quick brown fox\n|jumps over the lazy dog"
        actual = """     |the quick brown fox
     ||jumps over the lazy dog""".stripMargin()
        assert expected == actual
    }

    void testStripIndent() {
        def actual   = """
                return 'foo'
            }

            def method() {
                return 'bar'
            }
        """.stripIndent()

        def expected = """
    return 'foo'
}

def method() {
    return 'bar'
}
"""

        assert expected == actual
    }

    void testStripIndentWithFirstLineBackslash() {
        def actual   = """\
                return 'foo'
            }

            def method() {
                return 'bar'
            }
        """.stripIndent()
        
        def expected = """\
    return 'foo'
}

def method() {
    return 'bar'
}
"""

        assert expected == actual
    }
}

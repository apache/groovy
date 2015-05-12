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
package groovy.xml

class MixedMarkupTestSupport {

    private static final mixedXml = '''
<p>Please read the <a href="index.html">Home</a> page</p>
'''
    static void checkMixedMarkup(Closure getRoot) {
        def root = getRoot(mixedXml)
        assert root != null
        def children = root.children()
        if (isSlurper(root)) {
            assert children.size() == 1
            assert children[0].name() == 'a'
            assert children[0].localText() == ['Home']
        } else {
            assert children.size() == 3
            assert children[1].name() == 'a'
            assert children[1].localText() == ['Home']
            if (isParser(root)) {
                assert children[2] == ' page'
            } else {
                assert children[2].text() == ' page'
            }
        }
        assert root.text() == 'Please read the Home page'
        assert root.localText() == ['Please read the ', ' page']
    }

    static void checkMixedMarkupText(Closure getRoot) {
        def root = getRoot("<foo a='xyz'><person>James</person><person>Bob</person>someText</foo>")
        assert root != null
        assert root.text() == 'JamesBobsomeText'
        assert root.localText() == ['someText']
        // below are legacy mechanisms for accessing localText()
        if (isSlurper(root)) {
            assert root[0].children()[-1] == 'someText'
        } else if (isParser(root)) {
            assert root.children()[-1] == 'someText'
        } else {
            assert root.children()[-1].nodeValue == 'someText'
        }
    }

    private static boolean isSlurper(node) {
        return node.getClass().name.contains('slurper')
    }

    private static boolean isParser(node) {
        return (node instanceof groovy.util.Node)
    }
}

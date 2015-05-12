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

class TraversalTestSupport {
    private static final nestedXml = '''
    <_1>
        <_1_1>
            <_1_1_1/>
            <_1_1_2>
                <_1_1_2_1/>
            </_1_1_2>
        </_1_1>
        <_1_2>
            <_1_2_1/>
        </_1_2>
    </_1>
    '''

    static void checkDepthFirst(Closure getRoot) {
        def root = getRoot(nestedXml)
        def trace = ''
        root.depthFirst().each{ trace += it.name() + ' ' }
        assert trace == '_1 _1_1 _1_1_1 _1_1_2 _1_1_2_1 _1_2 _1_2_1 '
        // test shorthand
        trace = ''
        root.'_1_2'.'**'.each{ trace += it.name() + ' ' }
        assert trace == '_1_2 _1_2_1 '
    }

    static void checkBreadthFirst(Closure getRoot) {
        def root = getRoot(nestedXml)
        def trace = ''
        root.breadthFirst().each{ trace += it.name() + ' ' }
        assert trace == '_1 _1_1 _1_2 _1_1_1 _1_1_2 _1_2_1 _1_1_2_1 '
    }
}

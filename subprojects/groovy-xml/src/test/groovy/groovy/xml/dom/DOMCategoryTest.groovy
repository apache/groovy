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
package groovy.xml.dom

import groovy.test.GroovyTestCase
import groovy.xml.DOMBuilder
import groovy.xml.GpathSyntaxTestSupport
import groovy.xml.MixedMarkupTestSupport
import groovy.xml.TraversalTestSupport
import groovy.xml.XmlUtil

import static javax.xml.xpath.XPathConstants.BOOLEAN
import static javax.xml.xpath.XPathConstants.NODE
import static javax.xml.xpath.XPathConstants.NODESET
import static javax.xml.xpath.XPathConstants.NUMBER

class DOMCategoryTest extends GroovyTestCase {

    def getRoot = { xml ->
        def reader = new StringReader(xml)
        def doc    = DOMBuilder.parse(reader)
        doc.documentElement
    }

    void testReplacementsAndAdditions() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkReplaceNode(getRoot)
            GpathSyntaxTestSupport.checkReplaceMultipleNodes(getRoot)
            GpathSyntaxTestSupport.checkPlus(getRoot)
        }
    }

    void testMixedMarkup() {
        use(DOMCategory) {
            MixedMarkupTestSupport.checkMixedMarkup(getRoot)
            MixedMarkupTestSupport.checkMixedMarkupText(getRoot)
        }
    }

    void testElement() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkUpdateElementValue(getRoot)
            GpathSyntaxTestSupport.checkElement(getRoot)
            GpathSyntaxTestSupport.checkFindElement(getRoot)
            GpathSyntaxTestSupport.checkElementTypes(getRoot)
            GpathSyntaxTestSupport.checkElementClosureInteraction(getRoot)
            GpathSyntaxTestSupport.checkElementTruth(getRoot)
            GpathSyntaxTestSupport.checkCDataText(getRoot)
        }
    }

    void testAttribute() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkAttribute(getRoot)
            GpathSyntaxTestSupport.checkAttributes(getRoot)
            GpathSyntaxTestSupport.checkAttributeTruth(getRoot)
        }
    }

    void testIndices() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkNegativeIndices(getRoot)
            GpathSyntaxTestSupport.checkRangeIndex(getRoot)
        }
    }

    void testNavigation() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkChildren(getRoot)
            GpathSyntaxTestSupport.checkParent(getRoot)
            GpathSyntaxTestSupport.checkNestedSizeExpressions(getRoot)
        }
    }

    void testTraversal() {
        use(DOMCategory) {
            TraversalTestSupport.checkDepthFirst(getRoot)
            TraversalTestSupport.checkBreadthFirst(getRoot)
        }
    }

    void testGetOnMapWithDomCategory() {
        Map ids = [:]
        ids.put("try", "sample1")
        assert ids.get("try") == "sample1"

        use(DOMCategory) {
            ids["try"] = "sample2"
            assert ids["try"] == "sample2"
            assert ids.get("try") == "sample2"
            assert ids.put("try", "sample3")
            assert ids.get("try") == "sample3"
        }
    }

    void testGetOnNonNodesWithDomCategory() {
        def myFoo = new Foo()
        assert myFoo.get("bar") == 3
        use(DOMCategory) {
            assert myFoo.get("bar") == 3
        }
    }

    /** Test for GROOVY-3109 */
    void testAccessToUnknownPropertyInAScriptWithDomCategory() {
        assertScript """
            import groovy.xml.dom.DOMCategory

            try {
                use(DOMCategory) {
                    test
                }
                assert false
            } catch (MissingPropertyException e) {
                assert 'test' == e.property
            }
        """
    }

    /** Test for GROOVY-3109 */
    void testAccessToUnknownPropertyInAClassWithDomCategory() {
        assertScript """
            import groovy.xml.dom.DOMCategory

            class MyClass {
                static boolean run() {
                    try {
                        use (DOMCategory) {
                            println test
                        }
                        return false
                    } catch (MissingPropertyException e) {
                        assert 'test' == e.property
                        return true
                    }
                }
            }

            assert true == MyClass.run()
        """
    }

    void testXPathWithDomCategory() {
        def reader = new StringReader('<a><b>B1</b><b>B2</b><c a1="4" a2="true">C</c></a>')
        def root = DOMBuilder.parse(reader).documentElement
        use(DOMCategory) {
            assert root.xpath('c/text()') == 'C'
            def text = { n -> n.xpath('text()') }
            assert text(root.xpath('c', NODE)) == 'C'
            assert root.xpath('c/@a1') == '4'
            assert root.xpath('c/@a1', NUMBER) == 4
            assert root.xpath('c/@a2', BOOLEAN)
            assert root.xpath('b', NODESET).collect(text).join() == 'B1B2'
        }
    }

    void testDomCategoryNameWithNodes() {
        def reader = new StringReader('<a><b>B1</b><b>B2</b></a>')
        def a = DOMBuilder.parse(reader).documentElement
        use(DOMCategory) {
            // TODO: rationalise this difference in a future version of Groovy
            // e.g. implement both depthFirst() for elements as well as
            // depthFirstNodes() and ditto for breadthFirst(), children() etc.
            assert a.breadthFirst()*.name().join('->') == 'a->b->b->#text->#text'
            assert a.depthFirst()*.name().join('->') == 'a->b->b'
        }
    }

    void testReplaceNode() {
        def readerOld = new StringReader('<root><old/></root>')
        def oldDoc = DOMBuilder.parse(readerOld)
        def oldRoot = oldDoc.documentElement
        def readerNew = new StringReader('<new><child/></new>')
        def newRoot = DOMBuilder.parse(readerNew).documentElement
        def imported = oldDoc.importNode(newRoot, true)
        use(DOMCategory) {
            def old = oldRoot[0]
            def removed = oldRoot.replaceChild(imported, old)
            assert removed.name() == 'old'
        }

        assert XmlUtil.serialize(oldRoot).readLines()*.trim().join('') ==
            '<?xml version="1.0" encoding="UTF-8"?><root><new><child/></new></root>'
    }
}

class Foo {
    def get(String name) {
        return name.size()
    }
}

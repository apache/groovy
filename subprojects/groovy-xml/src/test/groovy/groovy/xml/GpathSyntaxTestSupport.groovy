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

import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.Diff
import groovy.xml.XmlUtil

class GpathSyntaxTestSupport {
    private static final sampleXml = '''
<characters>
    <character id="1" name="Wallace">
        <likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
        <likes>sleep</likes>
    </character>
    <numericValue>1</numericValue>
    <booleanValue>y</booleanValue>
    <uriValue>http://example.org/</uriValue>
    <urlValue>http://example.org/</urlValue>
    <empty/>
</characters>
'''

    private static final nestedXml = '''
<root>
    <a><z/><z/><y/></a>
    <b><z/></b>
    <c><x/></c>
    <d></d>
</root>
'''

    static void checkUpdateElementValue(Closure getRoot) {
        def root = getRoot('<root><a>foo</a><b/></root>')
        assert root != null
        def a = root.a[0]
        def b = root.b[0]
        a.value = a.text() + 'bar'
        b.value = 'baz'
        String actual = XmlUtil.serialize(root)
        def expected = '''\
<?xml version="1.0" encoding="UTF-8"?>
<root>
  <a>foobar</a>
  <b>baz</b>
</root>
'''
        XMLUnit.ignoreWhitespace = true
        def xmlDiff = new Diff(actual, expected)
        assert xmlDiff.similar(), xmlDiff.toString()
    }

    static void checkElement(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert root != null
        def characters = root.character
        assert 2 == characters.size()
        assert 2 == root.'character'.size()
        assert 2 == root['character'].size()
        def wallace = characters[0]
        assert wallace.name() == 'character'
        def likes = characters.likes
        assert 2 == likes.size()
        def wallaceLikes = likes[0]
        assert wallaceLikes.name() == 'likes'
        assert wallaceLikes.text() == 'cheese'
        checkEmptyMissingCases(root)
        if (isDom(root)) {
            // additional DOM long-hand syntax
            // for illustrative purposes only
            assert likes.item(0).nodeName == 'likes'
            assert wallaceLikes.firstChild.nodeValue == 'cheese'
            if (wallaceLikes.class.name.contains('xerces')) {
                assert 'cheese' == wallaceLikes.textContent
            }
        }
    }

    private static void checkEmptyMissingCases(root) {
        def unknownChild = root.xxx
        assert unknownChild.isEmpty()
        def unknownAttr = root.'@xxx'
        assert isSlurper(root) || !unknownAttr
        assert !isSlurper(root) || unknownAttr.isEmpty()
        assert root.'empty'.text() == ''
    }

    static void checkCDataText(Closure getRoot) {
        def root = getRoot('<root><![CDATA[A & B < C]]></root>')
        assert root.text() == 'A & B < C'
    }

    static void checkFindElement(Closure getRoot) {
        def root = getRoot(sampleXml)
        // let's find Gromit
        def gromit = root.character.find { it.'@id' == '2' }
        assert gromit != null, "Should have found Gromit!"
        assert gromit['@name'] == "Gromit"
        // let's find what Wallace likes in 1 query
        def answer = root.character.find { it['@id'] == '1' }.likes[0].text()
        assert answer == "cheese"
        assert root.character.findAll{ it.'@id'.toInteger() < 3 }*.likes*.text() == ['cheese', 'sleep']
        assert root.character.findAll{ it.'@id'.toInteger() < 3 }.likes*.text() == ['cheese', 'sleep']
    }

    static void checkNestedSizeExpressions(Closure getRoot) {
        def root = getRoot(nestedXml)
        assert root.'*'.size() == 4, "Expected size 4 but was ${root.'*'.size()}"
        assert root.'a'.'z'.size() == 2, "Expected size 2 but was ${root.'a'.'z'.size()}"
        assert root.'*'.'*'.size() == 5
        assert root.a.'*'.size() == 3
        assert root.'*'.z.size() == 3
        assert root.'*'.'*'.collect{it.name()} == ["z", "z", "y", "z", "x"]
        assert root.a.'*'.collect{it.name()} == ["z", "z", "y"]
        assert root.'*'.findAll{ it.z.size() > 0 }.collect{it.name()} == ["a", "b"]
    }

    static void checkElementTypes(Closure getRoot) {
        def root = getRoot(sampleXml)
        def numericValue = root.numericValue[0]
        def booleanValue = root.booleanValue[0]
        def uriValue     = root.uriValue[0]
        def urlValue     = root.urlValue[0]
        assert numericValue.text().toInteger() == 1
        assert numericValue.text().toLong() == 1
        assert numericValue.text().toFloat() == 1
        assert numericValue.text().toDouble() == 1
        assert numericValue.text().toBigInteger() == 1
        assert numericValue.text().toBigDecimal() == 1
        assert booleanValue.text().toBoolean()
        assert uriValue.text().toURI() == "http://example.org/".toURI()
        assert urlValue.text().toURL() == "http://example.org/".toURL()
        if (isSlurper(root)) {
            // slurper shorthand - are these really pulling their weight?
            assert numericValue.toInteger() == 1
            assert numericValue.toLong() == 1
            assert numericValue.toFloat() == 1
            assert numericValue.toDouble() == 1
            assert numericValue.toBigInteger() == 1
            assert numericValue.toBigDecimal() == 1
            assert booleanValue.toBoolean()
            assert uriValue.toURI() == "http://example.org/".toURI()
            assert urlValue.toURL() == "http://example.org/".toURL()
        }
    }

    static void checkElementClosureInteraction(Closure getRoot) {
        def root = getRoot(sampleXml)
        def sLikes = root.character.likes.findAll{ it.text().startsWith('s') }
        assert sLikes.size() == 1
        assert root.likes.size() == 0
        if (isDom(root)) {
            // additional DOMCategory long-hand notation gets nested nodes from root
            assert root.getElementsByTagName('likes').size() == 2
        }
        assert 'sleep' == sLikes[0].text()
        assert 'cheesesleep' == root.character.likes.collect{ it.text() }.join()
        assert root.character.likes.every{ it.text().contains('ee') }
        def groupLikesByFirstLetter
        def likes = root.character.likes.collect{ it }
        if (isSlurper(root)) {
            //groupLikesByFirstLetter = likes.groupBy{ like ->
            //    root.character.find{ it.likes[0].text() == like.text() }.@name.toString()[0]
            //}
            // TODO: Broken? Why doesn't below work? FIX with GROOVY-6125
            groupLikesByFirstLetter = likes.groupBy{ it.parent().@name.toString()[0] }
        } else {
            groupLikesByFirstLetter = likes.groupBy{ it.parent().'@name'[0] }
        }
        groupLikesByFirstLetter.keySet().each{
            groupLikesByFirstLetter[it] = groupLikesByFirstLetter[it][0].text()
        }
        assert groupLikesByFirstLetter == [W:'cheese', G:'sleep']
    }

    static void checkElementTruth(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert root.character[0]
        assert !root.doesnotexist[0]
    }

    static void checkAttribute(Closure getRoot) {
        def root = getRoot(sampleXml)
        if (isSlurper(root)) {
            assert 'Wallace' == root.character[0].'@name'.text()
            assert 'Wallace' == root.character[0]['@name'].text()
            assert 'Wallace' == (root.character.'@name')[0].text()
            assert ['Wallace', 'Gromit'] == root.character.'@name'.list()*.text()
            assert 'WallaceGromit' == root.character.'@name'.text()
        } else {
            assert 'Wallace' == root.character[0].'@name'
            assert 'Wallace' == root.character[0]['@name']
            assert 'Wallace' == (root.character.'@name')[0]
            assert ['Wallace', 'Gromit'] == root.character.collect{ it.'@name' }
            assert 'WallaceGromit' == root.character.'@name'.join()
        }
        if (isSlurper(root)) {
            // additional slurper shorthand
            assert 'Wallace' == root.character[0].@name.text()
            def gromit = root.character.find{ it.@id == '2' }
            assert gromit.@name.name() == "name"
        }
        if (isParser(root)) {
            // additional parser shorthand
            assert 'Wallace' == root.character[0].@name
            def gromit = root.character.find {it.@id == '2'}
            def actualName = gromit.@name
            assert actualName == 'Gromit'
        }
        if (isSlurper(root)) {
            // validate the behavior of the '.name()' method.
            root.character.each {
                assert 'id' == it.@id.name()
                assert 'name' == it.@name.name()
            }
            root.character.@id.each {
                assert 'id' == it.name()
            }
            root.character.@name.each {
                assert 'name' == it.name()
            }
        }
    }

    static void checkAttributes(Closure getRoot) {
        def root = getRoot(sampleXml)
        def attributes = root.character[0].attributes()
        assert         2 == attributes.size()
        assert 'Wallace' == attributes['name']
        assert 'Wallace' == attributes.name
        assert       '1' == attributes.'id'
    }

    static void checkAttributeTruth(Closure getRoot) {
        def root = getRoot(sampleXml)
        if (isDom(root)) {
            // no native attribute syntax for dom, so use quotes
            assert root.character.findAll { it.'@id' == '2' }[0]
            assert !root.character.findAll { it.'@id' == '3' }[0]
        } else {
            assert root.character.findAll { it.@id == '2' }[0]
            assert !root.character.findAll { it.@id == '3' }[0]
        }
    }

    static void checkChildren(Closure getRoot) {
        def root = getRoot(sampleXml)
        def children = root.children()
        // count direct children
        assert children.size() == 7, "Children ${children.size()}"
        assert root.'*'.size() == 7
        // illustrative purposes only
        if (isDom(root)) {
            // count whitespace and nested children
            assert root.childNodes.size() == 15
            // count nested children
            assert root.getElementsByTagName('*').size() == 9
        }
    }

    static void checkParent(Closure getRoot) {
        def root = getRoot(sampleXml)
        def gromit = root.character.find { it['@id'] == '2' }
        assert gromit.likes[0].parent() == gromit
        assert gromit.likes[0].'..' == gromit
        assert gromit.likes[0].parent().parent() == root
        assert root.character.likes.find{ it.text() == 'sleep' }.parent() == gromit
        assert gromit.parent() == root
        if (isSlurper(root)) {
            // additional slurper shorthand
            assert gromit.likes.parent() == gromit
            assert gromit.likes.findAll{ it.text() == 'sleep' } == gromit
            // pop() method to backtrack on GPath
            assert gromit.'..'.pop() == gromit
        }
        if (isSlurper(root)) {
            assert root.parent() == root
        } else if (isParser(root)) {
            assert root.parent() == null
        } else if (isDom(root)) {
            assert (root.parent() instanceof org.w3c.dom.Document)
        }
    }

    static void checkNegativeIndices(Closure getRoot) {
        def root = getRoot('<a><b>B1</b><b>B2</b><b>B3</b><c><n id="n1"/><n id="n2"/></c></a>')
        assert root.b[-1].text() == 'B3'
        assert root.b[-3].text() == 'B1'
        assert root.c.n[-1].'@id' == 'n2'
        assert root.c.n[-2].'@id' == 'n1'
    }

    static void checkRangeIndex(Closure getRoot) {
        def root = getRoot('<a><b>B1</b><b>B2</b><b>B3</b><c><n id="n1"/><n id="n2"/></c></a>')
        def bs = root.b[1..2]
        assert bs.size() == 2
        assert bs[0].text() == 'B2'
        assert bs[1].text() == 'B3'

        bs = root.b[1..-1]
        assert bs.size() == 2
        assert bs[0].text() == 'B2'
        assert bs[1].text() == 'B3'

        bs = root.b[-3..-2]
        assert bs.size() == 2
        assert bs[0].text() == 'B1'
        assert bs[1].text() == 'B2'

        // Reverse order.
        bs = root.b[2..1]
        assert bs.size() == 2
        assert bs[0].text() == 'B3'
        assert bs[1].text() == 'B2'

        bs = root.b[-2..-3]
        assert bs.size() == 2
        assert bs[0].text() == 'B2'
        assert bs[1].text() == 'B1'

        bs = root.b[1..-3]
        assert bs.size() == 2
        assert bs[0].text() == 'B2'
        assert bs[1].text() == 'B1'
    }

    static void checkReplaceNode(Closure getRoot) {
        def root = getRoot('<a><b>B1</b><b>B2</b><c a1="4" a2="true"><x>500</x></c><d/></a>')
        def r = root.c.replaceNode {
            n(type: 'string') {
                hello('world')
            }
        }
        String actual = XmlUtil.serialize(root)
        def expected = '''\
<?xml version="1.0" encoding="UTF-8"?>
<a>
  <b>B1</b>
  <b>B2</b>
  <n type="string">
    <hello>world</hello>
  </n>
  <d/>
</a>
'''
        XMLUnit.ignoreWhitespace = true
        def xmlDiff = new Diff(actual, expected)
        assert xmlDiff.similar(), xmlDiff.toString()

        // XmlSlurper replacements are deferred so can't check here
        if (!isSlurper(root)) {
            assert r.name() == 'c'
            assert r.'@a1' == '4'
        }
    }

    static void checkReplaceMultipleNodes(Closure getRoot) {
        def root = getRoot('<a><b>B1</b><b>B2</b><c a1="4" a2="true"><x>500</x></c><d/></a>')
        def r = root.c.replaceNode {
            n(type: 'string') {
                hello('world')
            }
            n(type: 'int', 330)
        }
        String actual = XmlUtil.serialize(root)
        def expected = '''\
<?xml version="1.0" encoding="UTF-8"?>
<a>
  <b>B1</b>
  <b>B2</b>
  <n type="string">
    <hello>world</hello>
  </n>
  <n type="int">330</n>
  <d/>
</a>
'''
        XMLUnit.ignoreWhitespace = true
        def xmlDiff = new Diff(actual, expected)
        assert xmlDiff.similar(), xmlDiff.toString()

        // XmlSlurper replacements are deferred so can't check here
        if (!isSlurper(root)) {
            assert r.name() == 'c'
            assert r.'@a1' == '4'
        }
    }

    static void checkPlus(Closure getRoot) {
        def root = getRoot('<a><b>B1</b><b>B2</b><c a1="4" a2="true">C</c></a>')
        root.c + {
            n(type: 'string') {
                hello('world')
            }
        }
        root.b + {
            b('B3')
        }
        root.b[-1] + {
            b('B4')
        }
        String actual = XmlUtil.serialize(root)
        def expected = '''\
<?xml version="1.0" encoding="UTF-8"?>
<a>
  <b>B1</b>
  <b>B3</b>
  <b>B2</b>
  <b>B3</b>
  <b>B4</b>
  <c a1="4" a2="true">C</c>
  <n type="string">
    <hello>world</hello>
  </n>
</a>
'''
        XMLUnit.ignoreWhitespace = true
        def xmlDiff = new Diff(actual, expected)
        assert xmlDiff.similar(), xmlDiff.toString()
    }

    private static boolean isSlurper(node) {
        return node.getClass().name.contains('slurper')
    }

    private static boolean isParser(node) {
        return (node instanceof groovy.util.Node)
    }

    private static boolean isDom(node) {
        return node.getClass().name.contains('Element')
    }
}

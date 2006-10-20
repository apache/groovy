package groovy.xml.dom

import groovy.xml.DOMBuilder
import groovy.xml.GpathSyntaxTestSupport

class DOMCategoryTest extends GroovyTestCase {

    def getRoot = { xml ->
        def reader = new StringReader(xml)
        def doc    = DOMBuilder.parse(reader)
        def root   = doc.documentElement
    }

    def testXml = """
<characters>
    <character id="1" name="Wallace">
    	<likes>cheese</likes>
    </character>
    <character id="2" name="Gromit">
	    <likes>sleep</likes>
    </character>
</characters>
"""

    void testDomCategory() {
        def reader = new StringReader(testXml)
        def doc    = DOMBuilder.parse(reader)
        def root   = doc.documentElement
        def node   = root.getElementsByTagName('character').item(0)

        use(DOMCategory) {
            assert           4 == root['*'].size()
            assert           4 == root.'*'.size()
            assert           2 == root.character.size()
            assert           2 == root.'character'.size()
            assert           2 == root['likes'].size()
            assert 'character' == node.nodeName
            assert 'character' == node.name()
            assert 'likes'     == node.item(1).nodeName
            assert node        == node.item(1).parent()
            assert 'cheese'    == node.likes[0].firstChild.nodeValue
            assert 'cheese'    == node.likes[0].text()
            if (node.class.name.contains('xerces')) {
                assert 'cheese' == node.likes[0].textContent
            }
            assert 'Wallace' == node.'@name'
            assert 'Wallace' == node['@name']
            assert ['Wallace', 'Gromit'] == root.'character'.'@name'
            assert ['cheese', 'sleep']   == root.likes.text()
            assert ['sleep'] == root.likes.findAll{ it.text().startsWith('s') }.text()
            assert root.likes.every{ it.text().contains('ee') }
            def groupLikesByFirstLetter = root.likes.list().groupBy{ it.parent().'@name'[0] }
            groupLikesByFirstLetter.keySet().each{
                groupLikesByFirstLetter[it] = groupLikesByFirstLetter[it][0].text()
            }
            assert groupLikesByFirstLetter == [W:'cheese', G:'sleep']
        }
    }

//    void testMixedMarkup() {
//        use(DOMCategory) {
//            GpathSyntaxTestSupport.checkMixedMarkup(getRoot)
//        }
//    }

    void testElement() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkElement(getRoot)
        }
    }

    void testAttribute() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkAttribute(getRoot)
        }
    }

    void testAttributes() {
        use(DOMCategory) {
            GpathSyntaxTestSupport.checkAttributes(getRoot)
        }
    }

//    void testDepthFirst() {
//        TraversalTestSupport.checkDepthFirst(getRoot)
//    }

//    void testBreadthFirst() {
//        TraversalTestSupport.checkBreadthFirst(getRoot)
//    }

/*
    def testSchemaXml = '''<?xml version="1.0" encoding="UTF-8"?>
<shiporder xmlns:color="http://www.acme.org/Color"
     xmlns:Order="http://www.acme.org/Order" orderid="123">
    <order:customer>customer</order:customer>
    <order:item>
        <order:name>My Red Item</order:name>
        <order:quantity>3</order:quantity>
        <color:name>Red</color:name>
    </order:item>
</shiporder>
'''

    void xxxtestNamespaces() {
        def reader = new StringReader(testSchemaXml)
        def doc    = DOMBuilder.parse(reader)
        def root   = doc.documentElement

        use(DOMCategory) {
            def firstChild = root['*'][0]
            println firstChild.class.name + " has name(): " + firstChild.name()
            println "namespaceURI         = " + firstChild.namespaceURI
            println "lookupNamespaceURI() = " + firstChild.lookupNamespaceURI()
            println "baseUR         I     = " + firstChild.baseURI
            println "tagName              = " + firstChild.tagName
            println "prefix               = " + firstChild.prefix
            println "lookupPrefix()       = " + firstChild.lookupPrefix()
            println "nodeName             = " + firstChild.nodeName
            println "isDefaultNamespace() = " + firstChild.isDefaultNamespace()
            println root['*'].size()
            println root['http://www.acme.org/Order#:*'].size()
            println root['http://www.acme.org/Order:*'].size()
            println root['order:*'].size()
            println root['*:name'].size()
        }
    }
*/
}

package groovy.xml.dom

import groovy.xml.DOMBuilder

class DOMCategoryTest extends GroovyTestCase {
    
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

        use(DOMCategory){
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
            assert ['Wallace', 'Gromit'] == root.'character'.'@name'
            assert ['cheese', 'sleep']   == root.likes.text()
            assert ['sleep'] == root.likes.findAll{ it.text().startsWith('s') }.text()
            assert root.likes.every{ it.text().contains('ee') }
            def groupLikesByFirstLetter = root.likes.list().groupBy{ it.parent().'@name'[0] }
            groupLikesByFirstLetter.keySet().each{
                groupLikesByFirstLetter[it] = groupLikesByFirstLetter[it][0].text()
            }
            assert groupLikesByFirstLetter == [W:'cheese', G:'sleep']
            def attributes = node.attributes()
            assert         2 == attributes.size()
            assert 'Wallace' == attributes.name
            assert       '1' == attributes.id
        }
    }
}

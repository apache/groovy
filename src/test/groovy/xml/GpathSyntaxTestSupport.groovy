package groovy.xml

class GpathSyntaxTestSupport {
    private static def sampleXml = '''
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
            assert likes.item(0).nodeName == 'likes'
            assert wallaceLikes.firstChild.nodeValue == 'cheese'
            if (wallaceLikes.class.name.contains('xerces')) {
                assert 'cheese' == wallaceLikes.textContent
            }
        }
    }

    private static void checkEmptyMissingCases(root) {
        def unknownChild = root.xxx
        assert unknownChild.size() == 0
        def unknownAttr = root.'@xxx'
        if (isParser(root)) {
            assert unknownAttr == null
        } else {
            assert unknownAttr.toString() == ''
        }
        if (isDom(root)) {
            assert root.empty.text().toString() == '[]'
        } else {
            assert root.empty.text() == ''
        }
    }

    static void checkFindElement(Closure getRoot) {
        def root = getRoot(sampleXml)
        // lets find Gromit
        def gromit = root.character.find { it['@id'] == '2' }
        assert gromit != null, "Should have found Gromit!"
        assert gromit['@name'] == "Gromit"
        // lets find what Wallace likes in 1 query
        def answer = root.character.find { it['@id'] == '1' }.likes[0].text()
        assert answer == "cheese"
    }

    static void checkElementTypes(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert root.numericValue[0].text().toInteger() == 1
        if (isSlurper(root)) {
            // additional slurper shorthand
            assert root.numericValue.toInteger() == 1
        }
        assert root.numericValue[0].text().toLong() == 1
        assert root.numericValue[0].text().toFloat() == 1
        assert root.numericValue[0].text().toDouble() == 1
        assert root.numericValue[0].text().toBigInteger() == 1
        assert root.numericValue[0].text().toBigDecimal() == 1
        assert root.booleanValue[0].text().toBoolean() == true
        assert root.uriValue[0].text().toURI() == "http://example.org/".toURI()
        assert root.urlValue[0].text().toURL() == "http://example.org/".toURL()
    }

    static void checkElementClosureInteraction(Closure getRoot) {
        def root = getRoot(sampleXml)
        def sLikes = root.character.likes.findAll{ it.text().startsWith('s') }
        assert sLikes.size() == 1
        assert root.likes.size() == 0
        if (isDom(root)) {
            // addtitional DOMCategory long-hand notation gets nested
            assert root.getElementsByTagName('likes').size() == 2
        }
        if (isSlurper(root)) {
            assert 'sleep' == sLikes.text()
            assert 'sleep' == sLikes.toString()
            assert 'cheesesleep' == root.character.likes.text()
            assert 'cheesesleep' == root.character.likes.toString()
        } else {
            assert ['sleep'] == sLikes*.text()
            if (isDom(root)) {
                assert '[<likes>sleep</likes>]' == sLikes.toString()
                assert '[cheese, sleep]' == root.character.likes.text().toString()
                assert '[<likes>cheese</likes>, <likes>sleep</likes>]' == root.character.likes.toString()
            }
            if (isParser(root)) {
                assert '[likes[attributes={}; value=[sleep]]]' == sLikes.toString()
                assert '[likes[attributes={}; value=[cheese]], likes[attributes={}; value=[sleep]]]' == root.character.likes.toString()
            }
        }
        assert root.character.likes.every{ it.text().contains('ee') }
        def groupLikesByFirstLetter
        def likes
        if (isSlurper(root)) {
            likes = root.character.likes.collect{ it }
        } else if (isDom(root)) {
            likes = root.character.likes.list()
        } else {
            likes = root.character.likes
        }
        if (isSlurper(root)) {
            groupLikesByFirstLetter = likes.groupBy{ like ->
                root.character.find{ it.likes[0].text() == like.text() }.@name.toString()[0]
            }
            // TODO: Broken? Why doesn't below work?
            //groupLikesByFirstLetter = likes.groupBy{ it.parent().@name.toString()[0] }
        } else {
            groupLikesByFirstLetter = likes.groupBy{ it.parent().'@name'[0] }
        }
        groupLikesByFirstLetter.keySet().each{
            groupLikesByFirstLetter[it] = groupLikesByFirstLetter[it][0].text()
        }
        assert groupLikesByFirstLetter == [W:'cheese', G:'sleep']
    }

    static void checkAttribute(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert 'Wallace' == root.character[0].'@name'.toString()
        assert 'Wallace' == (root.character.'@name')[0].toString()
        if (isSlurper(root)) {
            assert 'WallaceGromit' == root.character.'@name'.toString()
            def gromit = root.character.find { it['@id'] == '2' }
            assert gromit.@name.name() == "name"
        } else {
            assert 'Wallace' == (root.character.'@name')[0]
            assert ['Wallace', 'Gromit'] == root.character.'@name'
            assert '[Wallace, Gromit]' == root.character.'@name'.toString()
            assert 'Wallace' == root.character[0].'@name'
            assert 'Wallace' == root.character[0]['@name']
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

    static void checkChildren(Closure getRoot) {
        def root = getRoot(sampleXml)
        def children = root.children()
        // count direct children
        assert children.size() == 7, "Children ${children.size()}"
        assert root.'*'.size() == 7
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
        assert gromit.parent() == root
        if (isSlurper(root)) {
            // additional slurper shorthand
            assert gromit.likes.parent() == gromit
        }
        if (isSlurper(root)) {
            assert root.parent() == root
        } else if (isParser(root)) {
            assert root.parent() == null
        } else if (isDom(root)) {
            assert (root.parent() instanceof org.w3c.dom.Document)
        }
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
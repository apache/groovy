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
</characters>
'''

    static void checkAttributes(Closure getRoot) {
        def root = getRoot(sampleXml)
        def attributes = root.character[0].attributes()
        assert         2 == attributes.size()
        assert 'Wallace' == attributes['name']
        assert 'Wallace' == attributes.name
        assert       '1' == attributes.'id'
    }

    static void checkElement(Closure getRoot) {
        def root = getRoot(sampleXml)
        def characters = root.character
        assert 2 == characters.size()
        def firstChild = characters[0]
        assert firstChild.name() == 'character'
        def likes = characters.likes
        assert 2 == likes.size()
        def firstLike = likes[0]
        assert firstLike.name() == 'likes'
        assert firstLike.text() == 'cheese'
    }

    static void checkAttribute(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert ['Wallace', 'Gromit'] == root.character.'@name'
        assert 'Wallace' == (root.character.'@name')[0]
        assert 'Wallace' == root.character[0].'@name'
    }

    static void checkAttributeMerging(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert 'WallaceGromit' == root.character.'@name'.toString()
        assert 'Wallace' == root.character[0].'@name'.toString()
    }

    static void checkTypes(Closure getRoot) {
        def root = getRoot(sampleXml)
        assert root.numericValue.toInteger() == 1
        assert root.numericValue.toLong() == 1
        assert root.numericValue.toFloat() == 1
        assert root.numericValue.toDouble() == 1
        assert root.numericValue.toBigInteger() == 1
        assert root.numericValue.toBigDecimal() == 1
        assert root.booleanValue.toBoolean() == true
        assert root.uriValue.toURI() == "http://example.org/".toURI()
        assert root.urlValue.toURL() == "http://example.org/".toURL()
    }

    static void checkMixedMarkup(Closure getRoot) {
        def mixedXml = '''
<p>Please read the <a href="index.html">Home</a> page</p>
'''
        def root = getRoot(mixedXml)
        assert root != null
        def children = root.children()
        println children.size()
        assert children.size() == 3
        assert children[0] instanceof String
        assert children[1] instanceof Node
        assert children[2] instanceof String
    }

}
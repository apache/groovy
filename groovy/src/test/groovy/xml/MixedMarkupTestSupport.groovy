package groovy.xml

class MixedMarkupTestSupport {

    private static def mixedXml = '''
<p>Please read the <a href="index.html">Home</a> page</p>
'''
    static void checkMixedMarkup(Closure getRoot) {
        def root = getRoot(mixedXml)
        assert root != null
        def children = root.children()
        if (isSlurper(root)) {
            assert children.size() == 1
            assert children[0].name() == 'a'
        } else {
            assert children.size() == 3
            assert children[1].name() == 'a'
            if (isParser(root)) {
                assert children[2] == 'page'
            } else {
                assert children[2].text() == 'page'
            }
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

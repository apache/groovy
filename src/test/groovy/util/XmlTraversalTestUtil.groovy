package groovy.util

class XmlTraversalTestUtil {
    static def nestedXml = '''
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

    static void checkDepthFirst(traverser) {
        def root = traverser.parseText(nestedXml)
        def trace = ''
        root.depthFirst().each{ trace += it.name() + ' ' }
        assert trace == '_1 _1_1 _1_1_1 _1_1_2 _1_1_2_1 _1_2 _1_2_1 '
    }

    static void checkBreadthFirst(traverser) {
        def root = traverser.parseText(nestedXml)
        def trace = ''
        root.breadthFirst().each{ trace += it.name() + ' ' }
        assert trace == '_1 _1_1 _1_2 _1_1_1 _1_1_2 _1_2_1 _1_1_2_1 '
    }
}

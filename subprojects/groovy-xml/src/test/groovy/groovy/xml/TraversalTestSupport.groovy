package groovy.xml

class TraversalTestSupport {
    private static def nestedXml = '''
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

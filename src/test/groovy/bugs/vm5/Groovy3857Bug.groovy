package groovy.bugs.vm5

import gls.CompilableTestSupport

class Groovy3857Bug extends CompilableTestSupport {
    void testInterfaceDefWithGenericsFollowedByANewLine() {
        shouldCompile """
            public interface MyMy <T extends Object>
            {
            }
        """
    }
}
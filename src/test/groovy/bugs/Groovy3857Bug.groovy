package groovy.bugs

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
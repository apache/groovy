package groovy.bugs

import gls.CompilableTestSupport

class Groovy4081Bug extends CompilableTestSupport {
    public void testEnumConstructorCallFromOutsideEnum() {
        shouldNotCompile """
            enum Alphabet {
                A(1), Z(26)
                private int m_pos;
            
                public Alphabet( int pos ) {
                    m_pos = pos;
                }
            }
            
            new Alphabet(2)
        """
    }    
}

package groovy.bugs

class Groovy3789Bug extends GroovyTestCase {
    void testAddReturnWhenLastStatementIsSwitch() {
        def ifClosure = { ->
            if ( 0 ) { 10 }
            else { 20 }
        }
        def switchClosure = { ->
            switch ( 0 ) {
                case 0 : 10 ; break
                default : 20 ; break
            }
        }
        
        assert ifClosure() == 20
        assert switchClosure() == 10
    }
}

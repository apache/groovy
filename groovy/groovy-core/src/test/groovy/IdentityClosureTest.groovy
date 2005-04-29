/**
 * Check that Object.identity(Closure) method works as expected
 *
 * @author Jeremy Rayner
 */
class IdentityClosureTest extends GroovyTestCase {
    
    def foo = [[1,2,3],[4,5,6],[7,8,9]]
    def bar = " bar "
    def mooky = 1

    /** most useful perceived usecase, almost like with(expr) */
    void testIdentity0() {
        assert " bar " == bar

        bar.toUpperCase().trim().identity{
            assert "BAR" == it
            assert 3 = it.size()
            assert it.indexOf("A") > 0
        }
    }  

    /** check the basics */
    void testIdentity1() {
        mooky.identity{ spooky->
            assert spooky == mooky
        }
    }

    /** test temp shortcut to an element of an array */
    void testIdentity2() {
        assert 6 == foo[1][2]
        
        foo[1].identity{ myArray->
            myArray[2] = 12
        }
        
        assert 12 == foo[1][2]
    }

    /** check nested identity usage */
    void testIdentity3() {
        mooky.toString().identity{ m->
            assert "1" == m
            m += "234567890"
            m.identity{ n->
                assert "1234567890" == n
            }
        }
    }
}

package groovy.bugs


class ClassGeneratorFixesTest extends GroovyTestCase {
    def count = 0;

    def pf(int p) {
        int i = p
        boolean b = true
    }

    void testPrimitvesInFunc() { // groovy-373, 453, 385, 451, 199
        pf(10)
    }

    void testPlusEqual() { // 372
        count += 1
        assert count == 1

        def foo =
            {i->
                return {j->
                    i += j
                    i
                }
            }
        def x = foo(1)
        x(5)
        foo(3)
        println x(2.3)
    }

    void testIfAndSwitchInClosure (){ // 321, 324, 412

        def a = 1
        1.times {
            if (a ==1) {
                a = 2
            }
        }

        def noneYet=true;
        ["a","b","c","d"].each { c ->
          if (noneYet) {
            noneYet=false;
          } else {
            print(" > ");
          }
          print( c );
        }

        a = 1
        switch (a) {
        case 1:
            a = 2;
        case 2:
            break;
        default:
            break;
        }
    }

    void returnVoid() {
        return
    }

    void testReturnVoid() { // groovy-405, 387
        returnVoid()
    }
    
    void testBooleanValue() { // groovy-385
            /** @todo
            boolean value
            */
        }

}


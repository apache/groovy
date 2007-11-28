package groovy.bugs

/**
 * @author John Wilson
 * @version $Revision$
 */
class VariblePrecedence extends GroovyTestCase {
    
    void testVariablePrecedence() {
 
        assertScript( """
            class VariableFoo {
                def x = 100
                def y = 93
                def c = {x -> assert x == 1; assert y == 93; }

                static void main(args) {
                    def vfoo = new VariableFoo()
                    vfoo.c.call(1)

                    def z = 874;
                    1.times { assert vfoo.x == 100; assert z == 874; z = 39; }
                    assert z == 39;

                    vfoo.local();
                }

                void local() {
                    c.call(1);

                    def z = 874;
                    1.times { assert x == 100; assert z == 874; z = 39; }
                    assert z == 39;
                }
            }

        """ );

    }


/*
 * CURRENTLY BROKEN.  Variable scoping needs an overhaul to * fix it.
 */
    void testVariablePrecedenceInScript_FAILS() { if (notYetImplemented()) return
        assertScript( """
            c = { x -> assert x == 1; assert y == 93; }
            x = 100;
            y = 93;

            c.call(1);
        """);
    }

}

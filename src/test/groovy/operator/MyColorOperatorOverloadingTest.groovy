package groovy.operator

import static java.awt.Color.*

class MyColorOperatorOverloadingTest extends GroovyTestCase {
    void testAll() {
        if (HeadlessTestSupport.headless) return

        def c = new MyColor(128, 128, 128)
        assert c.delegate == GRAY
        def c2 = -c
        assert c2.delegate == DARK_GRAY
        assert (+c).delegate == WHITE
        use(MyColorCategory) {
            assert (~c2).delegate == LIGHT_GRAY
        }
    }
}

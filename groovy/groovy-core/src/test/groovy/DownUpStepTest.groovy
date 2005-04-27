public class DownUpStepTest extends GroovyTestCase {

    void testDownto() {
        z = 0.0
        (10.5).downto(5.9) { z += it }
        assert z == 10.5 + 9.5 + 8.5 + 7.5 + 6.5
        assert z == 42.5
    }

    void testUpto() {
        z = 0.0
        (3.1).upto(7.2) { z += it }
        assert z == 3.1 + 4.1 + 5.1 + 6.1 + 7.1
        assert z == 25.5
    }

    void testStep() {
        z = 0.0
        (1.2).step(3.9, 0.1) { z += it }
        assert z == 67.5
    }

    void testDownStep() {
        z = 0.0
        (3.8).step(1.1, -0.1) { z += it }
        assert z == 67.5
    }
}

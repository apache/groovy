package groovy;

import org.junit.TestCase;

class GroovyTest extends TestCase, StaticLogMixin {

    // add a main method 
    static main(args) {
        TestRunner.run(suite());
    }

    // define the static suite() method that JUnit expects
    static suite() {
        return TestSuite(thisClass);
    }
}
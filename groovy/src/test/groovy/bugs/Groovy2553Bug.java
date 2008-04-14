package groovy.bugs;

import groovy.lang.GroovyShell;
import junit.framework.TestCase;

class Autobox {

    public static class Util {
        public static void printByte(String str, Byte defaultValue) {
            System.out.println(str + ", " + defaultValue);
        }
    }
}

public class Groovy2553Bug extends TestCase {

    public void testMe () {
        new GroovyShell().evaluate("groovy.bugs.Autobox.Util.printByte(\"1\", Byte.valueOf((byte)1));");
        new GroovyShell().evaluate("groovy.bugs.Autobox.Util.printByte(\"1\", (byte)1);");
    }
}

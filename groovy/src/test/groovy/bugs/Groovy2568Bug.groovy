package groovy.bugs

public class Groovy2568Bug extends GroovyTestCase {

    public static void print_byte(String str, byte defaultValue) {
        System.out.println(str + ", " + defaultValue);
    }

    public static void printByte(String str, Byte defaultValue) {
        System.out.println(str + ", " + defaultValue);
    }

    void testAutoboxBytes() {
        printByte("1", new Byte((byte) 1));
        printByte("1", (byte) 1);
        print_byte("1", new Byte((byte) 1));
        print_byte("1", (byte) 1);
    }

}

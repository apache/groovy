import java.sql.SQLException

trait AAA {
    public AAA() {

    }

    public AAA(String name) {

    }

    @Test2
    public AAA(String name, int age) throws Exception {

    }

    AAA(String name, int age, String title) throws Exception {

    }

    private AAA(String name, int age, String title, double income) throws Exception {

    }

    @Test2
    public String sayHello(String name) {
        return "Hello, $name";
    }

    @Test2
    public <T> T sayHello2(T name) throws IOException, SQLException {
        return "Hello, $name";
    }

    public static privateStaticMethod(){}

    public void m(final int param) {}
    public void m2(def param) {}
    public void m3(final int param1, long param2, final String param3) {}

    def "hello world"(p1, p2) {
        println "$p1, $p2"
    }

    def run() {
        this."hello world"('ab', 'bc')
    }
}

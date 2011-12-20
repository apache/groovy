package groovy.bugs

class Groovy5101Test extends GroovyTestCase {

    static class ClassA {
        Runnable r
        ClassA(Runnable r) { this.r = r }
        void run() { "hello" }
    }

    static class Factory {
        void getClassA1(Runnable r) {
            new ClassA(r) // OK
        }
        void getClassA2(Runnable r) {
            new ClassA(r) {
                void run() { "a fixed message" } // OK
            }
        }
        void getClassA3(Runnable r) {
            new ClassA(r) {
                void run() { r } // NG
            }
        }
    }

    Runnable r
    Factory factory

    void setUp() {
        this.r = new Runnable() {
            void run() { "A!" }
        }
        this.factory = new Factory()
    }

    void test_NotInnerAnonymousClass() {
        factory.getClassA1(r)
    }

    void test_InnerAnonymousClass_NotUsingArgument() {
        factory.getClassA2(r)
    }

    void test_InnerAnonymousClass_UsingArgument() {
        factory.getClassA3(r) // it should be OK but the result is NG
    }

}

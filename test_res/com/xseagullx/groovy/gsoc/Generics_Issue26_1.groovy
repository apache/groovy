
class A1<T> {
    T a;
    private def <K> K m() {}
}

class A2 {
    private <T> T a() {
        T.classLoader
    }
}

abstract class A3 implements List<Integer> {
    private Iterator<Integer> iterator() {
        return null
    }
}

abstract class A4 extends ArrayList<Integer> {
    private Iterator<Integer> iterator() {
        return null
    }
}

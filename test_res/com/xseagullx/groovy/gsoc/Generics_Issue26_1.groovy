
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
    private <T extends Integer & Cloneable> Iterator iterator() {
        return null
    }
}

class A extends ArrayList<Long> {}

class B<T> extends HashMap<T,List<T>> {}

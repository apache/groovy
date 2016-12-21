public class OuterA {
    class InnerB {}
}

class OuterClazz {
    enum InnerEnum implements SomeInterface {
        A, B
    }
}


class AA {
    class Inner {
        public def innerMethod() {}
    }
    private class PrivateInner {}
    protected final class ProtectedFinalInner {
        ProtectedFinalInner() {

        }

        ProtectedFinalInner(Integer a) {
            new A() {
                public int method() {
                    1
                }
            }
        }
    }

    public int method() {
        0
    }
}



interface A {
    static enum B {
        static interface C {
        }
    }
}

interface A2 {
    static class B2 {
        static enum C2 {
        }
    }
}

enum A4 {
    static interface B4 {
        static class C4 {
        }
    }
}


class A3 {
    static class B3 {
        static enum C3 {
        }
    }
}

class A5 {
    static class B5 {
        static class C5 {
        }
    }
}

interface A1 {
    static interface B1 {
        static enum C1 {
        }
    }
}





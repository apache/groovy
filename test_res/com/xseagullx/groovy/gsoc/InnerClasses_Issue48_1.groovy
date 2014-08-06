
class A {
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



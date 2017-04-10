enum E {
    A() {}, B(1) {{}},
    C(1, 2) {
        public void prt() {
            println "$x, $y"
        }

        void prt2() {
            println "$x, $y"
        }

        @Test2 prt3() {
            println "$x, $y"
        }

        private void prt4() {
            println "$x, $y"
        }

        private prt5() {
            println "$x, $y"
        }
    },
    D {
        void hello() {}
    }

    protected int x;
    protected int y;

    E() {}
    E(int x) {
        this.x = x;
    }
    E(int x, y) {
        this(x)
        this.y = y;
    }

    void prt() {
        println "123"
    }
}

enum F {
    @Test2
    A
}

enum G implements I<T> {

}
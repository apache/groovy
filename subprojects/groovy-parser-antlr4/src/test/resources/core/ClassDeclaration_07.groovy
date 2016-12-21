new A() {}
new A(1, '2') {
    public void prt() {
        new B() {}
        new C() {
            {
                new D() {
                    {
                        new E() {}
                        new F() {}
                    }
                }
            }
        }
    }
}

class OuterAA {
    public void method() {
        new InnerBB() {}
        new InnerCC() {{
            new InnerDD() {}
            new InnerEE() {}
        }}
        new InnerFF() {}
    }
}
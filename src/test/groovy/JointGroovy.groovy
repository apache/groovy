package groovy

class JointGroovy {
    StaticInner property

    static class StaticInner {
        NonStaticInner property2

        class NonStaticInner {
            Closure property3 = {}
        }
    }
}
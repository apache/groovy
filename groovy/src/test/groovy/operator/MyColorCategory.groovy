package groovy.operator

class MyColorCategory {
    static MyColor bitwiseNegate(MyColor self) {
        return new MyColor(256 - self.delegate.red, 256 - self.delegate.green, 256 - self.delegate.blue)
    }
}

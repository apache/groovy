package groovy.operator

import java.awt.Color

class MyColor {
    Color delegate

    MyColor(int r, int g, int b) {
        delegate = new Color(r, g, b)
    }

    MyColor negative() {
        return new MyColor(delegate.red.intdiv(2), delegate.green.intdiv(2), delegate.blue.intdiv(2))
    }

    MyColor positive() {
        return new MyColor(2 * delegate.red - 1, 2 * delegate.green - 1, 2 * delegate.blue - 1)
    }

    String toString() {
        return delegate.toString()
    }
}

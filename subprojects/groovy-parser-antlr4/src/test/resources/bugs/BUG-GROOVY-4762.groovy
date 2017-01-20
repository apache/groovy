package bugs

def get123() {2}
def foo(i) {this}

def a = foo(2).'123'
def b = foo 2   123

assert a == b
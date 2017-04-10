assert null == null?[1];
assert null == null?[1]?[1, 2];
assert null == null?[1]?[1, 2]?[1, 2, 3];

def a = null;
assert null == a?[1, 2];

def f() {return null}
assert null == f()?[1];


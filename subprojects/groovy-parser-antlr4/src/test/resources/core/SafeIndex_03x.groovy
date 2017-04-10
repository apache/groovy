import groovy.transform.*

@CompileStatic
def csObjArray() {
    String[] array = ['a', 'b'];
    assert 'b' == array?[1];

    array?[1] = 'c';
    assert 'c' == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 'c';
    assert null == array?[1];
}
csObjArray();

def objArray() {
    String[] array = ['a', 'b'];
    assert 'b' == array?[1];

    array?[1] = 'c';
    assert 'c' == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 'c';
    assert null == array?[1];
}
objArray();

@CompileStatic
def csBooleanArray() {
    boolean[] array = [true, false];
    assert false == array?[1];

    array?[1] = true;
    assert true == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = true;
    assert null == array?[1];
}
csBooleanArray();

def booleanArray() {
    boolean[] array = [true, false];
    assert false == array?[1];

    array?[1] = true;
    assert true == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = true;
    assert null == array?[1];
}
booleanArray();

@CompileStatic
def csCharArray() {
    char[] array = ['a' as char, 'b' as char];
    assert ('b' as char) == array?[1];

    array?[1] = 'c';
    assert ('c' as char) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 'c';
    assert null == array?[1];
}
csCharArray();

def charArray() {
    char[] array = ['a' as char, 'b' as char];
    assert ('b' as char) == array?[1];

    array?[1] = 'c';
    assert ('c' as char) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 'c';
    assert null == array?[1];
}
charArray();

@CompileStatic
def csByteArray() {
    byte[] array = [1 as byte, 2 as byte];
    assert (2 as byte) == array?[1];

    array?[1] = 3 as byte;
    assert (3 as byte) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3 as byte;
    assert null == array?[1];
}
csByteArray();

def byteArray() {
    byte[] array = [1 as byte, 2 as byte];
    assert (2 as byte) == array?[1];

    array?[1] = 3 as byte;
    assert (3 as byte) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3 as byte;
    assert null == array?[1];
}
byteArray();

@CompileStatic
def csShortArray() {
    short[] array = [1 as short, 2 as short];
    assert (2 as short) == array?[1];

    array?[1] = 3 as short;
    assert (3 as short) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3 as short;
    assert null == array?[1];
}
csShortArray();

def shortArray() {
    short[] array = [1 as short, 2 as short];
    assert (2 as short) == array?[1];

    array?[1] = 3 as short;
    assert (3 as short) == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3 as short;
    assert null == array?[1];
}
shortArray();

@CompileStatic
def csIntArray() {
    int[] array = [1, 2];
    assert 2 == array?[1];

    array?[1] = 3;
    assert 3 == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3;
    assert null == array?[1];
}
csIntArray();

def intArray() {
    int[] array = [1, 2];
    assert 2 == array?[1];

    array?[1] = 3;
    assert 3 == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3;
    assert null == array?[1];
}
intArray();

@CompileStatic
def csLongArray() {
    long[] array = [1L, 2L];
    assert 2L == array?[1];

    array?[1] = 3L;
    assert 3L == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3L;
    assert null == array?[1];
}
csLongArray();

def longArray() {
    long[] array = [1L, 2L];
    assert 2L == array?[1];

    array?[1] = 3L;
    assert 3L == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3L;
    assert null == array?[1];
}
longArray();

@CompileStatic
def csFloatArray() {
    float[] array = [1.1f, 2.2f];
    assert 2.2f == array?[1];

    array?[1] = 3.3f;
    assert 3.3f == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3.3f;
    assert null == array?[1];
}
csFloatArray();

def floatArray() {
    float[] array = [1.1f, 2.2f];
    assert 2.2f == array?[1];

    array?[1] = 3.3f;
    assert 3.3f == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3.3f;
    assert null == array?[1];
}
floatArray();

@CompileStatic
def csDoubleArray() {
    double[] array = [1.1d, 2.2d];
    assert 2.2d == array?[1];

    array?[1] = 3.3d;
    assert 3.3d == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3.3d;
    assert null == array?[1];
}
csDoubleArray();

def doubleArray() {
    double[] array = [1.1d, 2.2d];
    assert 2.2d == array?[1];

    array?[1] = 3.3d;
    assert 3.3d == array?[1];

    array = null;
    assert null == array?[1];

    array?[1] = 3.3d;
    assert null == array?[1];
}
doubleArray();
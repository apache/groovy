a.m(x: 1, y: 2) {
    println('named arguments');
}

a.m(x: 1, y: 2, z: 3) {
    println('named arguments');
} {
    println('named arguments');
}


a.m(x: 1, y: 2, z: 3)

{
    println('named arguments');
}

{
    println('named arguments');
}



a.m(1, 2) {
    println('normal arguments');
}

a.m(1, 2, 3) {
    println('normal arguments');
} {
    println('normal arguments');
}

a.m(1, 2, 3)

{
    println('normal arguments');
}


{
    println('normal arguments');
}




m {
    println('closure arguments');
}

m {
    println('closure arguments');
} {
    println('closure arguments');
}

m {
    println('closure arguments');
} {
    println('closure arguments');
} {
    println('closure arguments');
}


m

{
    println('closure arguments');
}

{
    println('closure arguments');
}

{
    println('closure arguments');
}

'm' {
    println('closure arguments');
}


1 {

}
1.1 {

}

-1 {

}

-1.1 {

}

1()
1.1()
1(1, 2, 3)
1.1(1, 2, 3)
-1()
-1.1()
-1(1, 2, 3)
-1.1(1, 2, 3)

1(1, 2) {

}

1.1(1, 2) {

}

-1(1, 2) {

}

-1.1(1, 2) {

}

hello(x: 1, y: 2, z: 3)
hello('a', 'b')
hello(x: 1, 'a', y: 2, 'b', z: 3)
hello('c', x: 1, 'a', y: 2, 'b', z: 3)


A[x: 1, y: 2]
A[*: someMap]
A[*: someMap, z: 3]
A[w: 0, *: someMap]
A[*: [x: 1, y: 2]]
A[*: [x: 1, y: 2], z: 3]
A[w: 0, *: [x: 1, y: 2]]

SomeMethod(a, b)


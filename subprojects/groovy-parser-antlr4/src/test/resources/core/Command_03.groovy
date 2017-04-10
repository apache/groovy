println withPool {
}

println aa("bb", "cc") {
}

println this.aa("bb", "cc") {
}

println aa("bb", {println 123;}, "cc") {
}

aa("bb", "cc") {
    println 1
} { println 2 }

cc  {
    println 1
} {
    println 2
}

dd {
    println 3
}

obj.cc  {
    println 1
} {
    println 2
}

bb 1, 2, {println 123;}

obj."some method" (groovy.xml.dom.DOMCategory) {
}

obj."some ${'method'}" (groovy.xml.dom.DOMCategory) {
}
obj.someMethod (groovy.xml.dom.DOMCategory) {
}

use (groovy.xml.dom.DOMCategory) {
}

['a','b','c'].inject('x') {
    result, item -> item + result + item
}

println a."${hello}"('world') {
}

println a."${"$hello"}"('world') {
}

a."${"$hello"}" 'world', {
}

a.<String, Object>someMethod 'hello', 'world';

a[x] b
a[x] b c
a[x] b c d

"$x"(1, 2) a
"$x" 1, 2  a

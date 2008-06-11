package org.codehaus.groovy.reflection

def types = [
        "boolean",
        "char",
        "byte",
        "short",
        "int",
        "long",
        "float",
        "double",
        "Object"
]

types.each { arg1 ->
    println "public Object invoke(Object receiver, $arg1 arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }"
    types.each { arg2 ->
        println "public Object invoke(Object receiver, $arg1 arg1, $arg2 arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }"
        types.each { arg3 ->
            println "public Object invoke(Object receiver, $arg1 arg1, $arg2 arg2, $arg3 arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }"
        }
    }
}
package org.codehaus.groovy.classgen

def ops = [
        "plus",
        "minus",
        "multiply",
        "div",
        "or",
        "and",
        "xor",
        "intdiv",
        "mod",
        "leftShift",
        "rightShift",
        "rightShiftUnsigned"
]

def numbers = ["Byte":"byte", "Short":"short", "Integer":"int", "Long":"long", "Float":"float", "Double":"double"]

ops.each { op ->
    numbers.each { wrappedType, type ->
        println "public boolean ${type}_${op};";    
    }
}

ops.each { op ->
    println "if (\"${op}\".equals(name)) {"
    numbers.each { wrappedType, type ->
        println """if (klazz==${wrappedType}.class) {
                ${type}_${op} = true;
            }"""
    }
    println "if (klazz==Object.class) {"
    numbers.each { wrappedType, type ->
        println "${type}_${op} = true;"
            }
    println "}"
    println "}"
}

ops.each { op ->
    numbers.each { wrappedType1, type1 ->
        numbers.each { wrappedType2, type2 ->
            def math = getMath(wrappedType1, wrappedType2)
            if (math [op]) {
                println """public static ${math.resType} ${op}(${type1} op1, ${type2} op2) {
                   if (instance.${type1}_${op}) {
                      return ${op}Slow(op1, op2);
                   }
                   else {
                      return ${math.resType != type1 ? "((" + math.resType+ ")op1)" : "op1"} ${math[op]} ${math.resType != type2 ? "((" + math.resType+ ")op2)" : "op2"};
                   }
                }"""
                println """private static ${math.resType} ${op}Slow(${type1} op1,${type2} op2) {
                      return ((Number)InvokerHelper.invokeMethod(op1, "${op}", op2)).${math.resType}Value();
                }"""
            }
        }
    }
}

def isFloatingPoint(number) {
    return number == "Double" || number == "Float";
}

def isLong(number) {
    return number == "Long";
}

def getMath (left, right) {
    if (isFloatingPoint(left) || isFloatingPoint(right)) {
        return [
                resType : "double",

                plus : "+",
                minus : "-",
                multiply : "*",
                div : "/",
        ];
    }
    if (isLong(left) || isLong(right)){
        return [
                resType : "long",

                plus : "+",
                minus : "-",
                multiply : "*",
                div : "/",
                or : "|",
                and : "&",
                xor : "^",
                intdiv : "/",
                mod : "%",
                leftShift : "<<",
                rightShift : ">>",
                rightShiftUnsigned : ">>>"
        ]
    }
    return [
            resType : "int",

            plus : "+",
            minus : "-",
            multiply : "*",
            div : "/",
            or : "|",
            and : "&",
            xor : "^",
            intdiv : "/",
            mod : "%",
            leftShift : "<<",
            rightShift : ">>",
            rightShiftUnsigned : ">>>"
    ]
}

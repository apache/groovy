package org.codehaus.groovy.classgen

def types = ["Integer", "Long", "Float", "Double"]

def getMath (a,b) {
    if (a == "Double" || b == "Double" || a == "Float" || b == "Float")
      return "FloatingPointMath"

    if (a == "Long" || b == "Long")
      return "LongMath"

    "IntegerMath"
}

println """
public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
    NumberMath m = NumberMath.getMath((Number)receiver, (Number)args[0]);
"""

types.each {
    a ->
    print """
    if (receiver instanceof $a) {"""
    types.each {
        b ->
        print """
        if (args[0] instanceof $b)
            return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                public final Object invoke(Object receiver, Object[] args) {
                    return ${getMath(a,b)}.INSTANCE.addImpl(($a)receiver,($b)args[0]);
                }

                public final Object invokeBinop(Object receiver, Object arg) {
                    return ${getMath(a,b)}.INSTANCE.addImpl(($a)receiver,($b)arg);
                }
            };
        """
    }
    println "}"
}

println """
    return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
        public final Object invoke(Object receiver, Object[] args) {
            return math.addImpl((Number)receiver,(Number)args[0]);
        }

        public final Object invokeBinop(Object receiver, Object arg) {
            return math.addImpl((Number)receiver,(Number)arg);
        }
}
"""

for (i in 2..256) {
    print "public Object invoke$i (Object receiver, "
    for (j in 1..(i-1)) {
        print "Object a$j, "
    }
    println "Object a$i) {"

    print "  return invoke (receiver, new Object[] {"

    for (j in 1..(i-1)) {
        print "a$j, "
    }
    println "a$i} );"

    println "}"
}

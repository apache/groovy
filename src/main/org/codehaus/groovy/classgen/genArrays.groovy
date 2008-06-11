package org.codehaus.groovy.classgen

print """

public class ArrayUtil {
   ${genMethods()}
}

"""

def genMethods () {
    def res = ""
    for (i in 1..250)
      res += "\n\n" + genMethod (i)
    res
}

def genMethod (int paramNum) {
    def res = "public static Object [] createArray ("
    for (k in 0..<paramNum) {
        res += "Object arg" + k
        if (k != paramNum-1)
          res += ", "
    }
    res += ") {\n"
    res += "return new Object [] {\n"
        for (k in 0..<paramNum) {
            res += "arg" + k
            if (k != paramNum-1)
              res += ", "
        }
        res += "};\n"
    res += "}"
    res
}

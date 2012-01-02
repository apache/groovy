/*
 * The Great Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Jochen Hinrichsen
 */

def dict = new TreeMap()

// read input, build dictionary
System.in.eachLine() { line ->
    // split on words
    line.split("\\W").each() { word ->
        def s = word.toLowerCase()
        def entry = dict[s]
        dict[s] = (entry == null) ? 1 : entry+1
    }
}

// default sort() is smallest first
// sort for multiple properties: [ it.value, it.key ]
assert dict != null
assert dict.values() != null
assert (dict.values().sort({ l, r -> r <=> l})) != null
dict.values().sort({ l, r -> r <=> l}).each() { value ->
    println "${value.toString().padLeft(8)}"
}

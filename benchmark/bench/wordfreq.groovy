#!/bin/env groovy
/*
	$Id: wordfreq.groovy,v 1.3 2005-09-25 20:16:20 igouy-guest Exp $

	The Great Computer Language Shootout
	http://shootout.alioth.debian.org/
 
	contributed by Jochen Hinrichsen
*/

// def dict = [:]
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
/*
	assert value != null
    def entry = dict.find() { e ->
        def v = e.getValue()
		assert v != null
        e.getValue() == value
    }
	assert entry != null
*/
    // println "${value.toString().padLeft(8)} ${entry.key}"
    println "${value.toString().padLeft(8)}"
}

// EOF


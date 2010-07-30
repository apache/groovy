#!/bin/env groovy
// 
// output tests in a junit xml report that took greater than specified time
//
// by Jeremy Rayner - 15 Dec 2004
//
// usage:   groovy BigTests.groovy <TEST.xml> <time in secs>

fileName = ""  // default
timeCutOff = new Float("1.0")

if (args.length > 1) {
    fileName = args[0]
    timeCutOff = new Float(args[1])
} else {
    println "usage: groovy BigTests.groovy <TEST.xml> <time in secs>"
}

testSuite = new XmlParser().parse(fileName)
name = testSuite['@name']
println "TestSuite: ${name}"
bigTests = [:]
testSuite.each {
    if ("testcase" == it.name()) {
        classname = it['@classname']
        name = it['@name']
        time = new Float(it['@time'])
        if (time > timeCutOff) {
            println "  ${time} - ${classname}.${name}()"
        }
    }
}


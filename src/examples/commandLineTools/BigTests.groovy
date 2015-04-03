#!/bin/env groovy
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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


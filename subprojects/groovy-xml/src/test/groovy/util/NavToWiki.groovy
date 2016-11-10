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
package util

if (args.size() < 1) {
    println "Usage: NavToWiki fileName"
}
else {
    file = args[0]
    println "About to parse ${file}"
    doc = new XmlParser().parse(file)

    println """
QuickLinks page
-------------------------------


"""
    links = doc.body.links.item
    println links.collect {
        return "{link:" + it['@name'] + "|" + it['@href'] + "}"
    }.join(" | ")

    println """



Navigation page
-------------------------------


"""
    menus = doc.body.menu
    menus.each {
        println "h3:${it['@name']}"

        it.item.each {
            println "- {link:" + it['@name'] + "|" + it['@href'] + "}"
        }
        println ""
    }
}


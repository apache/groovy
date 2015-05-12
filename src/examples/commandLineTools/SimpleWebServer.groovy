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
/**
 * Simple web server
 * @author <a href="mailto:jeremy.rayner@gmail.com">Jeremy Rayner</a>
 * 
 * invoke using
 *    groovy -l 80 SimpleWebServer.groovy
 * 
 *       (where 80 is the port to listen for requests upon)
 */
import java.io.File

if (init) { 
    headers = [:] 
    binaryTypes = ["gif","jpg","png"]          
    mimeTypes = [
        "css" : "text/css",         
        "gif" : "image/gif",
        "htm" : "text/html",         
        "html": "text/html",         
        "jpg" : "image/jpeg",         
        "png" : "image/png"
    ]                                 
}

// parse the request
if (line.toLowerCase().startsWith("get")) {
    content = line.tokenize()[1]
} else {
    h = line.tokenize(":")
    headers[h[0]] = h[1]
}

// all done, now process request
if (line.size() == 0) {
    processRequest()
    return "success"
}

// ------------------------

def processRequest() {
    if (content.indexOf("..") < 0) { //simplistic security
        // simple file browser rooted from current dir
        f = new File("." + content)
        if (f.isDirectory()) {
            printDirectoryListing(f)
        } else {
            extension = content.substring(content.lastIndexOf(".") + 1)
            printHeaders(mimeTypes.get(extension,"text/plain"))          
                      
            if (binaryTypes.contains(extension)) {
                socket.outputStream.write(f.readBytes())
            } else {
                println(f.text)
            }
        }
    }
}

def printDirectoryListing(f) {
    printHeaders("text/html")          
    println "<html><head></head><body>"
    for (i in f.list().toList().sort()) {
        if ("/" == content) { content = "" } // special case for root document
        println "<a href='${content}/${i}'>${i}</a><br>"
    }
    println "</body></html>"
}

def printHeaders(mimeType) {
    println "HTTP/1.0 200 OK"
    println "Content-Type: ${mimeType}"
    println ""          
}

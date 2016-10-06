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
package transforms.global

/**
* Demonstrates how a global transformation works. 
* 
* @author Hamlet D'Arcy
*/ 

def greet() {
    println "Hello World"
}
    
// this prints out Hello World along with the extra compile time logging
greet()


//
// The rest of this script is asserting that this all works correctly. 
//

// redirect standard out so we can make assertions on it
def standardOut = new ByteArrayOutputStream();
System.setOut(new PrintStream(standardOut)); 
  
greet()
def result = standardOut.toString("ISO-8859-1").split('\n')
assert "Starting greet"  == result[0].trim()
assert "Hello World"     == result[1].trim()
assert "Ending greet"    == result[2].trim()
standardOut.close()

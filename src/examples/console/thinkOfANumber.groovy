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
 * Simple game
 * @author: Jeremy Rayner
 * based on algorithms from INPUT/Marshall Cavendish/1984
 */
while (true) {
    try {
        int x = Math.random() * 6
        print "The computer has chosen a number between 0 and 5. Can you guess it?"
              
        def line = System.in.readLine()
        int g = line.toInteger()
        if (g == x) {
           println "Well done" 
        } else {
           println "Tough luck - you're wrong"
        }
     } catch (NumberFormatException e) {
         println "The computer didn't understand '$line'"
     }
}

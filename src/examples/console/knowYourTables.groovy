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
 * Simple mathematics quiz
 * @author: Jeremy Rayner
 * based on algorithms from INPUT/Marshall Cavendish/1984
 */
while (true) {
    try {
        def n = (int)(Math.random() * 12) + 1
        println "What is $n times 9?"
        def a = System.in.readLine().toInteger()
        if (a == n * 9) println "Correct"
              
    } catch (Exception e) {
        println "The computer didn't understand your input"
    }
}

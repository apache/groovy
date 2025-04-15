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
package groovy.benchmarks

class Loop2 {
  def array = new ArrayList()
  def pos = 0

  void push(obj){
     array[pos] = obj
     pos = pos + 1
  }

  Object pop(){
     pos = pos - 1
     return array[pos]
  }

  static void main(args){
     println "Starting the Loop2 test"
     def s = new Loop2()
     for (i in 1..1000000){
       s.push(i)
     }
     for (i in 1..1000000){
       s.pop()
     }
  }
}

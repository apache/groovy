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
package groovy

/** 
 * A dummy bean for testing the use of properties in Groovy
 */
class Foo implements Serializable {

    // public properties
    def name = "James"
    def count
    String location = "London"
    
    // declare private variables
    private blah = 9
    private invisible = "invisible"

    // declare a protected variable
    protected prot = "hide me!"

    // declare a bean with explicit typing
    private String body

    static void main(args) {
        def f = new Foo()
        println f
    }
    
    // provide a getter method
    def getCount() {
         if (count == null) {
             count = 1
         }
         return count
    }
     
    def getBlah() {
         return blah
    }

    public String getBody() {
        return this.body ? this.body : 'null'
    }

    public void setBody(String body) {
        this.body = body
    }

    String toString() {
        return super.toString() + " name: ${name} location: ${location}"
    }
}

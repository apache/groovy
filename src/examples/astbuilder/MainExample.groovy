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
package examples.astbuilder

/**
 * Exists to test the @Main annotation. At compile time, there will be a main()
 * method added to this class that has the same body as the greet() method. 
 * It can be invoked either by using either Java or Groovy to run the class. 
 *
 * @author Hamlet D'Arcy
 */
class MainExample {

    @Main
    public void greet() {
        println "Hello from the greet() method!"
    }
}

====
     Licensed to the Apache Software Foundation (ASF) under one
     or more contributor license agreements.  See the NOTICE file
     distributed with this work for additional information
     regarding copyright ownership.  The ASF licenses this file
     to you under the Apache License, Version 2.0 (the
     "License"); you may not use this file except in compliance
     with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on an
     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied.  See the License for the
     specific language governing permissions and limitations
     under the License.
====

Global AST Transformation Example

This example shows how to wire together a global transformation. 

The example requires ant in your path and the Groovy 1.6 (or greater) 
Jar in your classpath. The current directory must *not* be on your
classpath, otherwise ant will try to read the META-INF directory and
apply the transformations prematurely. 

To build the example run "ant" from the current directory. The default 
target will compile the classes needed. The last step of the build 
script prints out the command needed to run the example. 

To run the first example perform the following from the command line: 
  groovy -cp LoggingTransform.jar LoggingExample.groovy
  
The example should print: 
  Starting greet
  Hello World
  Ending greet

To run the second example perform the following from the command line: 
  groovy -cp LoggingTransform.jar CompiledAtExample.groovy
  
The example should print: 
  Scripted compiled at: [recently]
  Class compiled at: [recently]

No exceptions should occur. 

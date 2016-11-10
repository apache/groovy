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
// tag::event[]
methodNotFound { receiver, name, argList, argTypes, call ->
    // receiver is the inferred type of the receiver
    // name is the name of the called method
    // argList is the list of arguments the method was called with
    // argTypes is the array of inferred types for each argument
    // call is the method call for which we couldn’t find a target method
    if (receiver==classNodeFor(String)
            && name=='longueur'
            && argList.size()==0) {
        handled = true
        return newMethod('longueur', classNodeFor(String))
    }
}
// end::event[]
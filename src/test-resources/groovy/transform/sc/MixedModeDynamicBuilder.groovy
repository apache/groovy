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
import groovy.xml.MarkupBuilder

setup {
    debug = true
    newScope() // make sure currentScope is always not null
}

methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (receiver==classNodeFor(MarkupBuilder) && argTypes[-1]==CLOSURE_TYPE) {
        // we recognized a call directly made on markupBuilder, like in
        // mkp.html { ... }
        // so we create a new "scope" so that subsequent unresolved calls are made dynamically
        newScope {
            dynamic = call
        }
        return makeDynamic(call)
    } else {
        // check if we're inside a builder
        if (currentScope.dynamic && isMethodCallExpression(call) && call.implicitThis) {
            return makeDynamic(call)
        }
    }

}

afterMethodCall { call ->
    // we need to recognize whenever we're exiting the scope of the dynamic builder
    if (call.is(currentScope.dynamic)) {
        log "Exiting scope of $call.text"
        scopeExit()
    }
}
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
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException

// An simple extension that will change the prefix of type checking errors
setup {
    context.pushErrorCollector() // collect every type checking error using a dedicated error collector
}

finish {
    def ec = context.popErrorCollector()
    def co = context.errorCollector
    ec.errors.each { err ->
        if (err instanceof SyntaxErrorMessage && err.cause.message.startsWith('[Static type checking] - ')) {
            err.cause = new SyntaxException(
                    err.cause.message.replace('[Static type checking]','[Custom]'),
                    err.cause.cause,
                    err.cause.startLine,
                    err.cause.startColumn,
                    err.cause.endLine,
                    err.cause.endColumn,
            )
        }
        co.addError(err)
    }
}
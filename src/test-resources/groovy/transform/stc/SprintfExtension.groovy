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
afterMethodCall { mc ->
    def method = getTargetMethod(mc)
    if (isExtensionMethod(method) && method.name == 'sprintf') {
        def argList = getArguments(mc)
        if (argList && isConstantExpression(argList[0])) {
            def pattern = argList[0].text
            def codes = pattern.replaceAll(/[^%]*%([a-zA-Z]+)/, '_$1').tokenize('_')
            def args = getArgumentTypes(argList).toList().tail()
            if (args.size() != codes.size()) {
                addStaticTypeError("Found ${args.size()} parameters for sprintf call with ${codes.size()} conversion code placeholders in the format string", argList)
                return
            }
            def codeTypes = codes.collect { code ->
                switch (code) {
                    case 's': return STRING_TYPE
                    case 'd': return int_TYPE
                    case 'tF': return classNodeFor(Date)
                    default: return null
                }
            }
            if (codeTypes != args) {
                addStaticTypeError("Parameter types didn't match types expected from the format String: ", argList)
                (0..<args.size()).findAll { args[it] != codeTypes[it] }.each { n ->
                    String msg = "For placeholder ${n + 1} [%${codes[n]}] expected '${codeTypes[n].toString(false)}' but was '${args[n].toString(false)}'"
                    addStaticTypeError(msg, argList.getExpression(n + 1))
                }
            }
        }
    }
}

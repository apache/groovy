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
setup {
    newScope()
    currentScope.m1 = newMethod('foo', String)
    currentScope.m2 = newMethod('bar') {
        int_TYPE
    }
}
finish {
    def m1 = currentScope.m1
    def m2 = currentScope.m2
    assert isGenerated(m1)
    assert isGenerated(m2)
    assert m1.returnType == STRING_TYPE
    assert m2.returnType == int_TYPE
    addStaticTypeError 'Extension was executed properly', context.source.AST.classes[0]
}

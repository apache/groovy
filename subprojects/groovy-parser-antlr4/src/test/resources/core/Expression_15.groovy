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
person.@name
person.child.@name
person.@name.count
person.&sayHello
person.child.&sayHello
person?.child
person?.@child
person*.child
person*.@child
person.'name'
person.@'name'
person?.'name'
person?.@'name'
person*.'child'
person*.@'child'
person.&'sayHello'
person.new
person.@new
person?.new
person?.@new
person*.new
person*.@new
person.&new
person."$name"
person.@"$name"
person?."$name"
person?.@"$name"
person*."$name"
person*.@"$name"
person.&"$methodName"
person.("$firstname" + "$lastname")
person.@("$firstname" + "$lastname")
person?.("$firstname" + "$lastname")
person?.@("$firstname" + "$lastname")
person*.("$firstname" + "$lastname")
person*.@("$firstname" + "$lastname")
person.&("$method" + "$name")

a.b?.c[1, 2, 3]*.d
a[1, *[2, 3]]*.b
a[*[2, 3]]*.b

person
*.
child
*.
@child
?.
child
?.
@child
.
child
        .
@name
.&
length

a.b()
a.'b'()
a."$b"()
a?.b()
a*.b()
a()
'a'()
"$a"()

obj.a
    .b()
    ?.c()
    *.d()

obj.a(1, 2)
a(1, 2)

obj.a(x: 1, y: 2)
a(x: 1, y: 2)

a.@b()
a.@b(1, 2, 3)
a?.@b()
a?.@b(1, 2, 3)
a*.@b()
a*.@b(1, 2, 3)

a.<Integer>b(1, 2)

a
        .
<Integer>b(1, 2)

a.<Integer, String>b(1, '2')
a?.<Integer, String>b(1, '2')
a*.<Integer, String>b(1, '2')
obj?.a*.<
        Integer,
        String
        >b(1, '2')

String[] codes = [
        className + '.' + propertyName + '.typeMismatch.error',
        className + '.' + propertyName + '.typeMismatch',
        classAsPropertyName + '.' + propertyName + '.typeMismatch.error',
        classAsPropertyName + '.' + propertyName + '.typeMismatch',
        bindingResult.resolveMessageCodes('typeMismatch', propertyName)
].flatten() as String[]


person*.child[1, 2 + 6, *[3, 4]]*.@child*.@child()?.@child().@child()?.child?.@child.child.getChild().getChild(1, 2).getChild(name: 'a', age: 2).'child'."$child".('chi' + 'ld').@name.class.&equals


(obj.x)()
(obj.@x)()


static.unused = { -> }

ResolveOptions resolveOptions = new ResolveOptions()\
            .setConfs(['default'] as String[])\
            .setOutputReport(false)\
            .setValidate(args.containsKey('validate') ? args.validate : false)

new A("b") C.d()

m()()

a = {a,b-> }()

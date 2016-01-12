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

class KeywordsInPropertyNamesTest extends GroovyTestCase {

    void testKeywords() {
        def value = "returnValue"
        StaticAndDefaultClass.metaClass.static.dynStaticMethod = {-> value }
        assert value == StaticAndDefaultClass.dynStaticMethod()

        StaticAndDefaultClass.metaClass.default = value
        StaticAndDefaultClass.metaClass.goto = value
        assert value == new StaticAndDefaultClass().default
        assert value == new StaticAndDefaultClass().goto
        assert String.package.name == 'java.lang'
    }

    void testModifierKeywordsAsMapKeys() {
        def map = [
                private: 1, public: 2, protected: 3, static: 4,
                transient: 5, final: 6, abstract: 7, native: 8,
                threadsafe: 9, synchronized: 10, volatile: 11, strictfp: 12
        ]
        assert 1..12 == [
                map.private, map.public, map.protected, map.static,
                map.transient, map.final, map.abstract, map.native,
                map.threadsafe, map.synchronized, map.volatile, map.strictfp
        ]
    }

    void testBuiltInTypeKeywordsAsExpandoKeys() {
        def e = new Expando(void: 1, boolean: 2, byte: 3, char: 4, short: 5, int: 6)
        e.float = 7
        e.long = 8
        e.double = 9
        assert 1..9 == [
                e.void, e.boolean, e.byte,
                e.char, e.short, e.int,
                e.float, e.long, e.double
        ]
    }

    void testMapWithKeywords() {
        def d = new Date()
        def map = [
                (d): 'foo',
                null: 'bar',
                (null): 'baz',

                as: 'shown',
                assert: 'true',
                break: 'free',
                case: 'tool',
                catch: 'cold',
                class: 'action',
                const: 'flux',
                continue: 'on',
                def: 'leppard',
                default: 'loan',
                do: 'nothing',
                else: 'where',
                enum: 'erate',
                extends: 'over',
                false: 'start',
                finally: 'finished',
                for: 'ever',
                goto: 'jail',
                if: 'then',
                implements: 'interface',
                import: 'tax',
                in: 'trouble',
                instanceof: 'abuse',
                interface: 'with',
                new: 'car',
                package: 'wrapped',
                return: 'home',
                super: 'duper',
                switch: 'off',
                this: 'time',
                throw: 'away',
                throws: 'up',
                true: 'love',
                try: 'again',
                while: 'away',
        ]
        assert map[d] == 'foo'
        assert map.null == 'bar'
        assert map[null] == 'baz'

        assert map.as == 'shown'
        assert map.assert == 'true'
        assert map.break == 'free'
        assert map.case == 'tool'
        assert map.catch == 'cold'
        assert map.class == 'action'
        assert map.const == 'flux'
        assert map.continue == 'on'
        assert map.def == 'leppard'
        assert map.default == 'loan'
        assert map.do == 'nothing'
        assert map.else == 'where'
        assert map.enum == 'erate'
        assert map.extends == 'over'
        assert map.false == 'start'
        assert map.finally == 'finished'
        assert map.for == 'ever'
        assert map.goto == 'jail'
        assert map.if == 'then'
        assert map.implements == 'interface'
        assert map.import == 'tax'
        assert map.in == 'trouble'
        assert map.instanceof == 'abuse'
        assert map.interface == 'with'
        assert map.new == 'car'
        assert map.package == 'wrapped'
        assert map.return == 'home'
        assert map.super == 'duper'
        assert map.switch == 'off'
        assert map.this == 'time'
        assert map.throw == 'away'
        assert map.throws == 'up'
        assert map.true == 'love'
        assert map.try == 'again'
        assert map.while == 'away'
    }
}

class StaticAndDefaultClass {}
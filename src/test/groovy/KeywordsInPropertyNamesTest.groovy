package groovy

import gls.CompilableTestSupport

class KeywordsInPropertyNamesTest extends CompilableTestSupport {

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
                def: 'ault',
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
                implements: '',
                import: 'tax',
                in: 'trouble',
                instanceof: 'kindof',
                interface: 'with',
                new: 'car',
                package: 'wrapped',
                return: 'home',
                switch: 'off',
                throw: 'away',
                throws: 'like a g...',
                true: 'love',
                try: 'again',
                while: 'u were sleeping',
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
        assert map.def == 'ault'
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
        assert map.implements == ''
        assert map.import == 'tax'
        assert map.in == 'trouble'
        assert map.instanceof == 'kindof'
        assert map.interface == 'with'
        assert map.new == 'car'
        assert map.package == 'wrapped'
        assert map.return == 'home'
        assert map.switch == 'off'
        assert map.throw == 'away'
        assert map.throws == 'like a g...'
        assert map.true == 'love'
        assert map.try == 'again'
        assert map.while == 'u were sleeping'
    }

    void testCantUseSuperAsPropertyName() {
        shouldNotCompile """
            def m = [super: "cool"]
            assert m.super == "cool"
        """.stripIndent()
    }
}

class StaticAndDefaultClass {}
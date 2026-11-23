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
package bugs

import groovy.lang.ExpandoMetaClass
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ReflectionCache
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * An {@link ExpandoMetaClass} accessor is a property accessor when its suffix
 * is already in the form {@code BeanUtils.capitalize} produces. That includes
 * a property whose name does not start with a letter ({@code _ajax}, {@code $id}),
 * which is the case a {@code get}/{@code set}/{@code is} prefix does not capitalize.
 */
final class Groovy3056 {

    @Test
    void underscoreGetterIsUsedByGetPropertyAndByDottedRead() {
        assertScript '''
            class Holder {
                def _ajax = 'field'
                def other = 'other'
            }
            // An instance created before the class meta class is replaced keeps
            // that meta class. Only instances created afterwards see the override.
            def early = new Holder()
            Holder.metaClass.getOther = { -> 'other-over' }
            Holder.metaClass.get_ajax = { -> 'ajax-over' }
            def name = '_ajax'
            Holder.metaClass."get${name}" = { -> 'ajax-over' }

            assert early.getProperty('other') == 'other'
            assert early.getProperty('_ajax') == 'field'

            def later = new Holder()
            assert later.getProperty('other') == 'other-over'
            assert later.other == 'other-over'
            assert later.getProperty('_ajax') == 'ajax-over'
            assert later._ajax == 'ajax-over'
            assert later.get_ajax() == 'ajax-over'
            assert later.getProperty("${'_ajax'}") == 'ajax-over'
            assert later.getProperty("${name}") == 'ajax-over'

            Holder.metaClass.get_ajax = { -> 'second' }
            assert new Holder().getProperty('_ajax') == 'second'
            assert Holder.metaClass.hasMetaProperty('_ajax')
        '''
    }

    @Test
    void underscoreSetterIsUsedBySetPropertyAndByDottedWrite() {
        assertScript '''
            class Holder {
                def _ajax = 'field'
                def seen = null
            }
            Holder.metaClass.set_ajax = { value ->
                delegate.seen = value
            }
            def holder = new Holder()
            holder.setProperty('_ajax', 'by-name')
            assert holder.seen == 'by-name'

            holder._ajax = 'by-dot'
            assert holder.seen == 'by-dot'
            holder.setProperty("${'_ajax'}", 'by-gstring')
            assert holder.seen == 'by-gstring'

            holder.set_ajax('by-call')
            assert holder.seen == 'by-call'
        '''
    }

    @Test
    void getterAndSetterRoundTripTheSameUnderscoreProperty() {
        assertScript '''
            class Holder {
                def _ajax = 'field'
            }
            def store = 'shadow'
            Holder.metaClass.get_ajax = { -> store }
            Holder.metaClass.set_ajax = { value -> store = value }
            def holder = new Holder()
            assert holder._ajax == 'shadow'
            assert holder.getProperty('_ajax') == 'shadow'
            holder._ajax = 'next'
            assert holder._ajax == 'next'
            assert holder.@_ajax == 'field'
            assert store == 'next'
        '''
    }

    @Test
    void newUnderscorePropertyCanBeIntroducedByAssignmentOrAppend() {
        assertScript '''
            class Holder {
            }
            Holder.metaClass.get_created = { -> 'assigned' }
            assert Holder.metaClass.hasMetaProperty('_created')
            assert new Holder().getProperty('_created') == 'assigned'
            assert new Holder()._created == 'assigned'

            class Fresh {
            }
            Fresh.metaClass.get_appended << { -> 'appended' }
            assert Fresh.metaClass.hasMetaProperty('_appended')
            assert new Fresh()._appended == 'appended'
        '''
    }

    @Test
    void booleanUnderscorePropertyHonorsIsAndGetOverrides() {
        assertScript '''
            class Flag {
                boolean _on = false
            }
            Flag.metaClass.is_on = { -> true }
            assert new Flag()._on
            assert new Flag().getProperty('_on')
            assert new Flag().is_on()

            class FlagGet {
                boolean _on = false
            }
            FlagGet.metaClass.get_on = { -> true }
            assert new FlagGet()._on
            assert new FlagGet().getProperty('_on')
            assert new FlagGet().get_on()
        '''
    }

    @Test
    void isAccessorForANonBooleanUnderscorePropertyIsStillAGetter() {
        assertScript '''
            class Holder {
                String _name = 'field'
            }
            Holder.metaClass.is_name = { -> 'over' }
            assert new Holder().getProperty('_name') == 'over'
            assert new Holder()._name == 'over'
            assert new Holder().is_name() == 'over'
        '''
    }

    @Test
    void staticUnderscoreAccessorIsUsedForReadsAndWrites() {
        assertScript '''
            class Holder {
                static _count = 'static'
                static seen = null
            }
            Holder.metaClass.'static'.get_count = { -> 'over' }
            Holder.metaClass.'static'.set_count = { value -> Holder.seen = value }
            assert Holder._count == 'over'
            assert Holder.metaClass.getProperty(Holder, '_count') == 'over'
            Holder._count = 'written'
            assert Holder.seen == 'written'
        '''
    }

    @Test
    void dollarAndRepeatedUnderscorePropertiesUseTheSameAccessorRule() {
        assertScript '''
            class Dollar {
                def $id = 'field'
            }
            Dollar.metaClass.'get$id' = { -> 'over' }
            Dollar.metaClass.'set$id' = { value -> delegate.@$id = value }
            assert new Dollar().getProperty('$id') == 'over'
            def dollar = new Dollar()
            dollar.setProperty('$id', 'written')
            assert dollar.@$id == 'written'

            class Repeated {
                def __id = 'field'
            }
            Repeated.metaClass.get__id = { -> 'over' }
            Repeated.metaClass.set__id = { value -> delegate.@__id = value }
            assert new Repeated().getProperty('__id') == 'over'
            def repeated = new Repeated()
            repeated.__id = 'written'
            assert repeated.@__id == 'written'
        '''
    }

    @Test
    void digitLeadingAccessorDefinesAPropertyReadableByName() {
        assertScript '''
            class Holder {
            }
            Holder.metaClass.get1st = { -> 'one' }
            assert Holder.metaClass.hasMetaProperty('1st')
            assert Holder.metaClass.getPropertyForSetter('set1st') == '1st'
            assert new Holder().getProperty('1st') == 'one'
        '''
    }

    @Test
    void metaClassDslRegistersAnUnderscoreGetter() {
        assertScript '''
            class Holder {
                def _ajax = 'field'
            }
            Holder.metaClass {
                get_ajax { -> 'dsl' }
            }
            assert new Holder().getProperty('_ajax') == 'dsl'
        '''
    }

    @Test
    void perInstanceOverrideDoesNotLeakToOtherInstances() {
        assertScript '''
            class Holder {
                def _ajax = 'field'
                def other = 'other'
            }
            def a = new Holder()
            def b = new Holder()
            a.metaClass.getOther = { -> 'only-other' }
            a.metaClass.get_ajax = { -> 'only-a' }
            assert a.other == 'only-other'
            assert a.getProperty('_ajax') == 'only-a'
            assert a._ajax == 'only-a'
            assert b.other == 'other'
            assert b.getProperty('_ajax') == 'field'
            assert b._ajax == 'field'
        '''
    }

    @Test
    void subclassReadsAnInheritedUnderscoreGetter() {
        try {
            assertScript '''
                class Parent {
                    def _ajax = 'ajax'
                    def other = 'other'
                }
                class Child extends Parent {
                }
                ExpandoMetaClass.enableGlobally()
                try {
                    Parent.metaClass.getOther = { -> 'other-over' }
                    Parent.metaClass.get_ajax = { -> 'ajax-over' }
                    def child = new Child()
                    assert child.other == 'other-over'
                    assert child.getProperty('_ajax') == 'ajax-over'
                    assert child._ajax == 'ajax-over'
                } finally {
                    ExpandoMetaClass.disableGlobally()
                }
            '''
        } finally {
            ExpandoMetaClass.disableGlobally()
        }
    }

    @Test
    void existingAccessorFormsKeepWorkingAndNonAccessorsStayMethods() {
        assertScript '''
            class Holder {
                def foo = 'foo'
                def other = 'other'
                def uRL = 'url'
                def URL = 'URL'
                def _Ajax = 'cap'
                boolean flag = false
            }
            Holder.metaClass.getfoo = { -> 'no' }
            Holder.metaClass.setfoo = { value -> delegate.@seen = value }
            Holder.metaClass.getOther = { -> 'yes' }
            Holder.metaClass.getuRL = { -> 'url-over' }
            Holder.metaClass.getURL = { -> 'URL-over' }
            Holder.metaClass.get_Ajax = { -> 'cap-over' }
            Holder.metaClass.isflag = { -> true }

            def holder = new Holder()
            assert holder.foo == 'foo'
            assert holder.getProperty('foo') == 'foo'
            assert holder.getfoo() == 'no'
            holder.foo = 'written'
            assert holder.foo == 'written'
            assert holder.@foo == 'written'

            assert holder.other == 'yes'
            assert holder.uRL == 'url-over'
            assert holder.URL == 'URL-over'
            assert holder.getProperty('_Ajax') == 'cap-over'
            assert !holder.flag
            assert holder.isflag()

            Holder.metaClass.isFlag = { -> true }
            assert new Holder().flag
            assert new Holder().isFlag()
        '''
    }

    @Test
    void wrongArityUnderscoreMethodsAreNotAccessors() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            class Holder {
                def _ajax = 'field'
                def other = 'other'
            }
            Holder.metaClass.get_ajax = { String value -> value }
            Holder.metaClass.set_ajax = { -> 'ignored' }
            def holder = new Holder()
            assert holder.getProperty('_ajax') == 'field'
            assert holder._ajax == 'field'
            assert holder.get_ajax('z') == 'z'
            holder._ajax = 'written'
            assert holder.@_ajax == 'written'

            shouldFail(GroovyRuntimeException) {
                Holder.metaClass.getOther << { -> 'append' }
            }
        '''
    }

    @Test
    void propertyNameForASingleUnderscoreIsRecognized() {
        assertScript '''
            class Holder {
                def _ = 'field'
            }
            Holder.metaClass.get_ = { -> 'over' }
            Holder.metaClass.set_ = { value -> delegate.@_ = value }
            assert new Holder().getProperty('_') == 'over'
            def holder = new Holder()
            holder.setProperty('_', 'written')
            assert holder.@_ == 'written'
        '''
    }

    @Test
    void setterRecognitionFollowsTheAccessorSuffixRule() {
        def emc = new ExpandoMetaClass(RecognitionTarget, false, true)
        CachedClass stringType = ReflectionCache.getCachedClass(String)
        CachedClass[] one = [stringType] as CachedClass[]
        CachedClass[] two = [stringType, stringType] as CachedClass[]
        CachedClass[] none = CachedClass.EMPTY_ARRAY

        assert emc.isSetter('set_ajax', one)
        assert emc.isSetter('set_', one)
        assert emc.isSetter('set__id', one)
        assert emc.isSetter('set$id', one)
        assert emc.isSetter('set1st', one)
        assert emc.isSetter('setFoo', one)
        assert emc.isSetter('setURL', one)
        assert emc.isSetter('setuRL', one)
        assert emc.isSetter('set_Ajax', one)

        assert !emc.isSetter('setfoo', one)
        assert !emc.isSetter('set', one)
        assert !emc.isSetter('is_ajax', one)
        assert !emc.isSetter('set_ajax', none)
        assert !emc.isSetter('set_ajax', two)
        assert !emc.isSetter('set_ajax', null)
        assert !emc.isSetter(null, one)
        assert !emc.isSetter('', one)

        assert emc.getPropertyForSetter('set_ajax') == '_ajax'
        assert emc.getPropertyForSetter('set_') == '_'
        assert emc.getPropertyForSetter('set__id') == '__id'
        assert emc.getPropertyForSetter('set$id') == '$id'
        assert emc.getPropertyForSetter('set1st') == '1st'
        assert emc.getPropertyForSetter('setFoo') == 'foo'
        assert emc.getPropertyForSetter('setURL') == 'URL'
        assert emc.getPropertyForSetter('setuRL') == 'uRL'
        assert emc.getPropertyForSetter('set_Ajax') == '_Ajax'
        assert emc.getPropertyForSetter('setfoo') == 'foo'
        assert emc.getPropertyForSetter(null) == null
        assert emc.getPropertyForSetter('') == null
        assert emc.getPropertyForSetter('ajax') == null
    }

    /** Class token used only as the subject of an unregistered meta class. */
    static class RecognitionTarget {
    }
}

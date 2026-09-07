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
package groovy.swing

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.swing.plaf.metal.DefaultMetalTheme
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.metal.MetalTheme

import static groovy.test.GroovyAssert.shouldFail

class LookAndFeelHelperThemeTest {

    // the handler sets MetalLookAndFeel.currentTheme (a global static) as a side effect;
    // save and restore it so the tests do not leak state into one another
    private MetalTheme originalTheme

    @BeforeEach
    void rememberTheme() {
        originalTheme = MetalLookAndFeel.currentTheme
    }

    @AfterEach
    void restoreTheme() {
        MetalLookAndFeel.currentTheme = originalTheme
    }

    // the Metal 'theme' attribute handler; invoked directly so the test does not
    // touch UIManager and therefore runs headless
    private static metalThemeHandler() {
        LookAndFeelHelper.instance.@extendedAttributes['javax.swing.plaf.metal.MetalLookAndFeel'].theme
    }

    @Test
    void rejectsNonMetalThemeClassWithoutConstructingIt() {
        ThemeProbeFlag.constructed = false
        def handler = metalThemeHandler()
        shouldFail(IllegalArgumentException) {
            handler(null, 'groovy.swing.NonMetalThemeProbe')
        }
        assert !ThemeProbeFlag.constructed: 'a non-MetalTheme class must not be constructed'
    }

    @Test
    void acceptsMetalThemeSubclass() {
        def handler = metalThemeHandler()
        // the handler returns the theme it set, so assert on that rather than the global
        def result = handler(null, 'groovy.swing.CustomMetalThemeProbe')
        assert result instanceof CustomMetalThemeProbe
    }

    @Test
    void rejectsAbstractMetalThemeSubclass() {
        def handler = metalThemeHandler()
        def err = shouldFail(IllegalArgumentException) {
            handler(null, 'groovy.swing.AbstractMetalThemeProbe')
        }
        assert err.message.contains('abstract')
    }

    @Test
    void rejectsMetalThemeWithoutNoArgConstructor() {
        def handler = metalThemeHandler()
        def err = shouldFail(IllegalArgumentException) {
            handler(null, 'groovy.swing.NoDefaultCtorThemeProbe')
        }
        assert err.message.contains('no-argument constructor')
    }
}

class ThemeProbeFlag {
    static boolean constructed = false
}

class NonMetalThemeProbe {
    NonMetalThemeProbe() {
        ThemeProbeFlag.constructed = true
    }
}

class CustomMetalThemeProbe extends DefaultMetalTheme {
}

abstract class AbstractMetalThemeProbe extends DefaultMetalTheme {
}

class NoDefaultCtorThemeProbe extends DefaultMetalTheme {
    NoDefaultCtorThemeProbe(String required) {
    }
}

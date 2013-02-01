/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.swing

import groovy.ui.Console

import groovy.ui.ConsoleActions
import groovy.ui.view.BasicMenuBar
import groovy.ui.view.MacOSXMenuBar

import java.awt.*

class SwingBuilderConsoleTest extends GroovySwingTestCase {

    void testMacOSXMenuBarHasBasicMenuBarSubElements() {

        testInEDT {
            def binding = new Binding()
            binding.setVariable("controller", new Console())

            final basicMenuBarScript = new BasicMenuBar()
            final macOSXMenuBarScript = new MacOSXMenuBar()
            final consoleActions = new ConsoleActions()

            def swing = new SwingBuilder()
            swing.controller = new Console()

            // we need to init the console actions - menu bar referes to them
            swing.build(consoleActions)

            def basicMenuBar = swing.build(basicMenuBarScript)
            def macOSXMenuBar = swing.build(macOSXMenuBarScript)

            if (macOSXMenuBar) { // null on windows
                // check if we have to same main menu items
                assert macOSXMenuBar.subElements*.text == basicMenuBar.subElements*.text - 'Help'

                // check whether the amount of sub menu elements and their types complies to the basic menu bar
                macOSXMenuBar.subElements.eachWithIndex { menu, i ->
                    assert menu.subElements.size() == basicMenuBar.subElements[i].subElements.size()
                    assert menu.subElements*.class == basicMenuBar.subElements[i].subElements*.class
                }
            }
        }
    }

}

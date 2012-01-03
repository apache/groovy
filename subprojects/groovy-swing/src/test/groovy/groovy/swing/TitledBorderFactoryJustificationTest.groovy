/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.swing

import javax.swing.border.TitledBorder
import groovy.swing.factory.TitledBorderFactory

/**
 * TitledBorderFactoryJustificationTest
 * Verifies that the justification attribute on the Factory gets passed through correctly to the TitledBorder
 */
class TitledBorderFactoryJustificationTest extends GroovySwingTestCase {
    void "test justification is set to the left"() {
            testJustificationIsSet(TitledBorder.LEFT, 'left')
    }

    void "test justification is set to the center"() {
            testJustificationIsSet(TitledBorder.CENTER, 'center')
    }

    void "test justification is set to the right"() {
            testJustificationIsSet(TitledBorder.RIGHT, 'right')
    }

    void "test justification is set to leading"() {
            testJustificationIsSet(TitledBorder.LEADING, 'leading')
    }

    void "test justification is set to trailing"() {
            testJustificationIsSet(TitledBorder.TRAILING, 'trailing')
    }

    void testJustificationIsSet(int justification, justificationString) {
        testInEDT {
            def swing = new SwingBuilder()
                swing.frame{
    
                def tbf = new TitledBorderFactory()
                TitledBorder titledBorder = tbf.newInstance(swing, "TestBorder", "", [justification:justificationString])
    
                assert justification == titledBorder.titleJustification:"justification should be -> ($justificationString)"
            }
        }
    }
}

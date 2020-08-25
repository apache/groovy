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
import gls.CompilableTestSupport

class DesignPatternsTest extends CompilableTestSupport {

    void testSimpleExample() {
        shouldCompile '''
        // tag::simple_example[]
        import groovy.swing.SwingBuilder
        import java.awt.BorderLayout as BL
        
        count = 0
        new SwingBuilder().edt {
          frame(title: 'Frame', size: [250, 75], show: true) {
            borderLayout()
            textlabel = label(text: 'Click the button!', constraints: BL.NORTH)
            button(text:'Click Me',
                 actionPerformed: {count++; textlabel.text = "Clicked ${count} time(s)."; println "clicked"}, constraints:BL.SOUTH)
          }
        }
        // end::simple_example[]
        '''
    }

    void testMoreInvolvedExample() {
        shouldCompile '''
        // tag::more_involved_example[]
        import groovy.swing.SwingBuilder
        import javax.swing.*
        import java.awt.*

        def swing = new SwingBuilder()

        def sharedPanel = {
             swing.panel() {
                label("Shared Panel")
            }
        }

        count = 0
        swing.edt {
            frame(title: 'Frame', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, pack: true, show: true) {
                vbox {
                    textlabel = label('Click the button!')
                    button(
                        text: 'Click Me',
                        actionPerformed: {
                            count++
                            textlabel.text = "Clicked ${count} time(s)."
                            println "Clicked!"
                        }
                    )
                    widget(sharedPanel())
                    widget(sharedPanel())
                }
            }
        }
        // end::more_involved_example[]
        '''
    }
    
    void testObservableBindingExample() {
        shouldCompile '''
        // tag::observable_binding_example[]
        import groovy.swing.SwingBuilder
        import groovy.beans.Bindable

        class MyModel {
           @Bindable int count = 0
        }

        def model = new MyModel()
        new SwingBuilder().edt {
          frame(title: 'Java Frame', size: [100, 100], locationRelativeTo: null, show: true) {
            gridLayout(cols: 1, rows: 2)
            label(text: bind(source: model, sourceProperty: 'count', converter: { v ->  v? "Clicked $v times": ''}))
            button('Click me!', actionPerformed: { model.count++ })
          }
        }
        // end::observable_binding_example[]
        '''
    }
}

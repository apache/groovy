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

import groovy.beans.Bindable
import groovy.beans.Vetoable

import javax.swing.DefaultBoundedRangeModel
import javax.swing.DefaultButtonModel
import javax.swing.DefaultListModel
import javax.swing.ListSelectionModel
import javax.swing.SpinnerNumberModel
import javax.swing.text.PlainDocument
import java.awt.event.ActionEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyVetoException
import java.text.SimpleDateFormat

class SwingBuilderBindingsTest extends GroovySwingTestCase {

    void testSliderValueBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                slider(id: 'sl')
                textField(id: 'txt', text: bind(source: sl, sourceProperty: 'value', id: 'binding'))
                slider(id: 'slReverse', value: bind(source: sl, sourceProperty: 'value', id: 'bindingReverse'))
                // need to use a second slider for reverse test, because string->int autobox, not so happy
            }

            swing.sl.value = 10
            assert swing.txt.text == '10'
            swing.sl.value = 95
            assert swing.txt.text == '95'

            swing.binding.rebind()
            swing.sl.value = 42
            assert swing.txt.text == '42'
            swing.binding.unbind()
            swing.sl.value = 13
            assert swing.txt.text == '42'
            swing.binding.bind()
            assert swing.txt.text == '42'
            swing.binding.update()
            assert swing.txt.text == '13'

            swing.sl.model = new DefaultBoundedRangeModel(30, 1, 20, 40)
            assert swing.txt.text == '30'

            // first make sure we've been fireing
            assert swing.slReverse.value == 30
            swing.slReverse.value = 21
            swing.bindingReverse.reverseUpdate()
            assert swing.sl.value == 21
        }
    }

    void testSpinnerValueBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                spinner(id: 'sp', model: spinnerNumberModel(minimum: 0, maximum: 100, stepSize: 5))
                textField(id: 'txt', text: bind(source: sp, sourceProperty: 'value', id: 'binding'))
                spinner(id: 'spReverse', value: bind(source: sp, sourceProperty: 'value', id: 'bindingReverse'))
                // need to use a second spinner for reverse test, because string->int autobox, not so happy
            }

            swing.sp.value = 10
            assert swing.txt.text == '10'
            swing.sp.value = 95
            assert swing.txt.text == '95'

            swing.binding.rebind()
            swing.sp.value = 42
            assert swing.txt.text == '42'
            swing.binding.unbind()
            swing.sp.value = 13
            assert swing.txt.text == '42'
            swing.binding.bind()
            assert swing.txt.text == '42'
            swing.binding.update()
            assert swing.txt.text == '13'

            swing.sp.model = new SpinnerNumberModel(30, 20, 40, 1)
            assert swing.txt.text == '30'

            // first make sure we've been fireing
            assert swing.spReverse.value == 30
            swing.spReverse.value = 21
            swing.bindingReverse.reverseUpdate()
            assert swing.sp.value == 21
        }
    }

    void testScrollBarValueBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            swing.actions {
                scrollBar(id: 'sb')
                textField(id: 'txt', text: bind(source: sb, sourceProperty: 'value', id: 'binding'))
                scrollBar(id: 'sbReverse', value: bind(source: sb, sourceProperty: 'value', id: 'bindingReverse'))
                // need to use a second scrollBar for reverse test, because string->int autobox, not so happy
            }

            swing.sb.value = 10
            assert swing.txt.text == '10'
            swing.sb.value = 85
            assert swing.txt.text == '85'

            swing.binding.rebind()
            swing.sb.value = 42
            assert swing.txt.text == '42'
            swing.binding.unbind()
            swing.sb.value = 13
            assert swing.txt.text == '42'
            swing.binding.bind()
            assert swing.txt.text == '42'
            swing.binding.update()
            assert swing.txt.text == '13'

            swing.sb.model = new DefaultBoundedRangeModel(30, 1, 20, 40)
            assert swing.txt.text == '30'

            // first make sure we've been fireing
            assert swing.sbReverse.value == 30
            swing.sbReverse.value = 21
            swing.bindingReverse.reverseUpdate()
            assert swing.sb.value == 21
        }
    }

    void testTextFieldTextBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            swing.actions {
                textField('Bind', id: 'txts')
                textField(id: 'txt', text: bind(source: txts, sourceProperty: 'text', id: 'binding'))
            }

            assert swing.txt.text == 'Bind'
            swing.txts.text = 'Text'
            assert swing.txt.text == 'Text'
            swing.txts.text = 'Easily'
            assert swing.txt.text == 'Easily'

            swing.binding.rebind()
            swing.txts.text = 'With'
            assert swing.txt.text == 'With'
            swing.binding.unbind()
            swing.txts.text = 'bind()'
            assert swing.txt.text == 'With'
            swing.binding.bind()
            assert swing.txt.text == 'With'
            swing.binding.update()
            assert swing.txt.text == 'bind()'

            swing.txt.text = 'reversal'
            swing.binding.reverseUpdate()
            assert swing.txts.text == 'reversal'

            PlainDocument doc = new PlainDocument()
            doc.insertString(0, '{}', null)
            swing.txts.document = doc
            assert swing.txt.text == '{}'
        }
    }

    void testCheckboxSelectedBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            swing.actions {
                checkBox(id: 'cb')
                textField(id: 'txt', enabled: bind(source: cb, sourceProperty: 'selected', id: 'binding'))
            }

            assert swing.txt.enabled == swing.cb.selected
            swing.cb.selected = !swing.cb.selected
            assert swing.txt.enabled == swing.cb.selected
            swing.cb.selected = !swing.cb.selected
            assert swing.txt.enabled == swing.cb.selected

            swing.binding.rebind()
            swing.cb.selected = !swing.cb.selected
            assert swing.txt.enabled == swing.cb.selected
            swing.binding.unbind()
            swing.cb.selected = !swing.cb.selected
            assert swing.txt.enabled != swing.cb.selected
            swing.binding.bind()
            assert swing.txt.enabled != swing.cb.selected
            swing.binding.update()
            assert swing.txt.enabled == swing.cb.selected

            DefaultButtonModel md = new DefaultButtonModel()
            md.enabled = !swing.txt.enabled
            swing.cb.model = md
            assert swing.txt.enabled == swing.cb.selected

            swing.txt.enabled = !swing.txt.enabled
            swing.binding.reverseUpdate()
            assert swing.txt.enabled == swing.cb.selected
        }
    }

    void testComboBoxBindSyntheticProperties() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            def comboData = ['Alpha', 'Bravo', 'Charlie', 'Delta']
            def vectorData = ['Adams', 'Boston', 'Chicago', 'Denver']
            swing.frame {
                comboBox(id: 'combo01', items: comboData)
                comboBox(id: 'combo02', model: new javax.swing.DefaultComboBoxModel(new Vector(vectorData)))

                t1e = label(text: bind {combo01.elements})
                t1sx = label(text: bind {combo01.selectedIndex})
                t1se = label(text: bind {combo01.selectedElement})
                t1si = label(text: bind {combo01.selectedItem})

                t2e = label(text: bind {combo02.elements})
                t2sx = label(text: bind {combo02.selectedIndex})
                t2se = label(text: bind {combo02.selectedElement})
                t2si = label(text: bind {combo02.selectedItem})
            }

            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '0'
            assert swing.t1se.text == 'Alpha'
            assert swing.t1si.text == 'Alpha'

            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '0'
            assert swing.t2se.text == 'Adams'
            assert swing.t2si.text == 'Adams'

            swing.combo01.selectedIndex = 1
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '1'
            assert swing.t1se.text == 'Bravo'
            assert swing.t1si.text == 'Bravo'

            swing.combo02.selectedIndex = 1
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '1'
            assert swing.t2se.text == 'Boston'
            assert swing.t2si.text == 'Boston'

            swing.combo01.selectedIndex = -1
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '-1'
            assert swing.t1se.text == null
            assert swing.t1si.text == null

            swing.combo02.selectedIndex = -1
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '-1'
            assert swing.t2se.text == null
            assert swing.t2si.text == null

            swing.combo01.selectedItem = 'Charlie'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '2'
            assert swing.t1se.text == 'Charlie'
            assert swing.t1si.text == 'Charlie'

            swing.combo02.selectedItem = 'Chicago'
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '2'
            assert swing.t2se.text == 'Chicago'
            assert swing.t2si.text == 'Chicago'

            swing.combo01.selectedItem = 'Fox Trot'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '2'
            assert swing.t1se.text == 'Charlie'
            assert swing.t1si.text == 'Charlie'

            swing.combo02.selectedItem = 'Frank'
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '2'
            assert swing.t2se.text == 'Chicago'
            assert swing.t2si.text == 'Chicago'

            swing.combo01.selectedElement = 'Delta'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '3'
            assert swing.t1se.text == 'Delta'
            assert swing.t1si.text == 'Delta'

            swing.combo02.selectedElement = 'Denver'
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '3'
            assert swing.t2se.text == 'Denver'
            assert swing.t2si.text == 'Denver'

            swing.combo01.selectedElement = 'Golf'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '3'
            assert swing.t1se.text == 'Delta'
            assert swing.t1si.text == 'Delta'

            swing.combo02.selectedElement = 'George'
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver]'
            assert swing.t2sx.text == '3'
            assert swing.t2se.text == 'Denver'
            assert swing.t2si.text == 'Denver'

            swing.combo01.model.addElement('Echo')
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta, Echo]'
            swing.combo02.model.addElement('Easy')
            assert swing.t2e.text == '[Adams, Boston, Chicago, Denver, Easy]'
            swing.combo01.model.removeElement('Bravo')
            assert swing.t1e.text == '[Alpha, Charlie, Delta, Echo]'
            swing.combo02.model.removeElement('Adams')
            assert swing.t2e.text == '[Boston, Chicago, Denver, Easy]'
        }
    }

    void testListBindSyntheticProperties() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            def listModel = new DefaultListModel()
            ['Alpha', 'Bravo', 'Charlie', 'Delta'].each {listModel << it}
            def map = [sis: null, ses: null, svs: null] as ObservableMap

            swing.frame {
                list(id: 'list01', model: listModel, selectionMode: ListSelectionModel.SINGLE_SELECTION)

                t1e = label(text: bind {list01.elements})
                t1sx = label(text: bind {list01.selectedIndex})
                t1se = label(text: bind {list01.selectedElement})
                t1si = label(text: bind {list01.selectedValue})
                bean(map, sis: bind {list01.selectedIndices},
                        ses: bind {list01.selectedElements},
                        svs: bind {list01.selectedValues})
            }

            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            swing.list01.selectedIndex = -1
            assert swing.t1sx.text == '-1'
            assert swing.t1se.text == null
            assert swing.t1si.text == null
            assert map.sis == []
            assert map.ses == []
            assert map.svs == []

            swing.list01.selectedIndex = 0
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '0'
            assert swing.t1se.text == 'Alpha'
            assert swing.t1si.text == 'Alpha'
            assert map.sis == [0]
            assert map.ses == ['Alpha']
            assert map.svs == ['Alpha']

            swing.list01.selectedIndex = 1
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '1'
            assert swing.t1se.text == 'Bravo'
            assert swing.t1si.text == 'Bravo'
            assert map.sis == [1]
            assert map.ses == ['Bravo']
            assert map.svs == ['Bravo']

            swing.list01.selectedValue = 'Charlie'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '2'
            assert swing.t1se.text == 'Charlie'
            assert swing.t1si.text == 'Charlie'
            assert map.sis == [2]
            assert map.ses == ['Charlie']
            assert map.svs == ['Charlie']

            swing.list01.selectedValue = 'Fox Trot'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '2'
            assert swing.t1se.text == 'Charlie'
            assert swing.t1si.text == 'Charlie'
            assert map.sis == [2]
            assert map.ses == ['Charlie']
            assert map.svs == ['Charlie']

            swing.list01.selectedElement = 'Delta'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '3'
            assert swing.t1se.text == 'Delta'
            assert swing.t1si.text == 'Delta'
            assert map.sis == [3]
            assert map.ses == ['Delta']
            assert map.svs == ['Delta']

            swing.list01.selectedElement = 'Golf'
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta]'
            assert swing.t1sx.text == '3'
            assert swing.t1se.text == 'Delta'
            assert swing.t1si.text == 'Delta'
            assert map.sis == [3]
            assert map.ses == ['Delta']
            assert map.svs == ['Delta']

            swing.list01.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            swing.list01.selectedIndices = ([1i, 3i] as int[])
            assert swing.t1sx.text == '1'
            assert swing.t1se.text == 'Bravo'
            assert swing.t1si.text == 'Bravo'
            assert map.sis == [1, 3]
            assert map.ses == ['Bravo', 'Delta']
            assert map.svs == ['Bravo', 'Delta']

            swing.list01.model.addElement('Echo')
            assert swing.t1e.text == '[Alpha, Bravo, Charlie, Delta, Echo]'

            swing.list01.model.removeElement('Bravo')
            assert swing.t1e.text == '[Alpha, Charlie, Delta, Echo]'
        }
    }

    void testEventBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            def capture
            swing.actions {
                button('Button!', id: 'b')
                textField(id: 'txt', text: bind(source: b, sourceEvent: 'actionPerformed', sourceValue: {b.text}))
                textField(id: 'txt2', text: bind(source: b, sourceEvent: 'actionPerformed', sourceValue: {evt -> capture = evt; 'Captured!'}))
            }

            assert swing.txt.text == 'Button!'
            swing.b.text = 'Pressed!'
            // not pressed yet...
            assert swing.txt.text == 'Button!'
            swing.b.doClick()
            //ok, now it's pressed
            assert swing.txt.text == 'Pressed!'
            //check that we get evet as closure arg
            assert swing.txt2.text == 'Captured!'
            assert capture instanceof ActionEvent
        }
    }

    void testPropertyBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                checkBox('Button!', id: 'cb')
                textField(id: 'txt', enabled: bind(source: cb, sourceProperty: 'enabled', id: 'binding'))
            }
            assert swing.txt.enabled == swing.cb.enabled
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled

            swing.binding.rebind()
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled
            swing.binding.unbind()
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled != swing.cb.enabled
            swing.binding.bind()
            assert swing.txt.enabled != swing.cb.enabled
            swing.binding.update()
            assert swing.txt.enabled == swing.cb.enabled

            swing.txt.enabled = !swing.txt.enabled
            swing.binding.reverseUpdate()
            assert swing.txt.enabled == swing.cb.enabled

            DefaultButtonModel md = new DefaultButtonModel()
            md.enabled = !swing.txt.enabled
            swing.cb.model = md
            assert swing.txt.enabled == swing.cb.enabled
        }
    }

    void testBindGroup() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            int enabledChangeCount = 1
            swing.actions {
                bindGroup(id: 'testGroup')
                checkBox('Button!', id: 'cb')
                textField(id: 'txt1', text: bind(source: cb, sourceProperty: 'text', group: testGroup))
                textField(id: 'txt2', text: bind(source: cb, sourceProperty: 'text', sourceValue: {enabledChangeCount++}, group: testGroup))
                textField(id: 'txt3', text: bind(source: cb, sourceProperty: 'text', group: testGroup))
            }
            assert swing.txt1.text == 'Button!'
            assert swing.txt2.text == '1'
            assert swing.txt3.text == 'Button!'

            swing.testGroup.unbind()
            swing.cb.text = 'CheckBox!'
            swing.cb.selected = true

            assert swing.txt1.text == 'Button!'
            assert swing.txt2.text == '1'
            assert swing.txt3.text == 'Button!'

            swing.testGroup.update()
            assert swing.txt1.text == 'CheckBox!'
            assert swing.txt2.text == '2'
            assert swing.txt3.text == 'CheckBox!'

            swing.testGroup.bind()
            swing.cb.text = 'ComboBox!'
            swing.cb.selected = true
            assert swing.txt1.text == 'ComboBox!'
            assert swing.txt2.text == '3'
            assert swing.txt3.text == 'ComboBox!'

            // test auto-bind
            // test explicit true
            swing.actions {
                bindGroup(id: 'testGroup', bind: true)
                checkBox('Button!', id: 'cb')
                textField(id: 'txt1', text: bind(source: cb, sourceProperty: 'text', group: testGroup))
            }

            assert swing.txt1.text == 'Button!'
            swing.cb.text = 'CheckBox!'
            assert swing.txt1.text == 'CheckBox!'

            // test explicit false
            swing.actions {
                bindGroup(id: 'testGroup', bind: false)
                checkBox('Button!', id: 'cb')
                textField(id: 'txt1', text: bind(source: cb, sourceProperty: 'text', group: testGroup))
            }

            assert swing.txt1.text == 'Button!'
            swing.cb.text = 'CheckBox!'
            assert swing.txt1.text == 'Button!'

            // test implied = true
            swing.actions {
                bindGroup(id: 'testGroup')
                checkBox('Button!', id: 'cb')
                textField(id: 'txt1', text: bind(source: cb, sourceProperty: 'text', group: testGroup))
            }

            assert swing.txt1.text == 'Button!'
            swing.cb.text = 'CheckBox!'
            assert swing.txt1.text == 'CheckBox!'
        }
    }

    void testPropertyEventBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            int enabledChangeCount = 1
            swing.actions {
                checkBox('Button!', id: 'cb')
                textField(id: 'txtp', text: bind(source: cb, sourceProperty: 'text',))
                textField(id: 'txtpv', text: bind(source: cb, sourceProperty: 'text', sourceValue: {enabledChangeCount++}))
                textField(id: 'txtep', text: bind(source: cb, sourceEvent: 'stateChanged', sourceProperty: 'text'))
            }
            shouldFail {
                swing.actions {
                    // all three are pointless
                    textField(id: 'txtepv', enabled: bind(source: cb, sourceEvent: 'stateChanged', sourceProperty: 'text', sourceValue: {enabledChangeCount++}))
                }
            }
            shouldFail {
                swing.actions {
                    // just value isn't enough info
                    textField(id: 'txtv', enabled: bind(source: cb, sourceValue: {enabledChangeCount++}))
                }
            }
            shouldFail {
                swing.actions {
                    // just event isn't enough
                    textField(id: 'txtepv', enabled: bind(source: cb, sourceEvent: 'stateChanged'))
                }
            }

            assert swing.txtp.text == 'Button!'
            assert swing.txtpv.text == '1'
            assert swing.txtep.text == 'Button!'

            swing.cb.text = 'CheckBox!'
            assert swing.txtp.text == 'CheckBox!'
            assert swing.txtpv.text == '2'
            assert swing.txtep.text == 'Button!'

            swing.cb.selected = true
            assert swing.txtp.text == 'CheckBox!'
            assert swing.txtpv.text == '2'
            assert swing.txtep.text == 'CheckBox!'

        }
    }

    void testBindNodeValue() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            Map model = [
                string: 'string',
                bool  : true
            ] as ObservableMap

            shouldFail {
                swing.actions {
                    textField(id: 'error', text: bind(model.bool))
                }
            }

            shouldFail {
                swing.actions {
                    textField(id: 'error', text: bind(model.string))
                }
            }
        }
    }

    void testReversePropertyBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                textField(id: 'txt')
                checkBox('Button!', id: 'cb', enabled: bind(target: txt, targetProperty: 'enabled', id: 'binding'))
            }
            assert swing.txt.enabled == swing.cb.enabled
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled

            swing.binding.rebind()
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled == swing.cb.enabled
            swing.binding.unbind()
            swing.cb.enabled = !swing.cb.enabled
            assert swing.txt.enabled != swing.cb.enabled
            swing.binding.bind()
            assert swing.txt.enabled != swing.cb.enabled
            swing.binding.update()
            assert swing.txt.enabled == swing.cb.enabled

            swing.txt.enabled = !swing.txt.enabled
            swing.binding.reverseUpdate()
            assert swing.txt.enabled == swing.cb.enabled

            DefaultButtonModel md = new DefaultButtonModel()
            md.enabled = !swing.txt.enabled
            swing.cb.model = md
            assert swing.txt.enabled == swing.cb.enabled
        }
    }

    void testValueNodeBinding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                checkBox(id: 'cb1')
                checkBox('Button!', id: 'cb2', selected: bind(source: cb1, 'selected', id: 'binding1'))
                checkBox(id: 'cb3')
                checkBox('Button!', id: 'cb4', selected: bind(target: cb3, 'selected', id: 'binding2'))
            }
            assert !swing.cb1.selected
            assert !swing.cb2.selected
            swing.cb1.selected = true
            assert swing.cb1.selected
            assert swing.cb2.selected
            swing.cb2.selected = false
            assert swing.cb1.selected
            assert !swing.cb2.selected
            swing.cb1.selected = false
            swing.cb1.selected = true
            assert swing.cb1.selected
            assert swing.cb2.selected

            assert !swing.cb3.selected
            assert !swing.cb4.selected
            swing.cb3.selected = true
            assert swing.cb3.selected
            assert !swing.cb4.selected
            swing.cb4.selected = true
            assert swing.cb3.selected
            assert swing.cb4.selected
            swing.cb4.selected = false
            assert !swing.cb3.selected
            assert !swing.cb4.selected
        }
    }

    void testReversePropertyPropertites() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                textField(id: 'txt')
                checkBox('Button!', id: 'cb1', enabled: bind(target: txt, targetProperty: 'enabled', converter: {it}, id: 'binding1'))
                checkBox('Button!', id: 'cb2', enabled: bind(target: txt, targetProperty: 'enabled', id: 'binding2'))
            }
            assert swing.binding1.converter != null
            assert swing.binding2.converter == null
        }
    }

    void testConverters() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                checkBox(id: 'doner')
                checkBox(id: 'cb1', enabled: bind(source: doner, sourceProperty: 'enabled', converter: {it}, id: 'binding1'))
                checkBox(id: 'cb2', enabled: bind(source: doner, sourceProperty: 'enabled', id: 'binding2') {it})
                checkBox(id: 'cb3', enabled: bind(source: doner, sourceProperty: 'enabled', id: 'binding3'))
            }
            assert swing.binding1.converter != null
            assert swing.binding2.converter != null
            assert swing.binding3.converter == null
            // check double duty
            shouldFail(RuntimeException) {
                swing.actions {
                    checkBox(id: 'doner')
                    checkBox(id: 'cb1', enabled: bind(source: doner, sourceProperty: 'enabled', converter: {it}, id: 'binding1') {it})
                }
            }

            // check reversed bindings
            swing.actions {
                checkBox(id: 'doner')
                checkBox(id: 'cb1', enabled: bind(target: doner, targetProperty: 'enabled', converter: {it}, id: 'binding1'))
                checkBox(id: 'cb2', enabled: bind(target: doner, targetProperty: 'enabled', id: 'binding2') {it})
                checkBox(id: 'cb3', enabled: bind(target: doner, targetProperty: 'enabled', id: 'binding3'))
            }
            assert swing.binding1.converter != null
            assert swing.binding2.converter != null
            assert swing.binding3.converter == null
            // check double duty
            shouldFail(RuntimeException) {
                swing.actions {
                    checkBox(id: 'doner')
                    checkBox(id: 'cb1', enabled: bind(target: doner, targetProperty: 'enabled', converter: {it}, id: 'binding1') {it})
                }
            }
        }
    }

    void testDateConverters() {
        testInEDT {
            BindableBean model = new BindableBean()
            model.date = new Date()
            def dateConverter = { dateString ->
                new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(dateString)
            }
            def dateValidator = { dateString ->
                try {
                    def parser = new SimpleDateFormat("dd.MM.yyyy", Locale.US)
                    parser.lenient = false
                    parser.parse(dateString)
                } catch (Exception e) {
                    return false
                }
                return true
            }

            SwingBuilder swing = new SwingBuilder()
            swing.actions {
                tf1 = textField('01.01.1970', id: 'birthdayText', columns: 20,
                        text: bind(
                                validator: dateValidator,
                                converter: dateConverter,
                                target: model, targetProperty: 'date'
                        ))
                tf2 = textField('01.01.1970', id: 'birthdayText', columns: 20)
                binding = bind(
                        source: tf2,
                        sourceProperty: 'text',
                        validator: dateValidator,
                        converter: dateConverter,
                        target: model,
                        targetProperty: 'date'
                )
            }
        }
    }

    void testPropertyValuePassthrough() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            swing.actions {
                spinner(id: 'spin1', value: 4)
                spinner(id: 'spin2', value: 8)
                slider(id: 's1', value: bind(target: spin1, targetProperty: 'value', id: 'binding1', value: 15))
                slider(id: 's2', value: bind(source: spin2, sourceProperty: 'value', id: 'binding2', value: 16))
            }
            // s1 is the source, so it's value should be reflected
            assert swing.s1.value == 15
            // s2 is target, not source, so it's value setting should have no effect
            assert swing.s2.value == 8

            swing.actions {
                slider(id: 's3', value: 4)
                slider(id: 's4', value: 8)
                textArea(id: 't1', text: bind(target: s3, targetProperty: 'value',
                        id: 'binding3', value: '15',
                        converter: {Integer.parseInt(String.valueOf(it))}))
                textArea(id: 't2', text: bind(source: s4, sourceProperty: 'value',
                        id: 'binding4', value: '16',
                        converter: {Integer.parseInt(String.valueOf(it))}))
            }
            // s1 is the source, so it's value should be reflected
            assert swing.s3.value == 15
            // s2 is target, not source, so it's value setting should have no effect
            assert swing.s4.value == 8
        }
    }

    void testModel() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            def bean = new DummyBean()

            swing.bindProxy(bean, id: 'dummyBean')

            // test initial binding
            swing.textField(id: 'textField', text: swing.dummyBean.name)
            assert swing.textField.text == bean.name

            // test no live update by default
            bean.name = 'Jochen'
            assert swing.textField.text != bean.name

            // test for no update on bean change
            bean = new DummyBean()
            bean.name = 'Alex'
            swing.dummyBean.setModel(bean)
            assert swing.textField.text != bean.name

            //test for auto-update
            swing.bindProxy(bean, id: 'boundDummyBean', bind: true)
            swing.textField(id: 'boundTextField', text: swing.boundDummyBean.name)
            assert swing.boundTextField.text == bean.name
            bean.name = 'Danno'
            assert swing.boundTextField.text == bean.name

            // old model binding could be listening...
            assert swing.textField.text != bean.name
        }
    }

    void testModelUpdate() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            def bean = new DummyBean()

            swing.bindProxy(bean, id: 'dummyBean', bind: true)

            // test initial binding
            bean.name = 'Jochen'
            swing.textField(id: 'textField', text: swing.dummyBean.name)
            assert swing.textField.text == bean.name

            // test for update on bean change
            bean = new DummyBean()
            bean.name = 'Alex'

            swing.dummyBean.setModel(bean)
            assert swing.textField.text == bean.name

            // now simply one more live update: test fails here because
            // setModel() has called rebind() instead of bind()!
            bean.name = 'Danno'
            assert swing.textField.text == bean.name

            // test unbound, nothing should update no matter what we do
            swing.dummyBean.unbind()
            bean.name = 'Guillaume'
            assert swing.textField.text != bean.name

            bean = new DummyBean()
            bean.name = 'Andres'

            swing.dummyBean.setModel(bean)
            assert swing.textField.text != bean.name

            bean.name = 'James'
            assert swing.textField.text != bean.name

            // but a manual update should work
            swing.dummyBean.update()
            assert swing.textField.text == bean.name
        }
    }

    def mutualPropertyShortWorkout = { source, sourceProperty, sourceMutators,
                                       target, targetProperty, targetMutators, binding ->
        sourceMutators[0]()
        targetMutators[0]()

        // test forward binding
        assert source[sourceProperty] == target[targetProperty]
        sourceMutators[1]()
        assert source[sourceProperty] == target[targetProperty]
        sourceMutators[0]()
        assert source[sourceProperty] == target[targetProperty]

        // test reverse binding
        targetMutators[1]()
        assert source[sourceProperty] == target[targetProperty]
        targetMutators[0]()
        assert source[sourceProperty] == target[targetProperty]
    }

    def mutualPropertyWorkout = { source, sourceProperty, sourceMutators,
                                  target, targetProperty, targetMutators, binding ->

        mutualPropertyShortWorkout(source, sourceProperty, sourceMutators,
                target, targetProperty, targetMutators, binding)

        // test rebound
        binding.rebind()
        targetMutators[1]()
        assert source[sourceProperty] == target[targetProperty]
        sourceMutators[0]()
        assert source[sourceProperty] == target[targetProperty]

        // test unbound not updating
        binding.unbind()
        sourceMutators[1]()
        assert source[sourceProperty] != target[targetProperty]
        targetMutators[1]()
        assert source[sourceProperty] == target[targetProperty]
        sourceMutators[0]()
        assert source[sourceProperty] != target[targetProperty]

        // test manual forward update
        sourceMutators[0]()
        assert source[sourceProperty] != target[targetProperty]
        binding.update()
        assert source[sourceProperty] == target[targetProperty]

        // test manual reverse update
        sourceMutators[1]()
        assert source[sourceProperty] != target[targetProperty]
        binding.reverseUpdate()
        assert source[sourceProperty] == target[targetProperty]
    }

    void testMutualPropertyBinding() {
        testInEDT {
            ['full', 'source', 'target'].each { mode -> // contextual bind mode
                ['prop', 'synth'].each { target -> // target binding
                    ['prop', 'synth'].each { source -> // source binding
                        println "Trying $mode binding on $source source and $target target"

                        SwingBuilder swing = new SwingBuilder()

                        def sProp, tProp
                        swing.actions {
                            switch (source) {
                                case 'prop':
                                    sProp = 'enabled'
                                    st = new BindableBean(text: 'Baz')
                                    break
                                case 'synth':
                                    sProp = 'selected'
                                    st = textField(text: 'Baz')
                                    break
                                default: fail()
                            }
                            switch (target) {
                                case 'prop':
                                    tProp = 'enabled'
                                    tt = new BindableBean(text: 'Baz')
                                    break
                                case 'synth':
                                    tProp = 'selected'
                                    tt = textField(text: 'Baz')
                                    break
                                default: fail()
                            }

                            switch (mode) {
                                case 'full':
                                    checkBox(id: 'cb1')
                                    checkBox(id: 'cb2')
                                    bind(source: cb1, sourceProperty: sProp,
                                            target: cb2, targetProperty: tProp,
                                            id: 'binding', mutual: 'true')

                                    bind('text', source: st, target: tt,
                                            id: 'textBinding', mutual: 'true')

                                    break
                                case 'source':
                                    checkBox(id: 'cb2')
                                    checkBox(id: 'cb1', "$sProp": bind(
                                            target: cb2, targetProperty: tProp,
                                            id: 'binding', mutual: 'true'))

                                    bean(st, text: bind(
                                            target: tt, 'text',
                                            id: 'textBinding', mutual: 'true'))
                                    break
                                case 'target':
                                    checkBox(id: 'cb1')
                                    checkBox(id: 'cb2', "$tProp": bind(
                                            source: cb1, sourceProperty: sProp,
                                            id: 'binding', mutual: 'true'))

                                    bean(tt, text: bind(
                                            source: st, 'text',
                                            id: 'textBinding', mutual: 'true'))
                                    break
                                default: fail()
                            }
                        }
                        mutualPropertyWorkout(swing.cb1, sProp, [{swing.cb1[sProp] = true}, {swing.cb1[sProp] = false}],
                                swing.cb2, tProp, [{swing.cb2[tProp] = true}, {swing.cb2[tProp] = false}],
                                swing.binding)

                        mutualPropertyWorkout(swing.st, 'text', [{swing.st.text = "Foo"}, {swing.st.text = "Bar"}],
                                swing.tt, 'text', [{swing.tt.text = "Foo"}, {swing.tt.text = "Bar"}],
                                swing.textBinding)
                    }
                    if (mode != 'source') {
                        println "Trying $mode binding on event source and $target target"
                        SwingBuilder swing = new SwingBuilder()
                        def tProp
                        swing.actions {
                            st = button(actionCommand: 'Baz')

                            switch (target) {
                                case 'prop':
                                    tProp = 'enabled'
                                    tt = new BindableBean(text: 'Baz')
                                    break
                                case 'synth':
                                    tProp = 'selected'
                                    tt = textField(text: 'Baz')
                                    break
                                default: fail()
                            }
                            switch (mode) {
                                case 'full':
                                    checkBox(id: 'cb1')
                                    checkBox(id: 'cb2')
                                    bind(source: cb1, sourceEvent: 'actionPerformed', sourceProperty: 'borderPaintedFlat',
                                            target: cb2, targetProperty: tProp,
                                            id: 'binding', mutual: 'true')

                                    bind('text', source: st, sourceEvent: 'actionPerformed', sourceProperty: 'actionCommand', target: tt,
                                            id: 'textBinding', mutual: 'true')

                                    break
                                case 'target':
                                    checkBox(id: 'cb1')
                                    checkBox(id: 'cb2', "$tProp": bind(source: cb1,
                                            sourceEvent: 'actionPerformed', sourceProperty: 'borderPaintedFlat',
                                            id: 'binding', mutual: 'true'))

                                    bean(tt, text: bind(
                                            source: st, 'actionCommand', sourceEvent: 'actionPerformed',
                                            id: 'textBinding', mutual: 'true'))
                                    break
                                default: fail()
                            }
                            mutualPropertyShortWorkout(swing.cb1, 'borderPaintedFlat',
                                    [
                                            {swing.cb1.borderPaintedFlat = true; swing.cb1.doClick()},
                                            {swing.cb1.borderPaintedFlat = false; swing.cb1.doClick()}
                                    ],
                                    swing.cb2, tProp, [{swing.cb2[tProp] = true}, {swing.cb2[tProp] = false}],
                                    swing.binding)

                            mutualPropertyShortWorkout(swing.st, 'actionCommand',
                                    [
                                            {swing.st.actionCommand = "Foo"; swing.st.doClick()},
                                            {swing.st.actionCommand = "Bar"; swing.st.doClick()}
                                    ],
                                    swing.tt, 'text', [{swing.tt.text = "Foo"}, {swing.tt.text = "Bar"}],
                                    swing.textBinding)
                        }
                    }
                }
            }
            println "finished all permutations successfully"
        }
    }

    void testConverter() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            def model = new BindableBean()

            def toNumber = { v ->
                if (v == null || v == "") return null
                try { return new Integer(v) }
                catch (x) { return null }
            }

            swing.actions {
                frame(title: "Binding test", size: [100, 60]) {
                    textField(id: "t1", text: bind(target: model,
                            targetProperty: "value", converter: {toNumber(it)}))
                    textField(id: "t2", text: bind(target: model,
                            'floatValue', value: '1234', converter: { Float.parseFloat(it) * 2 }))
                }
            }
            assert model.floatValue == 2468
            swing.t1.text = '1234'
            assert model.value == 1234
        }
    }

    void testValidator() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()
            def model = new BindableBean()

            def isInteger = { v ->
                try {
                    Float.parseFloat(v)
                    return true
                } catch (NumberFormatException ignore) {
                    return false
                }
            }

            swing.actions {
                frame(title: "Binding test", size: [100, 60]) {
                    textField(id: "t1", text: bind(target: model, value: '123',
                            targetProperty: "value", validator: isInteger, converter: Integer.&parseInt))
                }
            }
            assert model.value == 123
            swing.t1.text = '1234'
            assert model.value == 1234
            swing.t1.text = 'Bogus'
            assert model.value == 1234
            swing.t1.text = '12345'
            assert model.value == 12345
        }
    }

    void testBindableVetoable() {
        testInEDT {
            def bbean = new BindableBean()
            bbean.vetoableChange = { PropertyChangeEvent pce ->
                if (pce.newValue ==~ /.*[ ]+.*/) {
                    throw new PropertyVetoException("No spaces allowed", pce)
                }
            }
            def abean = null
            SwingBuilder.build {
                abean = bean(new BindableBean(), text: 'text', id: 'tf')
                bean(bbean, vetoField: bind { tf.text })
            }
            abean.text = "test1"
            assert abean.text == bbean.vetoField
            abean.text = "this should fail"
            assert abean.text != bbean.vetoField
        }
    }

    void testGroovy4627_source_binding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            BindableBean model = new BindableBean(text: '0')

            swing.actions {
                bindGroup(id: 'formElements')
                textField(id: 'txt1', text: bind(source: model, sourceProperty: 'text', group: formElements))
                textField(id: 'txt2', text: bind(source: model, sourceProperty: 'text', group: formElements))
            }

            assert model.text == '0'
            assert swing.txt1.text == '0'
            assert swing.txt2.text == '0'

            swing.formElements.unbind()
            model.text = '1'
            assert swing.txt1.text == '0'
            assert swing.txt2.text == '0'

            swing.formElements.rebind()
            swing.formElements.update()
            assert swing.txt1.text == '1'
            assert swing.txt2.text == '1'

            model.text = '2'
            assert swing.txt1.text == '1'
            assert swing.txt2.text == '1'
            swing.formElements.update()
            assert swing.txt1.text == '2'
            assert swing.txt2.text == '2'
        }
    }

    void testGroovy4627_target_binding() {
        testInEDT {
            SwingBuilder swing = new SwingBuilder()

            BindableBean model = new BindableBean()

            swing.actions {
                bindGroup(id: 'formElements')
                textField(id: 'txt1', text: bind(target: model, targetProperty: 'text', group: formElements, value: '0'))
            }

            assert model.text == '0'
            assert swing.txt1.text == '0'

            swing.formElements.unbind()
            swing.txt1.text = '1'
            assert model.text == '0'

            swing.formElements.rebind()
            swing.formElements.update()
            assert model.text == '1'

            swing.txt1.text = '2'
            assert model.text == '1'
            swing.formElements.update()
            assert model.text == '2'
        }
    }
}

@Bindable class BindableBean {
    boolean enabled
    Integer value
    float floatValue
    int pvalue
    String text
    Date date
    @Vetoable String vetoField
}

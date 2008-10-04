/*
 * $Id$
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
 *
 */

package groovy.swing

import java.awt.event.ActionEvent
import javax.swing.DefaultBoundedRangeModel
import javax.swing.DefaultButtonModel
import javax.swing.text.PlainDocument

public class SwingBuilderBindingsTest extends GroovySwingTestCase {

    public void testSliderValueBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            slider(id:'sl')
            textField(id:'txt', text:bind(source:sl, sourceProperty:'value', id:'binding'))
            slider(id:'slReverse', value:bind(source:sl, sourceProperty:'value', id:'bindingReverse'))
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

    public void testScrollBarValueBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            scrollBar(id:'sl')
            textField(id:'txt', text:bind(source:sl, sourceProperty:'value', id:'binding'))
            scrollBar(id:'slReverse', value:bind(source:sl, sourceProperty:'value', id:'bindingReverse'))
            // need to use a second scrollBar for reverse test, because string->int autobox, not so happy
        }

        swing.sl.value = 10
        assert swing.txt.text == '10'
        swing.sl.value = 85
        assert swing.txt.text == '85'

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

    public void testTextFieldTextBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            textField('Bind', id:'txts')
            textField(id:'txt', text:bind(source:txts, sourceProperty:'text', id:'binding'))
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

    public void testCheckboxSelectedBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            checkBox(id:'cb')
            textField(id:'txt', enabled:bind(source:cb, sourceProperty:'selected', id:'binding'))
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

    public void testComboBoxBindSyntheticProperties() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()
        def comboData = ['Alpha', 'Bravo', 'Charlie', 'Delta']

        def vectorData = ['Adams', 'Boston', 'Chicago', 'Denver']


        swing.frame() {
            comboBox(id: 'combo01', items:comboData)
            comboBox(id: 'combo02', model: new javax.swing.DefaultComboBoxModel(new Vector(vectorData)))

            t1e  = label(text:bind {combo01.elements})
            t1sx = label(text:bind {combo01.selectedIndex})
            t1se = label(text:bind {combo01.selectedElement})
            t1si = label(text:bind {combo01.selectedItem})

            t2e  = label(text:bind {combo02.elements})
            t2sx = label(text:bind {combo02.selectedIndex})
            t2se = label(text:bind {combo02.selectedElement})
            t2si = label(text:bind {combo02.selectedItem})
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

    public void testEventBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()
        def capture
        swing.actions() {
            button('Button!', id:'b')
            textField(id:'txt', text:bind(source:b, sourceEvent:'actionPerformed', sourceValue:{b.text}))
            textField(id:'txt2', text:bind(source:b, sourceEvent:'actionPerformed', sourceValue:{evt->capture=evt; 'Captured!'}))
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

    public void testPropertyBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            checkBox('Button!', id:'cb')
            textField(id:'txt', enabled:bind(source:cb, sourceProperty:'enabled', id:'binding'))
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

    public void testPropertyEventBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        int enabledChangeCount = 1
        swing.actions() {
            checkBox('Button!', id:'cb')
            textField(id:'txtp', text:bind(source:cb, sourceProperty:'text',))
            textField(id:'txtpv', text:bind(source:cb, sourceProperty:'text', sourceValue: {enabledChangeCount++}))
            textField(id:'txtep', text:bind(source:cb, sourceEvent:'stateChanged', sourceProperty:'text'))
        }
        shouldFail {
            swing.actions() {
                // all three are pointless
                textField(id:'txtepv', enabled:bind(source:cb, sourceEvent:'stateChanged', sourceProperty:'text', sourceValue: {enabledChangeCount++}))
            }
        }
        shouldFail {
            swing.actions() {
                // just value isn't enough info
                textField(id:'txtv', enabled:bind(source:cb, sourceValue: {enabledChangeCount++}))
            }
        }
        shouldFail {
            swing.actions() {
                // just event isn't enough
                textField(id:'txtepv', enabled:bind(source:cb, sourceEvent:'stateChanged'))
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

    public void testReversePropertyBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            textField(id:'txt')
            checkBox('Button!', id:'cb', enabled:bind(target:txt, targetProperty:'enabled', id:'binding'))
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

    public void testValueNodeBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            checkBox(id:'cb1')
            checkBox('Button!', id:'cb2', selected:bind(source:cb1, 'selected', id:'binding1'))
            checkBox(id:'cb3')
            checkBox('Button!', id:'cb4', selected:bind(target:cb3, 'selected', id:'binding2'))
        }
        assert !swing.cb1.selected
        assert !swing.cb2.selected
        swing.cb1.selected=true
        assert swing.cb1.selected
        assert swing.cb2.selected
        swing.cb2.selected=false
        assert swing.cb1.selected
        assert !swing.cb2.selected
        swing.cb1.selected=false
        swing.cb1.selected=true
        assert swing.cb1.selected
        assert swing.cb2.selected

        assert !swing.cb3.selected
        assert !swing.cb4.selected
        swing.cb3.selected=true
        assert swing.cb3.selected
        assert !swing.cb4.selected
        swing.cb4.selected=true
        assert swing.cb3.selected
        assert swing.cb4.selected
        swing.cb4.selected=false
        assert !swing.cb3.selected
        assert !swing.cb4.selected
      }
    }

    public void testReversePropertyPropertites() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            textField(id:'txt')
            checkBox('Button!', id:'cb1', enabled:bind(target:txt, targetProperty:'enabled', converter: {it}, id:'binding1'))
            checkBox('Button!', id:'cb2', enabled:bind(target:txt, targetProperty:'enabled', id:'binding2'))
        }
        assert swing.binding1.converter != null
        assert swing.binding2.converter == null
      }
    }

    public void testConverters() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            checkBox(id:'doner')
            checkBox(id:'cb1', enabled:bind(source:doner, sourceProperty:'enabled', converter: {it}, id:'binding1'))
            checkBox(id:'cb2', enabled:bind(source:doner, sourceProperty:'enabled', id:'binding2') {it})
            checkBox(id:'cb3', enabled:bind(source:doner, sourceProperty:'enabled', id:'binding3'))
        }
        assert swing.binding1.converter != null
        assert swing.binding2.converter != null
        assert swing.binding3.converter == null
        // check double duty
        shouldFail(RuntimeException) {
            swing.actions() {
                checkBox(id:'doner')
                checkBox(id:'cb1', enabled:bind(source:doner, sourceProperty:'enabled', converter: {it}, id:'binding1') {it})
            }
        }

        // check reversed bindings
        swing.actions() {
            checkBox(id:'doner')
            checkBox(id:'cb1', enabled:bind(target:doner, targetProperty:'enabled', converter: {it}, id:'binding1'))
            checkBox(id:'cb2', enabled:bind(target:doner, targetProperty:'enabled', id:'binding2') {it})
            checkBox(id:'cb3', enabled:bind(target:doner, targetProperty:'enabled', id:'binding3'))
        }
        assert swing.binding1.converter != null
        assert swing.binding2.converter != null
        assert swing.binding3.converter == null
        // check double duty
        shouldFail(RuntimeException) {
            swing.actions() {
                checkBox(id:'doner')
                checkBox(id:'cb1', enabled:bind(target:doner, targetProperty:'enabled', converter: {it}, id:'binding1') {it})
            }
        }

      }
    }

    public void testPropertyValuePassthrough() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            spinner(id:'spin1', value:4)
            spinner(id:'spin2', value:8)
            slider(id:'s1', value:bind(target:spin1, targetProperty:'value', id:'binding1', value:15))
            slider(id:'s2', value:bind(source:spin2, sourceProperty:'value', id:'binding2', value:16))
        }
        // s1 is the source, so it's value should be reflected
        assert swing.s1.value == 15
        // s2 is target, not source, so it's value setting should have no effect
        assert swing.s2.value == 8
      }
    }

    public void testModel() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        def bean = new org.codehaus.groovy.runtime.DummyBean()

        swing.bindProxy(bean, id:'dummyBean')

        // test initial binding
        swing.textField(id:'textField', text:swing.dummyBean.name)
        assert swing.textField.text == bean.name

        // test no live update by default
        bean.name = 'Jochen'
        assert swing.textField.text != bean.name

        // test for update on bean change
        bean = new org.codehaus.groovy.runtime.DummyBean()
        bean.name = 'Alex'
        swing.dummyBean.setModel(bean)
        assert swing.textField.text == bean.name


        //test for auto-update
        swing.bindProxy(bean, id:'boundDummyBean', bind:true)
        swing.textField(id:'boundTextField', text:swing.boundDummyBean.name)
        assert swing.boundTextField.text == bean.name
        bean.name = 'Danno'
        assert swing.boundTextField.text == bean.name

        // old model binding could be listening...
        assert swing.textField.text != bean.name
      }
    }
}

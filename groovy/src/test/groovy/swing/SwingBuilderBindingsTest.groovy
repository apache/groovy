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

import javax.swing.DefaultBoundedRangeModel
import javax.swing.DefaultButtonModel
import javax.swing.JFrame
import javax.swing.text.PlainDocument

public class SwingBuilderBindingsTest extends GroovyTestCase {

    private Boolean isHeadless

    private Boolean isHeadless() {
        if (isHeadless != null)
          return isHeadless;

        try {
            new JFrame("testing")
            isHeadless = false
        } catch (java.awt.HeadlessException he) {
            isHeadless = true
        }
        return isHeadless
    }

    public void testSliderValueBinding() {
        if (isHeadless()) return
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

    public void testTextFieldTextBinding() {
        if (isHeadless()) return
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

    public void testCheckboxSelectedBinding() {
        if (isHeadless()) return
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

    public void testEventBinding() {
        if (isHeadless()) return
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            button('Button!', id:'b')
            textField(id:'txt', text:bind(source:b, sourceEvent:'actionPerformed', sourceValue:{b.text}))
        }

        assert swing.txt.text == 'Button!'
        swing.b.text = 'Pressed!'
        // not pressed yet...
        assert swing.txt.text == 'Button!'
        swing.b.doClick()
        //ok, now it's pressed
        assert swing.txt.text == 'Pressed!'
    }

    public void testPropertyBinding() {
        if (isHeadless()) return
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


    public void testModel() {
        if (isHeadless()) return
        SwingBuilder swing = new SwingBuilder()

        def bean = new org.codehaus.groovy.runtime.DummyBean()

        swing.model(bean, id:'dummyBean')

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
        swing.model(bean, id:'boundDummyBean', bind:true)
        swing.textField(id:'boundTextField', text:swing.boundDummyBean.name)
        assert swing.boundTextField.text == bean.name
        bean.name = 'Danno'
        assert swing.boundTextField.text == bean.name

        // old model binding could be listening...
        assert swing.textField.text != bean.name
    }
}

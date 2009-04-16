/*
 * $Id: SwingBuilderBindingsTest.groovy 14196 2008-11-27 16:32:25Z shemnon $
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

package groovy.swing.vm5

import groovy.beans.Bindable
import groovy.swing.SwingBuilder

public class SwingBuilderBindingsTest extends GroovySwingTestCase {
    public void testMutualPropertyBinding() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            bean(new BindableBean(), id:'cb')
            textField(id:'txt', enabled:bind(source:cb, sourceProperty:'enabled', id:'binding', mutual:true))
        }

          // test gorward binding
        assert swing.txt.enabled == swing.cb.enabled
        swing.cb.enabled = !swing.cb.enabled
        assert swing.txt.enabled == swing.cb.enabled
        swing.cb.enabled = !swing.cb.enabled
        assert swing.txt.enabled == swing.cb.enabled

          // test reverse binding
        swing.txt.enabled = !swing.txt.enabled
        assert swing.txt.enabled == swing.cb.enabled
        swing.txt.enabled = !swing.txt.enabled
        assert swing.txt.enabled == swing.cb.enabled

        // test rebound
        swing.binding.rebind()
        swing.cb.enabled = !swing.cb.enabled
        assert swing.txt.enabled == swing.cb.enabled
        swing.txt.enabled = !swing.txt.enabled
        assert swing.txt.enabled == swing.cb.enabled

        // test unbound not updating
        swing.binding.unbind()
        swing.cb.enabled = !swing.cb.enabled
        assert swing.txt.enabled != swing.cb.enabled
        swing.txt.enabled = !swing.txt.enabled
        assert swing.txt.enabled == swing.cb.enabled
        swing.txt.enabled = !swing.txt.enabled
        assert swing.txt.enabled != swing.cb.enabled

        // test manual forward update
        swing.txt.enabled = !swing.cb.enabled
        assert swing.txt.enabled != swing.cb.enabled
        swing.binding.update()
        assert swing.txt.enabled == swing.cb.enabled

        // test manual reverse update
        swing.txt.enabled = !swing.cb.enabled
        assert swing.txt.enabled != swing.cb.enabled
        swing.binding.reverseUpdate()
        assert swing.txt.enabled == swing.cb.enabled
      }
    }

    public void testConverter() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()
        def model = new BindableBean()

        def toNumber = { v ->
          if( v == null || v == "" ) return null
          try { return new Integer(v) }
          catch( x ) { return null }
        }

        swing.actions {

          frame( title: "Binding test", size: [100,60]) {
            textField( id: "t1", text: bind(target:model,
            targetProperty: "value", converter: {toNumber(it)}) )
          textField( id: "t2", text: bind(target:model,
            'floatValue', value:'1234', converter: { Float.parseFloat(it) * 2 }))
          }
        }
        assert model.floatValue == 2468
        swing.t1.text = '1234'
        assert model.value == 1234
      }
    }

    public void testValidator() {
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
          frame( title: "Binding test", size: [100,60]) {
            textField( id: "t1", text: bind(target:model, value:'123',
              targetProperty: "value", validator: isInteger, converter: Integer.&parseInt) )
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
}


class BindableBean {
    @Bindable boolean enabled
    @Bindable Integer value
    @Bindable float floatValue
    @Bindable int pvalue
}

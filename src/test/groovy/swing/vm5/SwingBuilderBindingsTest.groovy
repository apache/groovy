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
}


class BindableBean {
    @Bindable boolean enabled
}

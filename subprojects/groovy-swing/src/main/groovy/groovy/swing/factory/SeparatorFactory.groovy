/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.swing.factory

import javax.swing.JMenu
import javax.swing.JPopupMenu.Separator as JPopupMenu_Separator
import javax.swing.JSeparator
import javax.swing.JToolBar
import javax.swing.JToolBar.Separator as JToolBar_Separator

// JetGroovy bug

public class SeparatorFactory extends AbstractFactory {
    
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        Object parent = builder.getCurrent();
        if (parent instanceof JMenu) {
            return new JPopupMenu_Separator();
        } else if (parent instanceof JToolBar) {
            return new JToolBar_Separator();
        } else {
            return new JSeparator();
        }
    }
}

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

import groovy.swing.impl.ClosureCellEditor
import java.awt.Component

/**
* @author Alexander Klein
*/
class CellEditorFactory extends AbstractFactory {

   public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       FactoryBuilderSupport.checkValueIsNull value, name
       return new ClosureCellEditor(null, attributes)
   }

   public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
       if (child instanceof Component) {
           parent.children += child
       }
   }

   public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
       node.editorValue = builder.context.editorValueClosure
       node.prepareEditor = builder.context.prepareEditorClosure
       parent.cellEditor = node
   }
}

class CellEditorGetValueFactory extends AbstractFactory {

   public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       return Collections.emptyMap()
   }

   public boolean isHandlesNodeChildren() {
       return true;
   }

   public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
       builder.parentContext.editorValueClosure = childContent
       return false
   }
}

class CellEditorPrepareFactory extends AbstractFactory {

   public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       return Collections.emptyMap()
   }

   public boolean isHandlesNodeChildren() {
       return true;
   }

   public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
       builder.parentContext.prepareEditorClosure = childContent
       return false
   }
}

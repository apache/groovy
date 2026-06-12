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
package groovy.swing.factory

import groovy.swing.impl.ClosureCellEditor

import java.awt.*

/**
 * Factory for creating closure-backed cell editors.
 */
class CellEditorFactory extends AbstractFactory {

   /**
    * Creates the node handled by this factory.
    *
    * @param builder the factory builder
    * @param name the node name
    * @param value the node value
    * @param attributes the node attributes
    * @return the created or reused node
    */
   Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       FactoryBuilderSupport.checkValueIsNull value, name
       return new ClosureCellEditor(null, attributes)
   }

   /**
    * Attaches a child node to its parent.
    *
    * @param builder the factory builder
    * @param parent the parent node
    * @param child the child node
    */
   void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
       if (child instanceof Component) {
           parent.children += child
       }
   }

   /**
    * Finalizes a node after its children have been processed.
    *
    * @param builder the factory builder
    * @param parent the parent node
    * @param node the current node
    */
   void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
       node.editorValue = builder.context.editorValueClosure
       node.prepareEditor = builder.context.prepareEditorClosure
       parent.cellEditor = node
   }
}

/**
 * Factory for capturing cell editor value closures.
 */
class CellEditorGetValueFactory extends AbstractFactory {

   /**
    * Creates the node handled by this factory.
    *
    * @param builder the factory builder
    * @param name the node name
    * @param value the node value
    * @param attributes the node attributes
    * @return the created or reused node
    */
   Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       return Collections.emptyMap()
   }

   /**
    * Indicates whether this factory processes child closures itself.
    *
    * @return true if child closures are handled here
    */
   boolean isHandlesNodeChildren() {
       return true
   }

   /**
    * Consumes the child closure for this node.
    *
    * @param builder the factory builder
    * @param node the current node
    * @param childContent the childContent
    * @return false once the child closure has been handled
    */
   boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
       builder.parentContext.editorValueClosure = childContent
       return false
   }
}

/**
 * Factory for capturing cell editor preparation closures.
 */
class CellEditorPrepareFactory extends AbstractFactory {

   /**
    * Creates the node handled by this factory.
    *
    * @param builder the factory builder
    * @param name the node name
    * @param value the node value
    * @param attributes the node attributes
    * @return the created or reused node
    */
   Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
       return Collections.emptyMap()
   }

   /**
    * Indicates whether this factory processes child closures itself.
    *
    * @return true if child closures are handled here
    */
   boolean isHandlesNodeChildren() {
       return true
   }

   /**
    * Consumes the child closure for this node.
    *
    * @param builder the factory builder
    * @param node the current node
    * @param childContent the childContent
    * @return false once the child closure has been handled
    */
   boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
       builder.parentContext.prepareEditorClosure = childContent
       return false
   }
}

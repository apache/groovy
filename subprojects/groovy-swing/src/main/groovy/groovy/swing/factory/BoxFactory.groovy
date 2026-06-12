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

import javax.swing.*
import java.awt.*

/**
 * Factory for creating {@link Box} containers.
 */
public class BoxFactory extends ComponentFactory {

    /**
     * Creates a factory for {@link Box} nodes.
     */
    public BoxFactory() {
        super(null);
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        if (FactoryBuilderSupport.checkValueIsType(value, name, Box)) {
            return value;
        }
        int axis = BoxLayout.X_AXIS; // default to X so it behaves like FlowLayout
        if (attributes.containsKey("axis")) {
            Object o = attributes.remove("axis");
            if (o instanceof Number) {
                axis = ((Number)o).intValue();
            }
        }
        return new Box(axis);
    }
}

/**
 * Factory for creating horizontal {@link Box} containers.
 */
public class HBoxFactory extends ComponentFactory {

    /**
     * Creates a factory for horizontal {@link Box} nodes.
     */
    public HBoxFactory() {
        super(null);
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createHorizontalBox();
    }
}

/**
 * Factory for creating horizontal glue components.
 */
public class HGlueFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createHorizontalGlue();
    }
}

/**
 * Factory for creating horizontal strut components.
 */
public class HStrutFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsType(value, name, Number);
        Object num;
        if (value != null) {
            num = value;
        } else {
            num = attributes.remove("width");
        }
        if (num instanceof Number) {
            return Box.createHorizontalStrut(((Number) num).intValue());
        } else {
            return Box.createHorizontalStrut(6);
        }
    }
}

/**
 * Factory for creating vertical {@link Box} containers.
 */
public class VBoxFactory extends ComponentFactory {

    /**
     * Creates a factory for vertical {@link Box} nodes.
     */
    public VBoxFactory() {
        super(null);
    }

    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createVerticalBox();
    }
}

/**
 * Factory for creating vertical glue components.
 */
public class VGlueFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createVerticalGlue();
    }
}

/**
 * Factory for creating vertical strut components.
 */
public class VStrutFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsType(value, name, Number);
        Object num;
        if (value != null) {
            num = value;
        } else {
            num = attributes.remove("height");
        }
        if (num instanceof Number) {
            return Box.createVerticalStrut(((Number) num).intValue());
        } else {
            return Box.createVerticalStrut(6);
        }
    }
}

/**
 * Factory for creating glue components that expand in both directions.
 */
public class GlueFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createGlue();
    }
}

/**
 * Factory for creating rigid area components.
 */
public class RigidAreaFactory extends AbstractFactory {
    /**
     * Creates the node handled by this factory.
     *
     * @param builder the factory builder
     * @param name the node name
     * @param value the node value
     * @param attributes the node attributes
     * @return the created or reused node
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        Dimension dim;
        Object o = attributes.remove("size");
        if (o instanceof Dimension) {
            dim = (Dimension) o;
        } else {
            int w, h;
            o = attributes.remove("width");
            w = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
            o = attributes.remove("height");
            h = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
            dim = new Dimension(w, h);
        }
        return Box.createRigidArea(dim);
    }
}

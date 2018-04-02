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

public class BoxFactory extends ComponentFactory {

    public BoxFactory() {
        super(null);
    }
    
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

public class HBoxFactory extends ComponentFactory {

    public HBoxFactory() {
        super(null);
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createHorizontalBox();
    }
}

public class HGlueFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createHorizontalGlue();
    }
}

public class HStrutFactory extends AbstractFactory {
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

public class VBoxFactory extends ComponentFactory {

    public VBoxFactory() {
        super(null);
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createVerticalBox();
    }
}

public class VGlueFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createVerticalGlue();
    }
}

public class VStrutFactory extends AbstractFactory {
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

public class GlueFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        FactoryBuilderSupport.checkValueIsNull(value, name);
        return Box.createGlue();
    }
}

public class RigidAreaFactory extends AbstractFactory {
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

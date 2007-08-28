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

package groovy.swing.factory;

import groovy.swing.SwingBuilder;
import java.awt.Dimension;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class BoxFactory implements Factory {
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (SwingBuilder.checkValueIsType(value, name, Box.class)) {
            return value;
        }
        int axis = BoxLayout.X_AXIS; // default to X so it behaves like FlowLayout
        if (properties.containsKey("axis")) {
            Object o = properties.remove("axis");
            if (o instanceof Number) {
                axis = ((Number)o).intValue();
            }
        }
        return new Box(axis);
    }
}

    public class HBoxFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            return Box.createHorizontalBox();
        }
    }
    
    public class HGlueFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            return Box.createHorizontalGlue();
        }
    }
    
    public class HStrutFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsType(value, name, Number.class);
            Object num;
            if (value != null) {
                num = value;
            } else {
                num = properties.remove("width");
            }
            if (num instanceof Number) {
                return Box.createHorizontalStrut(((Number) num).intValue());
            } else {
                return Box.createHorizontalStrut(6);
            }
        }
    }
    
    public class VBoxFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            return Box.createVerticalBox();
        }
    }
    
    public class VGlueFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            return Box.createVerticalGlue();
        }
    }
    
    public class VStrutFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsType(value, name, Number.class);
            Object num;
            if (value != null) {
                num = value;
            } else {
                num = properties.remove("height");
            }
            if (num instanceof Number) {
                return Box.createVerticalStrut(((Number) num).intValue());
            } else {
                return Box.createVerticalStrut(6);
            }
        }
    }
    
    public class GlueFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            return Box.createGlue();
        }
    }
    
    public class RigidAreaFactory implements Factory {
        public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
            SwingBuilder.checkValueIsNull(value, name);
            Dimension dim;
            Object o = properties.remove("size");
            if (o instanceof Dimension) {
                dim = (Dimension) o;
            } else {
                int w, h;
                o = properties.remove("width");
                w = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
                o = properties.remove("height");
                h = ((o instanceof Number)) ? ((Number) o).intValue() : 6;
                dim = new Dimension(w, h);
            }
            return Box.createRigidArea(dim);
        }
    }
    

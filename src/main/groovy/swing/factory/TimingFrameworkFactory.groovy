/*
 * Copyright 2007 the original author or authors.
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

import org.codehaus.groovy.binding.ClosureSourceBinding
import org.codehaus.groovy.binding.FullBinding
import org.codehaus.groovy.binding.TimingFrameworkTriggerBinding

/**
 * attributes --
 *  range (range or 2 item array of point, dimension, etc):
 *     an range objet.  defaults to [0.0f..1.0f] also, default argument
 *
 *  duration (int) : time in ms to operate
 *  interval (int) : for discrete ranges, time between items
 *  start (boolean) : Wheter or not to start animation immediately default true
 *  repeatCount (double) : how many times to repeat the animation
 *  repeatBehavior (Animatior.RepeatBehavior) : how to repeat
 *  resolution (int) : for indescrete ranges, time between updates
 *  startDelay (int) : ms to delay start
 *  startDirection (Animator.Direction) : direction to start
 *  startValue (range value) : value to start animations at.
 *  endBehavior (Animator.endBehavior) :
 *  acceleration (float) : fraction of time to accelerate
 *  deceleration (float) : fraction of time to accelerate
 *  easeIn (boolean) : same as acceleration: easeIn ? 0.25F : 0.0F
 *  easeOut (boolean) : same as deceleration: easeIn ? 0.25F : 0.0F
 *
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7797 $
 * @since Groovy 1.1
 */
public class TimingFrameworkFactory extends AbstractFactory {

    // because isleaf is true we can keep state across calls assuming the same object
    protected boolean doBind

    public boolean isLeaf() {
        return true
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value != null) {
            properties.put("range", value);
        }

        FullBinding fb  = new TimingFrameworkTriggerBinding().createBinding(
            new ClosureSourceBinding({ it }, [0]),  null);

        if (isDiscreteRange(properties.get('range'))) {
            if (properties.containsKey("resolution")) {
                throw new RuntimeException("Resolution cannot be specified on discrete ranges")
            }
            Number duration = (Number) properties.get("duration");
            Number interval = (Number) properties.get("interval");

            def animateRange = properties.range
            // attempt to do per-item animation if not specified
            int divisions = animateRange.size() - 1;
            if (duration == null) {
                if (interval != null) {
                    duration = new Long(interval.longValue() * divisions);
                    // in Groovy 2.0 use valueOf
                } else {
                    interval = (1000 / divisions) as int;
                    // in Groovy 2.0 use valueOf
                    duration = (interval.longValue() * divisions) as long;
                    // in Groovy 2.0 use valueOf
                    // duration won't often be completely 1 sec by default, but it will fire evenly this way
                }
            } else if (interval == null) {
                interval = (duration.intValue() / divisions) as Integer;
                // in Groovy 2.0 use valueOf
            }
            properties.put("duration", duration);
            properties.put("interval", interval);
        } else {
            if (properties.containsKey("interval")) {
                throw new RuntimeException("Inverval cannot be specified on indiscrete ranges")
            }
        }

        if (fb != null) {
            // unless start:false, bind the animation
            Object o = properties.remove("start");
            doBind = ((o == null)
                || ((o instanceof Boolean) && ((Boolean)o).booleanValue())
                || ((o instanceof String) && Boolean.valueOf((String)o).booleanValue()))
        }

        builder.addDisposalClosure(fb.&unbind)
        return fb
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
        if (doBind) {
            node.bind()
        }
    }

    private boolean isDiscreteRange(Object range) {
        // for now, int ranges and non-range lists
        return (range instanceof IntRange);
    }

}
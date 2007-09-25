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

import java.util.logging.Logger
import org.codehaus.groovy.binding.ClosureSourceBinding
import org.codehaus.groovy.binding.FullBinding
import org.codehaus.groovy.binding.SwingTimerTriggerBinding

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7797 $
 * @since Groovy 1.1
 */



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
 */

public class AnimateFactory extends AbstractFactory {

    private static final Logger LOG = Logger.getLogger(AnimateFactory.class.getName());


    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        LOG.warning("Using a lower-grade animation node for 1.4 level JVMs.  Use a 5.0 or higher VM for better a better animate()")

        if (value == null) {
            value = properties.remove("range");
        }

        FullBinding fb  = null;
        if (value instanceof List) {
            //if (((List)value).size() > 2) {
            //    return createInterpolateAnimation(builder, ((List)value).get(0), ((List)value).get(1), properties);
            //} else {
                fb = createListAnimation((List) value, properties);
            //}
        }
        if (fb != null) {
            // unless start:false, bind the animation
            Object o = properties.remove("start");
            if (o == null
                || ((o instanceof Boolean) && ((Boolean)o).booleanValue())
                || ((o instanceof String) && Boolean.valueOf((String)o).booleanValue()))
            {
                fb.bind();
            }
        }

        builder.addDisposalClosure {fb.unbind()}
        return fb;
    }

    private FullBinding createListAnimation(final List animateRange, Map properties) {
        Number duration = (Number) properties.get("duration");
        Number interval = (Number) properties.get("interval");

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
        properties.put("stepSize", (duration.intValue() / divisions) as Integer);
        // in Groovy 2.0 use valueOf

        properties.put("reportSteps", Boolean.TRUE);
        properties.put("reportFraction", Boolean.FALSE);
        properties.put("reportElapsed", Boolean.FALSE);


        return new SwingTimerTriggerBinding().createBinding(
            new ClosureSourceBinding({ num -> animateRange[num] }, [0]),
            null);
        // in Groovy 2.0 use valueOf
    }
}

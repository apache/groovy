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

import groovy.swing.SwingBuilder
import org.codehaus.groovy.binding.ClosureSourceBinding
import org.codehaus.groovy.binding.FullBinding
import org.codehaus.groovy.binding.SwingTimerTriggerBinding

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7797 $
 * @since Groovy 1.1
 */
public class AnimateFactory implements Factory {

    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (value == null) {
            value = properties.remove("range");
        }
        //if ((value == null) && properties.containsKey("begin") && properties.containsKey("end")) {
        //    value = new Object[] {properties.remove("begin"), properties.remove("")};
        //}
        //if ((value != null) && (value.getClass().isArray())) {
        //    Arrays.asList(((Object[])value));
        //}



        FullBinding fb  = null;
        if (value instanceof List) {
            //if (((List)value).size() > 2) {
            //    return createInterpolateAnimation(builder, ((List)value).get(0), ((List)value).get(1), properties);
            //} else {
                fb = createListAnimation(builder, (List) value, properties);
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
        return fb;
    }

    private FullBinding createListAnimation(SwingBuilder builder, final List animateRange, Map properties) {
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
            interval = new Integer(duration.intValue() / divisions);
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

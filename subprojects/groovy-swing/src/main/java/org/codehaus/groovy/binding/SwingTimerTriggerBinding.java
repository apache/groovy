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
package org.codehaus.groovy.binding;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @since Groovy 1.1
 */
@Deprecated
public class SwingTimerTriggerBinding implements TriggerBinding {
    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        return new SwingTimerFullBinding((ClosureSourceBinding) source, target);
    }
}

/**
 * @since Groovy 1.1
 */
@Deprecated
class SwingTimerFullBinding extends AbstractFullBinding implements ActionListener {
    Timer timer;
    long startTime;
    long duration;
    int stepSize;

    boolean reportSteps;
    boolean reportFraction;
    boolean reportElapsed;
    boolean repeat;
    boolean bound;

    SwingTimerFullBinding(ClosureSourceBinding source, TargetBinding target) {
        this(source, target, 50, 1000);
    }

    SwingTimerFullBinding(SourceBinding source, TargetBinding target, int interval, int duration) {
        setSourceBinding(source);
        setTargetBinding(target);
        timer = new Timer(interval, this);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        this.duration = duration;
    }

    void resetTimer() {
        timer.stop();
        startTime = System.currentTimeMillis();
        timer.start();
    }

    public void bind() {
        if (!bound) {
            resetTimer();
            bound = true;
        }
    }

    public void unbind() {
        if (bound) {
            timer.stop();
            bound = false;
        }
    }

    public void rebind() {
        if (bound) {
            resetTimer();
        }
    }

    public void actionPerformed(ActionEvent e) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        if (elapsed >= duration) {
            if (repeat) {
                startTime = currentTime;
            } else {
                timer.stop();
            }
            // no over-runs...
            elapsed = duration;
        }

        // calculate
        if (reportSteps) {
            ((ClosureSourceBinding)sourceBinding).setClosureArgument(
                    (int) (elapsed / stepSize));
        } else if (reportFraction) {
            ((ClosureSourceBinding)sourceBinding).setClosureArgument(
                    (float) elapsed / (float) duration);
            //in Groovy2.0 use valueOf
        } else if (reportElapsed) {
            ((ClosureSourceBinding)sourceBinding).setClosureArgument(
                    elapsed);
            //in Groovy2.0 use valueOf
        } 

        update();
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getInterval() {
        return timer.getDelay();
    }

    public void setInterval(int interval) {
        timer.setDelay(interval);
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    public boolean isCoalesce() {
        return timer.isCoalesce();
    }

    public void setCoalesce(boolean coalesce) {
        timer.setCoalesce(coalesce);
    }

    public boolean isReportSteps() {
        return reportSteps;
    }

    public void setReportSteps(boolean reportSteps) {
        this.reportSteps = reportSteps;
    }

    public boolean isReportFraction() {
        return reportFraction;
    }

    public void setReportFraction(boolean reportFraction) {
        this.reportFraction = reportFraction;
    }

    public boolean isReportElapsed() {
        return reportElapsed;
    }

    public void setReportElapsed(boolean reportElapsed) {
        this.reportElapsed = reportElapsed;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
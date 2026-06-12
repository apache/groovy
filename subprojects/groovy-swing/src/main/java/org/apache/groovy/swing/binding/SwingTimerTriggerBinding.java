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
package org.apache.groovy.swing.binding;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Creates bindings driven by a Swing timer.
 *
 * @since Groovy 1.1
 */
public class SwingTimerTriggerBinding implements TriggerBinding {
    /**
     * Creates a timer-backed full binding.
     *
     * @param source the source binding, expected to be a {@link ClosureSourceBinding}
     * @param target the target binding
     * @return the created timer-backed binding
     */
    @Override
    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        return new SwingTimerFullBinding((ClosureSourceBinding) source, target);
    }
}

/**
 * A full binding that updates on a fixed Swing timer cadence.
 *
 * @since Groovy 1.1
 */
class SwingTimerFullBinding extends AbstractFullBinding implements ActionListener {
    /**
     * The timer driving periodic updates.
     */
    Timer timer;
    /**
     * The time at which the current timer cycle started.
     */
    long startTime;
    /**
     * The total duration of one timer cycle in milliseconds.
     */
    long duration;
    /**
     * The number of milliseconds represented by one reported step.
     */
    int stepSize;

    /**
     * Whether the closure should receive step counts instead of raw elapsed values.
     */
    boolean reportSteps;
    /**
     * Whether the closure should receive a completion fraction between {@code 0} and {@code 1}.
     */
    boolean reportFraction;
    /**
     * Whether the closure should receive elapsed milliseconds.
     */
    boolean reportElapsed;
    /**
     * Whether the timer should restart after reaching its duration.
     */
    boolean repeat;
    /**
     * Indicates whether the timer binding is currently active.
     */
    boolean bound;

    /**
     * Creates a timer binding with default interval and duration settings.
     *
     * @param source the closure-based source binding
     * @param target the target binding
     */
    SwingTimerFullBinding(ClosureSourceBinding source, TargetBinding target) {
        this(source, target, 50, 1000);
    }

    /**
     * Creates a timer binding with explicit interval and duration settings.
     *
     * @param source the source binding
     * @param target the target binding
     * @param interval the timer delay in milliseconds
     * @param duration the total cycle duration in milliseconds
     */
    SwingTimerFullBinding(SourceBinding source, TargetBinding target, int interval, int duration) {
        setSourceBinding(source);
        setTargetBinding(target);
        timer = new Timer(interval, this);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        this.duration = duration;
    }

    /**
     * Restarts the timer and resets the cycle start time.
     */
    void resetTimer() {
        timer.stop();
        startTime = System.currentTimeMillis();
        timer.start();
    }

    /**
     * Starts the timer and begins producing timed updates.
     */
    @Override
    public void bind() {
        if (!bound) {
            resetTimer();
            bound = true;
        }
    }

    /**
     * Stops the timer and marks the binding as inactive.
     */
    @Override
    public void unbind() {
        if (bound) {
            timer.stop();
            bound = false;
        }
    }

    /**
     * Restarts the timer when the binding is already active.
     */
    @Override
    public void rebind() {
        if (bound) {
            resetTimer();
        }
    }

    /**
     * Calculates the current timer progress value and pushes it to the target binding.
     *
     * @param e the timer event
     */
    @Override
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

    /**
     * Returns the configured cycle duration.
     *
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the configured cycle duration.
     *
     * @param duration the duration in milliseconds
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Returns the timer delay between updates.
     *
     * @return the delay in milliseconds
     */
    public int getInterval() {
        return timer.getDelay();
    }

    /**
     * Sets the timer delay between updates.
     *
     * @param interval the delay in milliseconds
     */
    public void setInterval(int interval) {
        timer.setDelay(interval);
    }

    /**
     * Returns the number of milliseconds represented by one reported step.
     *
     * @return the step size in milliseconds
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * Sets the number of milliseconds represented by one reported step.
     *
     * @param stepSize the step size in milliseconds
     */
    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * Returns whether timer events should be coalesced.
     *
     * @return {@code true} when coalescing is enabled
     */
    public boolean isCoalesce() {
        return timer.isCoalesce();
    }

    /**
     * Enables or disables timer event coalescing.
     *
     * @param coalesce {@code true} to coalesce timer events
     */
    public void setCoalesce(boolean coalesce) {
        timer.setCoalesce(coalesce);
    }

    /**
     * Returns whether the source closure should receive step counts.
     *
     * @return {@code true} when step reporting is enabled
     */
    public boolean isReportSteps() {
        return reportSteps;
    }

    /**
     * Enables or disables step-count reporting.
     *
     * @param reportSteps {@code true} to report step counts
     */
    public void setReportSteps(boolean reportSteps) {
        this.reportSteps = reportSteps;
    }

    /**
     * Returns whether the source closure should receive a completion fraction.
     *
     * @return {@code true} when fraction reporting is enabled
     */
    public boolean isReportFraction() {
        return reportFraction;
    }

    /**
     * Enables or disables fraction reporting.
     *
     * @param reportFraction {@code true} to report a completion fraction
     */
    public void setReportFraction(boolean reportFraction) {
        this.reportFraction = reportFraction;
    }

    /**
     * Returns whether the source closure should receive elapsed milliseconds.
     *
     * @return {@code true} when elapsed-time reporting is enabled
     */
    public boolean isReportElapsed() {
        return reportElapsed;
    }

    /**
     * Enables or disables elapsed-time reporting.
     *
     * @param reportElapsed {@code true} to report elapsed milliseconds
     */
    public void setReportElapsed(boolean reportElapsed) {
        this.reportElapsed = reportElapsed;
    }

    /**
     * Returns whether the timer repeats after completing a cycle.
     *
     * @return {@code true} when repeat mode is enabled
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * Enables or disables repeat mode.
     *
     * @param repeat {@code true} to restart the timer after each cycle
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}

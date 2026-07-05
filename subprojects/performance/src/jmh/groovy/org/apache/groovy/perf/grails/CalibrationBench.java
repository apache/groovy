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
package org.apache.groovy.perf.grails;

import org.apache.groovy.calibration.AbstractCalibrationBench;

/**
 * Host-speed calibration ruler for the {@code grails-ad} CI suite; the class
 * name/package exist solely to match that suite's {@code benchInclude}
 * pattern ({@code \.perf\.grails\.[A-D]}). See {@link AbstractCalibrationBench}
 * for what is measured and why.
 */
public class CalibrationBench extends AbstractCalibrationBench {
}

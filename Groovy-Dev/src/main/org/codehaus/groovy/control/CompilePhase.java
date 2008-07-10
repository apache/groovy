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

package org.codehaus.groovy.control;

public enum CompilePhase {

    INITIALIZATION(Phases.INITIALIZATION),
    PARSING(Phases.PARSING),
    CONVERSION(Phases.CONVERSION),
    SEMANTIC_ANALYSIS(Phases.SEMANTIC_ANALYSIS),
    CANONICALIZATION(Phases.CANONICALIZATION),
    INSTRUCTION_SELECTION(Phases.INSTRUCTION_SELECTION),
    CLASS_GENERATION(Phases.CLASS_GENERATION),
    OUTPUT(Phases.OUTPUT),
    FINALIZATION(Phases.FINALIZATION),
    ;

    public static CompilePhase[] phases = {
        null,
        INITIALIZATION,
        PARSING,
        CONVERSION,
        SEMANTIC_ANALYSIS,
        CANONICALIZATION,
        INSTRUCTION_SELECTION,
        CLASS_GENERATION,
        OUTPUT,
        FINALIZATION,
    };

    int phaseNumber;

    CompilePhase(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }
}

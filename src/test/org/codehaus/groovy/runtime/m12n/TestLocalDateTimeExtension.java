package org.codehaus.groovy.runtime.m12n;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class TestLocalDateTimeExtension {

    public static int compareTo(LocalDateTime self, LocalDate other) {
        return self.compareTo(other.atStartOfDay());
    }
}

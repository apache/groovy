package org.codehaus.groovy.sandbox.ui;

import java.util.List;

public interface Completer {
  List findCompletions(String token);
}

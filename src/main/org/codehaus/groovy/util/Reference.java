package org.codehaus.groovy.util;

public interface Reference<T,V extends Finalizable> {
    T get();
    void clear();
    V getHandler();
}

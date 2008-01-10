package org.codehaus.groovy.runtime;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class CallSiteArray extends AtomicReferenceArray{
        public CallSiteArray(int size) {
            super(size);
        }

        public final CallSite getCallSite (int index) {
            CallSite binopCallSite = (CallSite) get(index);
            if (binopCallSite == null) {
               compareAndSet(index, null, new CallSite());
               return (CallSite) get(index);
            }
            return binopCallSite;
        }
    }

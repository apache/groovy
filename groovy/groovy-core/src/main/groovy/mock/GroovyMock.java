package groovy.mock;

import groovy.lang.GroovyObject;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import com.mockobjects.Verifiable;
import com.mockobjects.dynamic.*;

/**
 * 
 * @author Joe Walnes
 * @author Chris Stevenson
 * @version $Revision$
 */
public class GroovyMock extends GroovyObjectSupport implements Verifiable {

    private CallBag calls = new CallBag();
    private CallFactory callFactory = new DefaultCallFactory();
    private Mock mock = new Mock(I.class);

    interface I {
    }

    private GroovyObject instance = new GroovyObjectSupport() {
        public Object invokeMethod(String name, Object args) {
            return callMethod(name, args);
        }
    };

    public Object invokeMethod(String name, Object args) {
        if (name.equals("verify")) {
            verify();
        }
        else {
            expectMethod(name, args);
        }
        return null;
    }

    public GroovyObject getInstance() {
        return instance;
    }

    public static GroovyMock newInstance() {
        return new GroovyMock();
    }

    private void expectMethod(String name, Object args) {
        ConstraintMatcher constraintMatcher = createMatcher(args);
        calls.addExpect(
            callFactory.createCallExpectation(
                callFactory.createCallSignature(name, constraintMatcher, callFactory.createVoidStub())));
    }

    private ConstraintMatcher createMatcher(Object args) {
        if (args instanceof Closure) {
            Closure closure = (Closure) args;
            return C.args(new ClosureConstraintMatcher(closure));
        }
        else {
            return C.args(C.eq(args));
        }
    }

    private Object callMethod(String name, Object args) {
        try {
            return calls.call(mock, name, new Object[] { args });
        }
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void verify() {
        calls.verify();
    }

}

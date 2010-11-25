package groovy.lang;


/**
 * A TrampolineClosure wraps a closure that needs to be executed on a functional trampoline.
 * Upon calling, a TrampolineClosure will call the original closure waiting for its result.
 * If the outcome of the call is another instance of a TrampolineClosure, created perhaps as a result to a call to the TrampolineClosure.trampoline()
 * method, the TrampolineClosure will again be invoked. This repetitive invocation of returned TrampolineClosure instances will continue
 * until a value other than TrampolineClosure is returned.
 * That value will become the final result of the trampoline.
 *
 * @author Vaclav Pech
 */
final class TrampolineClosure extends Closure<Object> {

    private final Closure original;

    TrampolineClosure(final Closure original) {
        super(original.getOwner(), original.getDelegate());
        this.original = original;
    }

    /**
     * Delegates to the wrapped closure
     */
    @Override
    public int getMaximumNumberOfParameters() {
        return original.maximumNumberOfParameters;
    }

    /**
     * Delegates to the wrapped closure
     */
    @Override
    public Class[] getParameterTypes() {
        return original.parameterTypes;
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public Object call() {
        return loop(original.call());
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public Object call(final Object arguments) {
        return loop(original.call(arguments));
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public Object call(final Object... args) {
        return loop(original.call(args));
    }

    private Object loop(final Object lastResult) {
        Object result = lastResult;

        for (;;) {
            if (result instanceof TrampolineClosure) {
                result = ((TrampolineClosure)result).original.call();
            } else return result;
        }
    }

    /**
     * Builds a trampolined variant of the current closure.
     * @param args Parameters to curry to the underlying closure.
     * @return An instance of TrampolineClosure wrapping the original closure after currying.
     */
    @Override
   public Closure trampoline(final Object... args) {
        return new TrampolineClosure(original.curry(args));
    }

    /**
     * Returns itself, since it is a good enough trampolined variant of the current closure.
     * @return An instance of TrampolineClosure wrapping the original closure.
     */
    @Override
    public Closure trampoline() {
        return this;
    }
}

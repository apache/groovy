package groovy.txn;

import groovy.lang.Closure;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class TransactionBean {
    private Closure run;
    private Closure onError;
    private Closure onSuccess;

    public Closure run() {
        return run;
    }

    public Closure onError() {
        return onError;
    }

    public Closure onSuccess() {
        return onSuccess;
    }

    public void run(Closure run) {
        this.run = run;
    }

    public void onError(Closure onError) {
        this.onError = onError;
    }

    public void onSuccess(Closure onSuccess) {
        this.onSuccess = onSuccess;
    }
}

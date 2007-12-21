package groovy.txn;

import groovy.lang.Closure;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class TransactionBuilder {
    public void transaction(Closure closure) {
        TransactionBean bean = new TransactionBean();
        closure.setDelegate(bean);
        closure.call(this);

        // lets call the closures now
        System.out.println("Performing normal transaction");
        bean.run().call(this);
        bean.onSuccess().call(this);

        System.out.println("Performing error transaction");
        bean.run().call(this);
        bean.onError().call(this);
    }
}

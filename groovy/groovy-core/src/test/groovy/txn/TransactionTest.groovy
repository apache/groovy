package groovy.txn

class TransactionTest extends GroovyTestCase {

    void testTxn() {
		builder = new TransactionBuilder()
		builder.transaction {
		    run {
		        System.out.println("run code!")
		    }
		    onError {
                System.out.println("on error!")
		    }
		    onSuccess {
                System.out.println("on success!")
		    }
		}
    }

}

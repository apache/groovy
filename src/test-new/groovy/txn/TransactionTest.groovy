package groovy.txn

class TransactionTest extends GroovyTestCase {

    void testTxn() {
		builder = new TransactionBuilder()
		builder.transaction {
		    run {
		        println("run code!")
		    }
		    onError {
                println("on error!")
		    }
		    onSuccess {
                println("on success!")
		    }
		}
    }

}

package support

// tag::instance_extension[]
class MaxRetriesExtension {                                     // <1>
    static void maxRetries(Integer self, Closure code) {        // <2>
        int retries = 0
        Throwable e
        while (retries<self) {
            try {
                code.call()
                break
            } catch (Throwable err) {
                e = err
                retries++
            }
        }
        if (retries==0 && e) {
            throw e
        }
    }
}
// end::instance_extension[]
class A {
    private void tryCatchMethod() {
        try {
            1
        }
        catch(any) {
            2
        }
    }

    private void tryCatchFinallyMethod() {
        try {
            1
        }
        catch(any) {
            2
        }
        finally {
            3
        }
    }

    private void trySeveralCatchMethod() {
        try {
            1
        }
        catch(NumberFormatException e) {
            2
        }
        catch(NullPointerException e) {
            3
        }
    }

    private void tryMultyCatchMethod() {
        try {
            1
        }
        catch(NumberFormatException | NullPointerException e) {
            2
        }
    }

    private void trySeveralCatchFinallyMethod() {
        try {
            1
        }
        catch(NumberFormatException e) {
            2
        }
        catch(NullPointerException e) {
            3
        }
        finally {
            4
        }
    }

    private void tryMultyCatchFinallyMethod() {
        try {
            1
        }
        catch(NumberFormatException | NullPointerException e) {
            2
        }
        finally {
            3
        }
    }

    private void testThrow() {
        throw e
    }
}

// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.stm;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.TxnFactoryBuilder;
import org.multiverse.api.exceptions.ControlFlowError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;

/**
 * Provides access to GPars Stm services.
 *
 * @author Vaclav Pech
 */
public abstract class GParsStm {
    static final String THE_CODE_FOR_AN_ATOMIC_BLOCK_MUST_NOT_BE_NULL = "The code for an atomic block must not be null.";
    static final String AN_EXCEPTION_WAS_EXPECTED_TO_BE_THROWN_FROM_UNWRAP_STM_CONTROL_ERROR_FOR = "An exception was expected to be thrown from unwrapStmControlError for ";
    private static final String CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED = "Cannot create an atomic block. Some of the specified parameters are not supported. ";

    /**
     * Gives access to multiverse TxnFactoryBuilder to allow customized creation of atomic blocks
     */
    public static final TxnFactoryBuilder transactionFactory = getGlobalStmInstance().newTxnFactoryBuilder();

    /**
     * The atomic block to use when no block is specified explicitly
     */
    private static TxnExecutor defaultTxnExecutor = GlobalStmInstance.getGlobalStmInstance().newTxnFactoryBuilder().setFamilyName("GPars.Stm").newTxnExecutor();

    /**
     * A factory method to create custom atomic blocks.
     *
     * @return The newly created instance of TxnExecutor
     */
    public static TxnExecutor createTxnExecutor() {
        return createTxnExecutor(Collections.<String, Object>emptyMap());
    }

    /**
     * A factory method to create custom atomic blocks allowing the caller to set desired transactional characteristics.
     *
     * @param params A map holding all values that should be specified. See the Multiverse documentation for possible values
     * @return The newly created instance of TxnExecutor
     */
    public static TxnExecutor createTxnExecutor(final Map<String, Object> params) {
        TxnFactoryBuilder localFactory = transactionFactory;

        final Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (final Map.Entry<String, Object> entry : entries) {
            if (entry.getValue() == null)
                throw new IllegalArgumentException("Cannot create an atomic block. The value for " + entry.getKey() + " is null.");
            if (entry.getKey() == null || "".equals(entry.getKey().trim()))
                throw new IllegalArgumentException("Cannot create an atomic block. Found an empty key.");
            final String key = "set" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);

            try {
                final Method method;
                if (entry.getValue().getClass().equals(Long.class)) {
                    method = TxnFactoryBuilder.class.getDeclaredMethod(key, Long.TYPE);
                } else if (entry.getValue().getClass().equals(Integer.class)) {
                    method = TxnFactoryBuilder.class.getDeclaredMethod(key, Integer.TYPE);
                } else if (entry.getValue().getClass().equals(Boolean.class)) {
                    method = TxnFactoryBuilder.class.getDeclaredMethod(key, Boolean.TYPE);
                } else {
                    method = TxnFactoryBuilder.class.getDeclaredMethod(key, entry.getValue().getClass());
                }
                localFactory = (TxnFactoryBuilder) method.invoke(localFactory, entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            }

        }
        return localFactory.newTxnExecutor();
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     * @param <T>  The type or the return value
     * @return The result returned from the supplied code when run in a transaction
     */
    public static <T> T atomic(final Closure code) {
        return defaultTxnExecutor.execute(new GParsTxnExecutor<T>(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     * @param <T>  The type or the return value
     * @return The result returned from the supplied code when run in a transaction
     */
    public static <T> T atomic(final TxnExecutor block, final Closure code) {
        return block.execute(new GParsTxnExecutor<T>(code));
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static int atomicWithInt(final Closure code) {
        return defaultTxnExecutor.execute(new GParsAtomicIntBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static int atomicWithInt(final TxnExecutor block, final Closure code) {
        return block.execute(new GParsAtomicIntBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static long atomicWithLong(final Closure code) {
        return defaultTxnExecutor.execute(new GParsAtomicLongBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static long atomicWithLong(final TxnExecutor block, final Closure code) {
        return block.execute(new GParsAtomicLongBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static boolean atomicWithBoolean(final Closure code) {
        return defaultTxnExecutor.execute(new GParsAtomicBooleanBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static boolean atomicWithBoolean(final TxnExecutor block, final Closure code) {
        return block.execute(new GParsAtomicBooleanBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static double atomicWithDouble(final Closure code) {
        return defaultTxnExecutor.execute(new GParsAtomicDoubleBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     * @return The result returned from the supplied code when run in a transaction
     */
    public static double atomicWithDouble(final TxnExecutor block, final Closure code) {
        return block.execute(new GParsAtomicDoubleBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction.
     *
     * @param code The code to run inside a transaction
     */
    public static void atomicWithVoid(final Closure code) {
        defaultTxnExecutor.execute(new GParsAtomicVoidBlock(code));
    }

    /**
     * Performs the supplied code atomically within a transaction using the supplied atomic block.
     *
     * @param code The code to run inside a transaction
     */
    public static void atomicWithVoid(final TxnExecutor block, final Closure code) {
        block.execute(new GParsAtomicVoidBlock(code));
    }

    /**
     * Unwraps the multiverse control exceptions from Groovy exceptions
     *
     * @param e The exception to unwrap from
     */
    static void unwrapStmControlError(final InvokerInvocationException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ControlFlowError) throw (Error) cause;
        else throw e;
    }
}

/*
 * Copyright 2010 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.groovy.util.concurrent.concurrentlinkedhashmap;

import org.apache.groovy.util.ObjectHolder;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.groovy.util.concurrent.concurrentlinkedhashmap.ConcurrentLinkedHashMap.DrainStatus.IDLE;
import static org.apache.groovy.util.concurrent.concurrentlinkedhashmap.ConcurrentLinkedHashMap.DrainStatus.PROCESSING;
import static org.apache.groovy.util.concurrent.concurrentlinkedhashmap.ConcurrentLinkedHashMap.DrainStatus.REQUIRED;

/**
 * A hash table supporting full concurrency of retrievals, adjustable expected
 * concurrency for updates, and a maximum capacity to bound the map by. This
 * implementation differs from {@link ConcurrentHashMap} in that it maintains a
 * page replacement algorithm that is used to evict an entry when the map has
 * exceeded its capacity. Unlike the <tt>Java Collections Framework</tt>, this
 * map does not have a publicly visible constructor and instances are created
 * through a {@link Builder}.
 * <p>
 * An entry is evicted from the map when the <tt>weighted capacity</tt> exceeds
 * its <tt>maximum weighted capacity</tt> threshold. A {@link EntryWeigher}
 * determines how many units of capacity that an entry consumes. The default
 * weigher assigns each value a weight of <tt>1</tt> to bound the map by the
 * total number of key-value pairs. A map that holds collections may choose to
 * weigh values by the number of elements in the collection and bound the map
 * by the total number of elements that it contains. A change to a value that
 * modifies its weight requires that an update operation is performed on the
 * map.
 * <p>
 * An {@link EvictionListener} may be supplied for notification when an entry
 * is evicted from the map. This listener is invoked on a caller's thread and
 * will not block other threads from operating on the map. An implementation
 * should be aware that the caller's thread will not expect long execution
 * times or failures as a side effect of the listener being notified. Execution
 * safety and a fast turn around time can be achieved by performing the
 * operation asynchronously, such as by submitting a task to an
 * {@link java.util.concurrent.ExecutorService}.
 * <p>
 * The <tt>concurrency level</tt> determines the number of threads that can
 * concurrently modify the table. Using a significantly higher or lower value
 * than needed can waste space or lead to thread contention, but an estimate
 * within an order of magnitude of the ideal value does not usually have a
 * noticeable impact. Because placement in hash tables is essentially random,
 * the actual concurrency will vary.
 * <p>
 * This class and its views and iterators implement all of the
 * <em>optional</em> methods of the {@link Map} and {@link Iterator}
 * interfaces.
 * <p>
 * Like {@link java.util.Hashtable} but unlike {@link HashMap}, this class
 * does <em>not</em> allow <tt>null</tt> to be used as a key or value. Unlike
 * {@link LinkedHashMap}, this class does <em>not</em> provide
 * predictable iteration order. A snapshot of the keys and entries may be
 * obtained in ascending and descending order of retention.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *      http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
@ThreadSafe
public final class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V>
    implements ConcurrentMap<K, V>, Serializable {

  /*
   * This class performs a best-effort bounding of a ConcurrentHashMap using a
   * page-replacement algorithm to determine which entries to evict when the
   * capacity is exceeded.
   *
   * The page replacement algorithm's data structures are kept eventually
   * consistent with the map. An update to the map and recording of reads may
   * not be immediately reflected on the algorithm's data structures. These
   * structures are guarded by a lock and operations are applied in batches to
   * avoid lock contention. The penalty of applying the batches is spread across
   * threads so that the amortized cost is slightly higher than performing just
   * the ConcurrentHashMap operation.
   *
   * A memento of the reads and writes that were performed on the map are
   * recorded in buffers. These buffers are drained at the first opportunity
   * after a write or when the read buffer exceeds a threshold size. The reads
   * are recorded in a lossy buffer, allowing the reordering operations to be
   * discarded if the draining process cannot keep up. Due to the concurrent
   * nature of the read and write operations a strict policy ordering is not
   * possible, but is observably strict when single threaded.
   *
   * Due to a lack of a strict ordering guarantee, a task can be executed
   * out-of-order, such as a removal followed by its addition. The state of the
   * entry is encoded within the value's weight.
   *
   * Alive: The entry is in both the hash-table and the page replacement policy.
   * This is represented by a positive weight.
   *
   * Retired: The entry is not in the hash-table and is pending removal from the
   * page replacement policy. This is represented by a negative weight.
   *
   * Dead: The entry is not in the hash-table and is not in the page replacement
   * policy. This is represented by a weight of zero.
   *
   * The Least Recently Used page replacement algorithm was chosen due to its
   * simplicity, high hit rate, and ability to be implemented with O(1) time
   * complexity.
   */

  /** The number of CPUs */
  static final int NCPU = Runtime.getRuntime().availableProcessors();

  /** The maximum weighted capacity of the map. */
  static final long MAXIMUM_CAPACITY = Long.MAX_VALUE - Integer.MAX_VALUE;

  /** The number of read buffers to use. */
  static final int NUMBER_OF_READ_BUFFERS = ceilingNextPowerOfTwo(NCPU);

  /** Mask value for indexing into the read buffers. */
  static final int READ_BUFFERS_MASK = NUMBER_OF_READ_BUFFERS - 1;

  /** The number of pending read operations before attempting to drain. */
  static final int READ_BUFFER_THRESHOLD = 32;

  /** The maximum number of read operations to perform per amortized drain. */
  static final int READ_BUFFER_DRAIN_THRESHOLD = 2 * READ_BUFFER_THRESHOLD;

  /** The maximum number of pending reads per buffer. */
  static final int READ_BUFFER_SIZE = 2 * READ_BUFFER_DRAIN_THRESHOLD;

  /** Mask value for indexing into the read buffer. */
  static final int READ_BUFFER_INDEX_MASK = READ_BUFFER_SIZE - 1;

  /** The maximum number of write operations to perform per amortized drain. */
  static final int WRITE_BUFFER_DRAIN_THRESHOLD = 16;

  /** A queue that discards all entries. */
  static final Queue<?> DISCARDING_QUEUE = new DiscardingQueue();

  /**
   * Returns the smallest power of two greater than or equal to the supplied value.
   *
   * @param x the value to round up
   * @return the rounded power of two
   */
  static int ceilingNextPowerOfTwo(int x) {
    // From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
    return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
  }

  // The backing data store holding the key-value associations
  /** Backing store for key-to-node associations. */
  final ConcurrentMap<K, Node<K, V>> data;
  /** Configured concurrency level for the backing map. */
  final int concurrencyLevel;

  // These fields provide support to bound the map by a maximum capacity
  /** Number of reads drained from each read buffer. */
  @GuardedBy("evictionLock")
  final long[] readBufferReadCount;
  /** Deque maintaining eviction order. */
  @GuardedBy("evictionLock")
  final LinkedDeque<Node<K, V>> evictionDeque;

  /** Current combined weight of all live entries. */
  @GuardedBy("evictionLock") // must write under lock
  final AtomicLong weightedSize;
  /** Maximum allowed combined weight. */
  @GuardedBy("evictionLock") // must write under lock
  final AtomicLong capacity;

  /** Lock protecting eviction bookkeeping. */
  final Lock evictionLock;
  /** Pending write tasks awaiting eviction bookkeeping. */
  final Queue<Runnable> writeBuffer;
  /** Number of reads written to each read buffer. */
  final AtomicLong[] readBufferWriteCount;
  /** Read-buffer write counts observed at the last drain. */
  final AtomicLong[] readBufferDrainAtWriteCount;
  /** Read buffers holding recently accessed nodes. */
  final AtomicReference<Node<K, V>>[][] readBuffers;

  /** Current drain state for the read and write buffers. */
  final AtomicReference<DrainStatus> drainStatus;
  /** Strategy used to weigh entries. */
  final EntryWeigher<? super K, ? super V> weigher;

  // These fields provide support for notifying a listener.
  /** Entries awaiting listener notification. */
  final Queue<Node<K, V>> pendingNotifications;
  /** Listener notified when entries are evicted. */
  final EvictionListener<K, V> listener;

  /** Lazily created key-set view. */
  transient Set<K> keySet;
  /** Lazily created values view. */
  transient Collection<V> values;
  /** Lazily created entry-set view. */
  transient Set<Entry<K, V>> entrySet;

  /**
   * Creates an instance based on the builder's configuration.
   */
  @SuppressWarnings({"unchecked", "cast"})
  private ConcurrentLinkedHashMap(Builder<K, V> builder) {
    // The data store and its maximum capacity
    concurrencyLevel = builder.concurrencyLevel;
    capacity = new AtomicLong(Math.min(builder.capacity, MAXIMUM_CAPACITY));
    data = new ConcurrentHashMap<>(builder.initialCapacity, 0.75f, concurrencyLevel);

    // The eviction support
    weigher = builder.weigher;
    evictionLock = new ReentrantLock();
    weightedSize = new AtomicLong();
    evictionDeque = new LinkedDeque<>();
    writeBuffer = new ConcurrentLinkedQueue<>();
    drainStatus = new AtomicReference<>(IDLE);

    readBufferReadCount = new long[NUMBER_OF_READ_BUFFERS];
    readBufferWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
    readBufferDrainAtWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
    readBuffers = new AtomicReference[NUMBER_OF_READ_BUFFERS][READ_BUFFER_SIZE];
    for (int i = 0; i < NUMBER_OF_READ_BUFFERS; i++) {
      readBufferWriteCount[i] = new AtomicLong();
      readBufferDrainAtWriteCount[i] = new AtomicLong();
      readBuffers[i] = new AtomicReference[READ_BUFFER_SIZE];
      for (int j = 0; j < READ_BUFFER_SIZE; j++) {
        readBuffers[i][j] = new AtomicReference<>();
      }
    }

    // The notification queue and listener
    listener = builder.listener;
    pendingNotifications = (listener == DiscardingListener.INSTANCE)
        ? (Queue<Node<K, V>>) DISCARDING_QUEUE
        : new ConcurrentLinkedQueue<>();
  }

  /** Ensures that the object is not null. */
  static void checkNotNull(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
  }

  /** Ensures that the argument expression is true. */
  static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  /** Ensures that the state expression is true. */
  static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /* ---------------- Eviction Support -------------- */

  /**
   * Retrieves the maximum weighted capacity of the map.
   *
   * @return the maximum weighted capacity
   */
  public long capacity() {
    return capacity.get();
  }

  /**
   * Sets the maximum weighted capacity of the map and eagerly evicts entries
   * until it shrinks to the appropriate size.
   *
   * @param capacity the maximum weighted capacity of the map
   * @throws IllegalArgumentException if the capacity is negative
   */
  public void setCapacity(long capacity) {
    checkArgument(capacity >= 0);
    evictionLock.lock();
    try {
      this.capacity.lazySet(Math.min(capacity, MAXIMUM_CAPACITY));
      drainBuffers();
      evict();
    } finally {
      evictionLock.unlock();
    }
    notifyListener();
  }

  /** Determines whether the map has exceeded its capacity. */
  @GuardedBy("evictionLock")
  boolean hasOverflowed() {
    return weightedSize.get() > capacity.get();
  }

  /**
   * Evicts entries from the map while it exceeds the capacity and appends
   * evicted entries to the notification queue for processing.
   */
  @GuardedBy("evictionLock")
  void evict() {
    // Attempts to evict entries from the map if it exceeds the maximum
    // capacity. If the eviction fails due to a concurrent removal of the
    // victim, that removal may cancel out the addition that triggered this
    // eviction. The victim is eagerly unlinked before the removal task so
    // that if an eviction is still required then a new victim will be chosen
    // for removal.
    while (hasOverflowed()) {
      final Node<K, V> node = evictionDeque.poll();

      // If weighted values are used, then the pending operations will adjust
      // the size to reflect the correct weight
      if (node == null) {
        return;
      }

      // Notify the listener only if the entry was evicted
      if (data.remove(node.key, node)) {
        pendingNotifications.add(node);
      }

      makeDead(node);
    }
  }

  /**
   * Performs the post-processing work required after a read.
   *
   * @param node the entry in the page replacement policy
   */
  void afterRead(Node<K, V> node) {
    final int bufferIndex = readBufferIndex();
    final long writeCount = recordRead(bufferIndex, node);
    drainOnReadIfNeeded(bufferIndex, writeCount);
    notifyListener();
  }

  /** Returns the index to the read buffer to record into. */
  @SuppressWarnings("deprecation")
  static int readBufferIndex() {
    // A buffer is chosen by the thread's id so that tasks are distributed in a
    // pseudo evenly manner. This helps avoid hot entries causing contention
    // due to other threads trying to append to the same buffer.
    return ((int) Thread.currentThread().getId()) & READ_BUFFERS_MASK;
  }

  /**
   * Records a read in the buffer and return its write count.
   *
   * @param bufferIndex the index to the chosen read buffer
   * @param node the entry in the page replacement policy
   * @return the number of writes on the chosen read buffer
   */
  long recordRead(int bufferIndex, Node<K, V> node) {
    // The location in the buffer is chosen in a racy fashion as the increment
    // is not atomic with the insertion. This means that concurrent reads can
    // overlap and overwrite one another, resulting in a lossy buffer.
    final AtomicLong counter = readBufferWriteCount[bufferIndex];
    final long writeCount = counter.get();
    counter.lazySet(writeCount + 1);

    final int index = (int) (writeCount & READ_BUFFER_INDEX_MASK);
    readBuffers[bufferIndex][index].lazySet(node);

    return writeCount;
  }

  /**
   * Attempts to drain the buffers if it is determined to be needed when
   * post-processing a read.
   *
   * @param bufferIndex the index to the chosen read buffer
   * @param writeCount the number of writes on the chosen read buffer
   */
  void drainOnReadIfNeeded(int bufferIndex, long writeCount) {
    final long pending = (writeCount - readBufferDrainAtWriteCount[bufferIndex].get());
    final boolean delayable = (pending < READ_BUFFER_THRESHOLD);
    final DrainStatus status = drainStatus.get();
    if (status.shouldDrainBuffers(delayable)) {
      tryToDrainBuffers();
    }
  }

  /**
   * Performs the post-processing work required after a write.
   *
   * @param task the pending operation to be applied
   */
  void afterWrite(Runnable task) {
    writeBuffer.add(task);
    drainStatus.lazySet(REQUIRED);
    tryToDrainBuffers();
    notifyListener();
  }

  /**
   * Attempts to acquire the eviction lock and apply the pending operations, up
   * to the amortized threshold, to the page replacement policy.
   */
  void tryToDrainBuffers() {
    if (evictionLock.tryLock()) {
      try {
        drainStatus.lazySet(PROCESSING);
        drainBuffers();
      } finally {
        drainStatus.compareAndSet(PROCESSING, IDLE);
        evictionLock.unlock();
      }
    }
  }

  /** Drains the read and write buffers up to an amortized threshold. */
  @GuardedBy("evictionLock")
  void drainBuffers() {
    drainReadBuffers();
    drainWriteBuffer();
  }

  /** Drains the read buffers, each up to an amortized threshold. */
  @GuardedBy("evictionLock")
  @SuppressWarnings("deprecation")
  void drainReadBuffers() {
    final int start = (int) Thread.currentThread().getId();
    final int end = start + NUMBER_OF_READ_BUFFERS;
    for (int i = start; i < end; i++) {
      drainReadBuffer(i & READ_BUFFERS_MASK);
    }
  }

  /** Drains the read buffer up to an amortized threshold. */
  @GuardedBy("evictionLock")
  void drainReadBuffer(int bufferIndex) {
    final long writeCount = readBufferWriteCount[bufferIndex].get();
    for (int i = 0; i < READ_BUFFER_DRAIN_THRESHOLD; i++) {
      final int index = (int) (readBufferReadCount[bufferIndex] & READ_BUFFER_INDEX_MASK);
      final AtomicReference<Node<K, V>> slot = readBuffers[bufferIndex][index];
      final Node<K, V> node = slot.get();
      if (node == null) {
        break;
      }

      slot.lazySet(null);
      applyRead(node);
      readBufferReadCount[bufferIndex]++;
    }
    readBufferDrainAtWriteCount[bufferIndex].lazySet(writeCount);
  }

  /** Updates the node's location in the page replacement policy. */
  @GuardedBy("evictionLock")
  void applyRead(Node<K, V> node) {
    // An entry may be scheduled for reordering despite having been removed.
    // This can occur when the entry was concurrently read while a writer was
    // removing it. If the entry is no longer linked then it does not need to
    // be processed.
    if (evictionDeque.contains(node)) {
      evictionDeque.moveToBack(node);
    }
  }

  /** Drains the read buffer up to an amortized threshold. */
  @GuardedBy("evictionLock")
  void drainWriteBuffer() {
    for (int i = 0; i < WRITE_BUFFER_DRAIN_THRESHOLD; i++) {
      final Runnable task = writeBuffer.poll();
      if (task == null) {
        break;
      }
      task.run();
    }
  }

  /**
   * Attempts to transition the node from the <tt>alive</tt> state to the
   * <tt>retired</tt> state.
   *
   * @param node the entry in the page replacement policy
   * @param expect the expected weighted value
   * @return if successful
   */
  boolean tryToRetire(Node<K, V> node, WeightedValue<V> expect) {
    if (expect.isAlive()) {
      final WeightedValue<V> retired = new WeightedValue<>(expect.value, -expect.weight);
      return node.compareAndSet(expect, retired);
    }
    return false;
  }

  /**
   * Atomically transitions the node from the <tt>alive</tt> state to the
   * <tt>retired</tt> state, if a valid transition.
   *
   * @param node the entry in the page replacement policy
   */
  void makeRetired(Node<K, V> node) {
    for (;;) {
      final WeightedValue<V> current = node.get();
      if (!current.isAlive()) {
        return;
      }
      final WeightedValue<V> retired = new WeightedValue<>(current.value, -current.weight);
      if (node.compareAndSet(current, retired)) {
        return;
      }
    }
  }

  /**
   * Atomically transitions the node to the <tt>dead</tt> state and decrements
   * the <tt>weightedSize</tt>.
   *
   * @param node the entry in the page replacement policy
   */
  @GuardedBy("evictionLock")
  void makeDead(Node<K, V> node) {
    for (;;) {
      WeightedValue<V> current = node.get();
      WeightedValue<V> dead = new WeightedValue<>(current.value, 0);
      if (node.compareAndSet(current, dead)) {
        weightedSize.lazySet(weightedSize.get() - Math.abs(current.weight));
        return;
      }
    }
  }

  /** Notifies the listener of entries that were evicted. */
  void notifyListener() {
    Node<K, V> node;
    while ((node = pendingNotifications.poll()) != null) {
      listener.onEviction(node.key, node.getValue());
    }
  }

  /** Adds the node to the page replacement policy. */
  final class AddTask implements Runnable {
    /** Node affected by this task. */
    final Node<K, V> node;
    /** Weight contributed by the node being added. */
    final int weight;

    /**
     * Creates a task that adds a node to the eviction structures.
     *
     * @param node the node to add
     * @param weight the node weight
     */
    AddTask(Node<K, V> node, int weight) {
      this.weight = weight;
      this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public void run() {
      weightedSize.lazySet(weightedSize.get() + weight);

      // ignore out-of-order write operations
      if (node.get().isAlive()) {
        evictionDeque.add(node);
        evict();
      }
    }
  }

  /** Removes a node from the page replacement policy. */
  final class RemovalTask implements Runnable {
    /** Node affected by this task. */
    final Node<K, V> node;

    /**
     * Creates a task that removes a node from the eviction structures.
     *
     * @param node the node to remove
     */
    RemovalTask(Node<K, V> node) {
      this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public void run() {
      // add may not have been processed yet
      evictionDeque.remove(node);
      makeDead(node);
    }
  }

  /** Updates the weighted size and evicts an entry on overflow. */
  final class UpdateTask implements Runnable {
    /** Weight delta applied by this update task. */
    final int weightDifference;
    /** Node affected by this task. */
    final Node<K, V> node;

    /**
     * Creates a task that updates a node weight within the eviction structures.
     *
     * @param node the node to update
     * @param weightDifference the change in weight
     */
    public UpdateTask(Node<K, V> node, int weightDifference) {
      this.weightDifference = weightDifference;
      this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public void run() {
      weightedSize.lazySet(weightedSize.get() + weightDifference);
      applyRead(node);
      evict();
    }
  }

  /* ---------------- Concurrent Map Support -------------- */

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return data.size();
  }

  /**
   * Returns the weighted size of this map.
   *
   * @return the combined weight of the values in this map
   */
  public long weightedSize() {
    return Math.max(0, weightedSize.get());
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    evictionLock.lock();
    try {
      // Discard all entries
      Node<K, V> node;
      while ((node = evictionDeque.poll()) != null) {
        data.remove(node.key, node);
        makeDead(node);
      }

      // Discard all pending reads
      for (AtomicReference<Node<K, V>>[] buffer : readBuffers) {
        for (AtomicReference<Node<K, V>> slot : buffer) {
          slot.lazySet(null);
        }
      }

      // Apply all pending writes
      Runnable task;
      while ((task = writeBuffer.poll()) != null) {
        task.run();
      }
    } finally {
      evictionLock.unlock();
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key) {
    return data.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    checkNotNull(value);

    for (Node<K, V> node : data.values()) {
      if (node.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public V get(Object key) {
    final Node<K, V> node = data.get(key);
    if (node == null) {
      return null;
    }
    afterRead(node);
    return node.getValue();
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null}
   * if this map contains no mapping for the key. This method differs from
   * {@link #get(Object)} in that it does not record the operation with the
   * page replacement policy.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or
   *     {@code null} if this map contains no mapping for the key
   * @throws NullPointerException if the specified key is null
   */
  public V getQuietly(Object key) {
    final Node<K, V> node = data.get(key);
    return (node == null) ? null : node.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public V put(K key, V value) {
    return put(key, value, false);
  }

  /** {@inheritDoc} */
  @Override
  public V putIfAbsent(K key, V value) {
    return put(key, value, true);
  }

  /**
   * Adds a node to the list and the data store. If an existing node is found,
   * then its value is updated if allowed.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @param onlyIfAbsent a write is performed only if the key is not already
   *     associated with a value
   * @return the prior value in the data store or null if no mapping was found
   */
  V put(K key, V value, boolean onlyIfAbsent) {
    checkNotNull(key);
    checkNotNull(value);

    final int weight = weigher.weightOf(key, value);
    final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);
    final Node<K, V> node = new Node<>(key, weightedValue);

    for (;;) {
      final Node<K, V> prior = data.putIfAbsent(node.key, node);
      if (prior == null) {
        afterWrite(new AddTask(node, weight));
        return null;
      } else if (onlyIfAbsent) {
        afterRead(prior);
        return prior.getValue();
      }
      for (;;) {
        final WeightedValue<V> oldWeightedValue = prior.get();
        if (!oldWeightedValue.isAlive()) {
          break;
        }

        if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
          final int weightedDifference = weight - oldWeightedValue.weight;
          if (weightedDifference == 0) {
            afterRead(prior);
          } else {
            afterWrite(new UpdateTask(prior, weightedDifference));
          }
          return oldWeightedValue.value;
        }
      }
    }
  }

  /**
   * If the specified key is not already associated with a value,
   * attempts to compute its value using the given mapping function
   * and enters it into this map unless {@code null}.  The entire
   * method invocation is performed atomically, so the function is
   * applied at most once per key.  Some attempted update operations
   * on this map by other threads may be blocked while computation
   * is in progress, so the computation should be short and simple,
   * and must not attempt to update any other mappings of this map.
   *
   * @param key key with which the specified value is to be associated
   * @param mappingFunction the function to compute a value
   * @return the current (existing or computed) value associated with
   *         the specified key, or null if the computed value is null
   * @throws NullPointerException if the specified key or mappingFunction
   *         is null
   * @throws IllegalStateException if the computation detectably
   *         attempts a recursive update to this map that would
   *         otherwise never complete
   * @throws RuntimeException or Error if the mappingFunction does so,
   *         in which case the mapping is left unestablished
   */
  @Override
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    return compute(key, mappingFunction, true);
  }

  /**
   * Computes and stores a value for the supplied key.
   *
   * @param key the key to compute
   * @param mappingFunction the function used to compute a value
   * @param onlyIfAbsent whether an existing mapping should be preserved
   * @return the current value associated with the key
   */
  V compute(final K key, final Function<? super K, ? extends V> mappingFunction, boolean onlyIfAbsent) {
    checkNotNull(key);
    checkNotNull(mappingFunction);

    final ObjectHolder<Node<K, V>> objectHolder = new ObjectHolder<>();

    for (;;) {
      Function<K, Node<K, V>> f = k -> {
        final V value = mappingFunction.apply(key);

        checkNotNull(value);

        final int weight = weigher.weightOf(key, value);
        final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);
        final Node<K, V> node = new Node<>(key, weightedValue);

        objectHolder.setObject(node);

        return node;
      };
      Node<K, V> prior = data.computeIfAbsent(key, f);

      Node<K, V> node = objectHolder.getObject();
      if (null == node) { // the entry is present
        V value = prior.getValue();
        final int weight = weigher.weightOf(key, value);
        final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);
        node = new Node<>(key, weightedValue);
      } else {
        // the return value of `computeIfAbsent` is different from the one of `putIfAbsent`.
        // if the key is absent in map, the return value of `computeIfAbsent` is the newly computed value, but `putIfAbsent` return null.
        // prior should keep the value with the same meaning of the return value of `putIfAbsent`, so reset it as null here.
        prior = null;
      }
      final WeightedValue<V> weightedValue = node.weightedValue;
      final int weight = weightedValue.weight;

      if (prior == null) {
        afterWrite(new AddTask(node, weight));
        return weightedValue.value;
      } else if (onlyIfAbsent) {
        afterRead(prior);
        return prior.getValue();
      }
      for (;;) {
        final WeightedValue<V> oldWeightedValue = prior.get();
        if (!oldWeightedValue.isAlive()) {
          break;
        }

        if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
          final int weightedDifference = weight - oldWeightedValue.weight;
          if (weightedDifference == 0) {
            afterRead(prior);
          } else {
            afterWrite(new UpdateTask(prior, weightedDifference));
          }
          return oldWeightedValue.value;
        }
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public V remove(Object key) {
    final Node<K, V> node = data.remove(key);
    if (node == null) {
      return null;
    }

    makeRetired(node);
    afterWrite(new RemovalTask(node));
    return node.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean remove(Object key, Object value) {
    final Node<K, V> node = data.get(key);
    if ((node == null) || (value == null)) {
      return false;
    }

    WeightedValue<V> weightedValue = node.get();
    for (;;) {
      if (weightedValue.contains(value)) {
        if (tryToRetire(node, weightedValue)) {
          if (data.remove(key, node)) {
            afterWrite(new RemovalTask(node));
            return true;
          }
        } else {
          weightedValue = node.get();
          if (weightedValue.isAlive()) {
            // retry as an intermediate update may have replaced the value with
            // an equal instance that has a different reference identity
            continue;
          }
        }
      }
      return false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public V replace(K key, V value) {
    checkNotNull(key);
    checkNotNull(value);

    final int weight = weigher.weightOf(key, value);
    final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);

    final Node<K, V> node = data.get(key);
    if (node == null) {
      return null;
    }
    for (;;) {
      final WeightedValue<V> oldWeightedValue = node.get();
      if (!oldWeightedValue.isAlive()) {
        return null;
      }
      if (node.compareAndSet(oldWeightedValue, weightedValue)) {
        final int weightedDifference = weight - oldWeightedValue.weight;
        if (weightedDifference == 0) {
          afterRead(node);
        } else {
          afterWrite(new UpdateTask(node, weightedDifference));
        }
        return oldWeightedValue.value;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    checkNotNull(key);
    checkNotNull(oldValue);
    checkNotNull(newValue);

    final int weight = weigher.weightOf(key, newValue);
    final WeightedValue<V> newWeightedValue = new WeightedValue<>(newValue, weight);

    final Node<K, V> node = data.get(key);
    if (node == null) {
      return false;
    }
    for (;;) {
      final WeightedValue<V> weightedValue = node.get();
      if (!weightedValue.isAlive() || !weightedValue.contains(oldValue)) {
        return false;
      }
      if (node.compareAndSet(weightedValue, newWeightedValue)) {
        final int weightedDifference = weight - weightedValue.weight;
        if (weightedDifference == 0) {
          afterRead(node);
        } else {
          afterWrite(new UpdateTask(node, weightedDifference));
        }
        return true;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public Set<K> keySet() {
    final Set<K> ks = keySet;
    return (ks == null) ? (keySet = new KeySet()) : ks;
  }

  /**
   * Returns an unmodifiable snapshot {@link Set} view of the keys contained in
   * this map. The set's iterator returns the keys whose order of iteration is
   * the ascending order in which its entries are considered eligible for
   * retention, from the least-likely to be retained to the most-likely.
   * <p>
   * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em>
   * a constant-time operation. Because of the asynchronous nature of the page
   * replacement policy, determining the retention ordering requires a traversal
   * of the keys.
   *
   * @return an ascending snapshot view of the keys in this map
   */
  public Set<K> ascendingKeySet() {
    return ascendingKeySetWithLimit(Integer.MAX_VALUE);
  }

  /**
   * Returns an unmodifiable snapshot {@link Set} view of the keys contained in
   * this map. The set's iterator returns the keys whose order of iteration is
   * the ascending order in which its entries are considered eligible for
   * retention, from the least-likely to be retained to the most-likely.
   * <p>
   * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em>
   * a constant-time operation. Because of the asynchronous nature of the page
   * replacement policy, determining the retention ordering requires a traversal
   * of the keys.
   *
   * @param limit the maximum size of the returned set
   * @return an ascending snapshot view of the keys in this map
   * @throws IllegalArgumentException if the limit is negative
   */
  public Set<K> ascendingKeySetWithLimit(int limit) {
    return orderedKeySet(true, limit);
  }

  /**
   * Returns an unmodifiable snapshot {@link Set} view of the keys contained in
   * this map. The set's iterator returns the keys whose order of iteration is
   * the descending order in which its entries are considered eligible for
   * retention, from the most-likely to be retained to the least-likely.
   * <p>
   * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em>
   * a constant-time operation. Because of the asynchronous nature of the page
   * replacement policy, determining the retention ordering requires a traversal
   * of the keys.
   *
   * @return a descending snapshot view of the keys in this map
   */
  public Set<K> descendingKeySet() {
    return descendingKeySetWithLimit(Integer.MAX_VALUE);
  }

  /**
   * Returns an unmodifiable snapshot {@link Set} view of the keys contained in
   * this map. The set's iterator returns the keys whose order of iteration is
   * the descending order in which its entries are considered eligible for
   * retention, from the most-likely to be retained to the least-likely.
   * <p>
   * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em>
   * a constant-time operation. Because of the asynchronous nature of the page
   * replacement policy, determining the retention ordering requires a traversal
   * of the keys.
   *
   * @param limit the maximum size of the returned set
   * @return a descending snapshot view of the keys in this map
   * @throws IllegalArgumentException if the limit is negative
   */
  public Set<K> descendingKeySetWithLimit(int limit) {
    return orderedKeySet(false, limit);
  }

  /**
   * Creates an ordered snapshot of the keys currently retained by the map.
   *
   * @param ascending whether to iterate from least-recent to most-recent retention
   * @param limit the maximum number of keys to include
   * @return the ordered key snapshot
   */
  Set<K> orderedKeySet(boolean ascending, int limit) {
    checkArgument(limit >= 0);
    evictionLock.lock();
    try {
      drainBuffers();

      final int initialCapacity = (weigher == Weighers.entrySingleton())
          ? Math.min(limit, (int) weightedSize())
          : 16;
      final Set<K> keys = new LinkedHashSet<>(initialCapacity);
      final Iterator<Node<K, V>> iterator = ascending
          ? evictionDeque.iterator()
          : evictionDeque.descendingIterator();
      while (iterator.hasNext() && (limit > keys.size())) {
        keys.add(iterator.next().key);
      }
      return unmodifiableSet(keys);
    } finally {
      evictionLock.unlock();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Collection<V> values() {
    final Collection<V> vs = values;
    return (vs == null) ? (values = new Values()) : vs;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Entry<K, V>> entrySet() {
    final Set<Entry<K, V>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet()) : es;
  }

  /**
   * Returns an unmodifiable snapshot {@link Map} view of the mappings contained
   * in this map. The map's collections return the mappings whose order of
   * iteration is the ascending order in which its entries are considered
   * eligible for retention, from the least-likely to be retained to the
   * most-likely.
   * <p>
   * Beware that obtaining the mappings is <em>NOT</em> a constant-time
   * operation. Because of the asynchronous nature of the page replacement
   * policy, determining the retention ordering requires a traversal of the
   * entries.
   *
   * @return an ascending snapshot view of this map
   */
  public Map<K, V> ascendingMap() {
    return ascendingMapWithLimit(Integer.MAX_VALUE);
  }

  /**
   * Returns an unmodifiable snapshot {@link Map} view of the mappings contained
   * in this map. The map's collections return the mappings whose order of
   * iteration is the ascending order in which its entries are considered
   * eligible for retention, from the least-likely to be retained to the
   * most-likely.
   * <p>
   * Beware that obtaining the mappings is <em>NOT</em> a constant-time
   * operation. Because of the asynchronous nature of the page replacement
   * policy, determining the retention ordering requires a traversal of the
   * entries.
   *
   * @param limit the maximum size of the returned map
   * @return an ascending snapshot view of this map
   * @throws IllegalArgumentException if the limit is negative
   */
  public Map<K, V> ascendingMapWithLimit(int limit) {
    return orderedMap(true, limit);
  }

  /**
   * Returns an unmodifiable snapshot {@link Map} view of the mappings contained
   * in this map. The map's collections return the mappings whose order of
   * iteration is the descending order in which its entries are considered
   * eligible for retention, from the most-likely to be retained to the
   * least-likely.
   * <p>
   * Beware that obtaining the mappings is <em>NOT</em> a constant-time
   * operation. Because of the asynchronous nature of the page replacement
   * policy, determining the retention ordering requires a traversal of the
   * entries.
   *
   * @return a descending snapshot view of this map
   */
  public Map<K, V> descendingMap() {
    return descendingMapWithLimit(Integer.MAX_VALUE);
  }

  /**
   * Returns an unmodifiable snapshot {@link Map} view of the mappings contained
   * in this map. The map's collections return the mappings whose order of
   * iteration is the descending order in which its entries are considered
   * eligible for retention, from the most-likely to be retained to the
   * least-likely.
   * <p>
   * Beware that obtaining the mappings is <em>NOT</em> a constant-time
   * operation. Because of the asynchronous nature of the page replacement
   * policy, determining the retention ordering requires a traversal of the
   * entries.
   *
   * @param limit the maximum size of the returned map
   * @return a descending snapshot view of this map
   * @throws IllegalArgumentException if the limit is negative
   */
  public Map<K, V> descendingMapWithLimit(int limit) {
    return orderedMap(false, limit);
  }

  /**
   * Creates an ordered snapshot of the mappings currently retained by the map.
   *
   * @param ascending whether to iterate from least-recent to most-recent retention
   * @param limit the maximum number of entries to include
   * @return the ordered mapping snapshot
   */
  Map<K, V> orderedMap(boolean ascending, int limit) {
    checkArgument(limit >= 0);
    evictionLock.lock();
    try {
      drainBuffers();

      final int initialCapacity = (weigher == Weighers.entrySingleton())
          ? Math.min(limit, (int) weightedSize())
          : 16;
      final Map<K, V> map = new LinkedHashMap<>(initialCapacity);
      final Iterator<Node<K, V>> iterator = ascending
          ? evictionDeque.iterator()
          : evictionDeque.descendingIterator();
      while (iterator.hasNext() && (limit > map.size())) {
        Node<K, V> node = iterator.next();
        map.put(node.key, node.getValue());
      }
      return unmodifiableMap(map);
    } finally {
      evictionLock.unlock();
    }
  }

  /** The draining status of the buffers. */
  enum DrainStatus {

    /** A drain is not taking place. */
    IDLE {
      /** {@inheritDoc} */
      @Override boolean shouldDrainBuffers(boolean delayable) {
        return !delayable;
      }
    },

    /** A drain is required due to a pending write modification. */
    REQUIRED {
      /** {@inheritDoc} */
      @Override boolean shouldDrainBuffers(boolean delayable) {
        return true;
      }
    },

    /** A drain is in progress. */
    PROCESSING {
      /** {@inheritDoc} */
      @Override boolean shouldDrainBuffers(boolean delayable) {
        return false;
      }
    };

    /**
     * Determines whether the buffers should be drained.
     *
     * @param delayable if a drain should be delayed until required
     * @return if a drain should be attempted
     */
    abstract boolean shouldDrainBuffers(boolean delayable);
  }

  /** A value, its weight, and the entry's status. */
  @Immutable
  static final class WeightedValue<V> {
    /** Encoded weight for the current value state. */
    final int weight;
    /** Value associated with this weight record. */
    final V value;

    /**
     * Creates a weighted value state.
     *
     * @param value the stored value
     * @param weight the encoded weight and state
     */
    WeightedValue(V value, int weight) {
      this.weight = weight;
      this.value = value;
    }

    /**
     * Returns whether the supplied object matches the stored value.
     *
     * @param o the object to test
     * @return {@code true} if the object matches the stored value
     */
    boolean contains(Object o) {
      return (o == value) || value.equals(o);
    }

    /**
     * If the entry is available in the hash-table and page replacement policy.
     */
    boolean isAlive() {
      return weight > 0;
    }

    /**
     * If the entry was removed from the hash-table and is awaiting removal from
     * the page replacement policy.
     */
    boolean isRetired() {
      return weight < 0;
    }

    /**
     * If the entry was removed from the hash-table and the page replacement
     * policy.
     */
    boolean isDead() {
      return weight == 0;
    }
  }

  /**
   * A node contains the key, the weighted value, and the linkage pointers on
   * the page-replacement algorithm's data structures.
   */
  @SuppressWarnings("serial")
  static final class Node<K, V> extends AtomicReference<WeightedValue<V>>
      implements Linked<Node<K, V>> {
    /** Entry key. */
    final K key;
    /** Previous node in eviction order. */
    @GuardedBy("evictionLock")
    Node<K, V> prev;
    /** Next node in eviction order. */
    @GuardedBy("evictionLock")
    Node<K, V> next;
    /** Initial weighted value assigned to this node. */
    WeightedValue<V> weightedValue;

    /** Creates a new, unlinked node. */
    Node(K key, WeightedValue<V> weightedValue) {
      super(weightedValue);
      this.key = key;
      this.weightedValue = weightedValue;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public Node<K, V> getPrevious() {
      return prev;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public void setPrevious(Node<K, V> prev) {
      this.prev = prev;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public Node<K, V> getNext() {
      return next;
    }

    /** {@inheritDoc} */
    @Override
    @GuardedBy("evictionLock")
    public void setNext(Node<K, V> next) {
      this.next = next;
    }

    /** Retrieves the value held by the current <tt>WeightedValue</tt>. */
    V getValue() {
      return get().value;
    }

    /**
     * Returns the weighted value captured when this node was created.
     *
     * @return the initial weighted value
     */
    WeightedValue<V> getWeightedValue() {
      return this.weightedValue;
    }
  }

  /** An adapter to safely externalize the keys. */
  final class KeySet extends AbstractSet<K> {
    /** Backing map for this view. */
    final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

    /** {@inheritDoc} */
    @Override
    public int size() {
      return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
      map.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object obj) {
      return containsKey(obj);
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object obj) {
      return (map.remove(obj) != null);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
      return map.data.keySet().toArray();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T[] toArray(T[] array) {
      return map.data.keySet().toArray(array);
    }
  }

  /** An adapter to safely externalize the key iterator. */
  final class KeyIterator implements Iterator<K> {
    /** Delegate iterator over the backing key set. */
    final Iterator<K> iterator = data.keySet().iterator();
    /** Most recently returned key. */
    K current;

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public K next() {
      current = iterator.next();
      return current;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
      checkState(current != null);
      ConcurrentLinkedHashMap.this.remove(current);
      current = null;
    }
  }

  /** An adapter to safely externalize the values. */
  final class Values extends AbstractCollection<V> {

    /** {@inheritDoc} */
    @Override
    public int size() {
      return ConcurrentLinkedHashMap.this.size();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
      ConcurrentLinkedHashMap.this.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
      return containsValue(o);
    }
  }

  /** An adapter to safely externalize the value iterator. */
  final class ValueIterator implements Iterator<V> {
    /** Delegate iterator over the backing values. */
    final Iterator<Node<K, V>> iterator = data.values().iterator();
    /** Most recently returned node. */
    Node<K, V> current;

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public V next() {
      current = iterator.next();
      return current.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
      checkState(current != null);
      ConcurrentLinkedHashMap.this.remove(current.key);
      current = null;
    }
  }

  /** An adapter to safely externalize the entries. */
  final class EntrySet extends AbstractSet<Entry<K, V>> {
    /** Backing map for this view. */
    final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

    /** {@inheritDoc} */
    @Override
    public int size() {
      return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
      map.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object obj) {
      if (!(obj instanceof Entry<?, ?> entry)) {
        return false;
      }
        Node<K, V> node = map.data.get(entry.getKey());
      return (node != null) && (node.getValue().equals(entry.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(Entry<K, V> entry) {
      return (map.putIfAbsent(entry.getKey(), entry.getValue()) == null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object obj) {
      if (!(obj instanceof Entry<?, ?> entry)) {
        return false;
      }
        return map.remove(entry.getKey(), entry.getValue());
    }
  }

  /** An adapter to safely externalize the entry iterator. */
  final class EntryIterator implements Iterator<Entry<K, V>> {
    /** Delegate iterator over the backing values. */
    final Iterator<Node<K, V>> iterator = data.values().iterator();
    /** Most recently returned node. */
    Node<K, V> current;

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public Entry<K, V> next() {
      current = iterator.next();
      return new WriteThroughEntry(current);
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
      checkState(current != null);
      ConcurrentLinkedHashMap.this.remove(current.key);
      current = null;
    }
  }

  /** An entry that allows updates to write through to the map. */
  final class WriteThroughEntry extends SimpleEntry<K, V> {
      @Serial private static final long serialVersionUID = 1;

    /**
     * Creates a write-through entry backed by the supplied node.
     *
     * @param node the backing node
     */
    WriteThroughEntry(Node<K, V> node) {
      super(node.key, node.getValue());
    }

    /** {@inheritDoc} */
    @Override
    public V setValue(V value) {
      put(getKey(), value);
      return super.setValue(value);
    }

    /**
     * Returns the serial proxy for this object.
     *
     * @return the serial replacement
     */
    @Serial
    Object writeReplace() {
      return new SimpleEntry<>(this);
    }
  }

  /** A weigher that enforces that the weight falls within a valid range. */
  static final class BoundedEntryWeigher<K, V> implements EntryWeigher<K, V>, Serializable {
      @Serial private static final long serialVersionUID = 1;
    /** Delegate weigher to validate. */
    final EntryWeigher<? super K, ? super V> weigher;

    /**
     * Creates a validating entry weigher wrapper.
     *
     * @param weigher the delegate weigher
     */
    BoundedEntryWeigher(EntryWeigher<? super K, ? super V> weigher) {
      checkNotNull(weigher);
      this.weigher = weigher;
    }

    /** {@inheritDoc} */
    @Override
    public int weightOf(K key, V value) {
      int weight = weigher.weightOf(key, value);
      checkArgument(weight >= 1);
      return weight;
    }

    /**
     * Returns the serial proxy for this object.
     *
     * @return the serial replacement
     */
    @Serial
    Object writeReplace() {
      return weigher;
    }
  }

  /** A queue that discards all additions and is always empty. */
  static final class DiscardingQueue extends AbstractQueue<Object> {
    /** {@inheritDoc} */
    @Override public boolean add(Object e) { return true; }
    /** {@inheritDoc} */
    @Override public boolean offer(Object e) { return true; }
    /** {@inheritDoc} */
    @Override public Object poll() { return null; }
    /** {@inheritDoc} */
    @Override public Object peek() { return null; }
    /** {@inheritDoc} */
    @Override public int size() { return 0; }
    /** {@inheritDoc} */
    @Override public Iterator<Object> iterator() { return Collections.emptyIterator(); }
  }

  /** A listener that ignores all notifications. */
  enum DiscardingListener implements EvictionListener<Object, Object> {
    /** Singleton instance. */
    INSTANCE;

    /** {@inheritDoc} */
    @Override public void onEviction(Object key, Object value) {}
  }

    /* ---------------- Serialization Support -------------- */

    @Serial private static final long serialVersionUID = 1;

  /**
   * Returns the serial proxy for this object.
   *
   * @return the serial replacement
   */
  @Serial
  Object writeReplace() {
    return new SerializationProxy<>(this);
  }

  @Serial
  private void readObject(ObjectInputStream stream) throws InvalidObjectException {
    throw new InvalidObjectException("Proxy required");
  }

  /**
   * A proxy that is serialized instead of the map. The page-replacement
   * algorithm's data structures are not serialized so the deserialized
   * instance contains only the entries. This is acceptable as caches hold
   * transient data that is recomputable and serialization would tend to be
   * used as a fast warm-up process.
   */
  static final class SerializationProxy<K, V> implements Serializable {
    /** Entry weigher to restore after deserialization. */
    final EntryWeigher<? super K, ? super V> weigher;
    /** Listener to restore after deserialization. */
    final EvictionListener<K, V> listener;
    /** Configured concurrency level for the backing map. */
    final int concurrencyLevel;
    /** Snapshot of entries to restore after deserialization. */
    final Map<K, V> data;
    /** Maximum weighted capacity to restore after deserialization. */
    final long capacity;

    /**
     * Creates a serialization proxy for the supplied map.
     *
     * @param map the map to serialize
     */
    SerializationProxy(ConcurrentLinkedHashMap<K, V> map) {
      concurrencyLevel = map.concurrencyLevel;
      data = new HashMap<>(map);
      capacity = map.capacity.get();
      listener = map.listener;
      weigher = map.weigher;
    }

    /**
     * Restores a live map instance from this proxy.
     *
     * @return the restored map
     */
    @Serial
    Object readResolve() {
      ConcurrentLinkedHashMap<K, V> map = new Builder<K, V>()
          .concurrencyLevel(concurrencyLevel)
          .maximumWeightedCapacity(capacity)
          .listener(listener)
          .weigher(weigher)
          .build();
      map.putAll(data);
      return map;
    }

      @Serial private static final long serialVersionUID = 1;
  }

  /* ---------------- Builder -------------- */

  /**
   * A builder that creates {@link ConcurrentLinkedHashMap} instances. It
   * provides a flexible approach for constructing customized instances with
   * a named parameter syntax. It can be used in the following manner:
   * <pre>{@code
   * ConcurrentMap<Vertex, Set<Edge>> graph = new Builder<Vertex, Set<Edge>>()
   *     .maximumWeightedCapacity(5000)
   *     .weigher(Weighers.<Edge>set())
   *     .build();
   * }</pre>
   */
  public static final class Builder<K, V> {
    /** Default concurrency level used by the builder. */
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    /** Default initial capacity used by the builder. */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /** Listener to notify when entries are evicted. */
    EvictionListener<K, V> listener;
    /** Strategy used to weigh entries created by the builder. */
    EntryWeigher<? super K, ? super V> weigher;

    /** Requested concurrency level for created maps. */
    int concurrencyLevel;
    /** Requested initial capacity for created maps. */
    int initialCapacity;
    /** Requested maximum weighted capacity for created maps. */
    long capacity;

    /**
     * Creates a builder with the default settings.
     */
    @SuppressWarnings("unchecked")
    public Builder() {
      capacity = -1;
      weigher = Weighers.entrySingleton();
      initialCapacity = DEFAULT_INITIAL_CAPACITY;
      concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
      listener = (EvictionListener<K, V>) DiscardingListener.INSTANCE;
    }

    /**
     * Specifies the initial capacity of the hash table (default <tt>16</tt>).
     * This is the number of key-value pairs that the hash table can hold
     * before a resize operation is required.
     *
     * @param initialCapacity the initial capacity used to size the hash table
     *     to accommodate this many entries.
     * @throws IllegalArgumentException if the initialCapacity is negative
     */
    public Builder<K, V> initialCapacity(int initialCapacity) {
      checkArgument(initialCapacity >= 0);
      this.initialCapacity = initialCapacity;
      return this;
    }

    /**
     * Specifies the maximum weighted capacity to coerce the map to and may
     * exceed it temporarily.
     *
     * @param capacity the weighted threshold to bound the map by
     * @throws IllegalArgumentException if the maximumWeightedCapacity is
     *     negative
     */
    public Builder<K, V> maximumWeightedCapacity(long capacity) {
      checkArgument(capacity >= 0);
      this.capacity = capacity;
      return this;
    }

    /**
     * Specifies the estimated number of concurrently updating threads. The
     * implementation performs internal sizing to try to accommodate this many
     * threads (default <tt>16</tt>).
     *
     * @param concurrencyLevel the estimated number of concurrently updating
     *     threads
     * @throws IllegalArgumentException if the concurrencyLevel is less than or
     *     equal to zero
     */
    public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
      checkArgument(concurrencyLevel > 0);
      this.concurrencyLevel = concurrencyLevel;
      return this;
    }

    /**
     * Specifies an optional listener that is registered for notification when
     * an entry is evicted.
     *
     * @param listener the object to forward evicted entries to
     * @throws NullPointerException if the listener is null
     */
    public Builder<K, V> listener(EvictionListener<K, V> listener) {
      checkNotNull(listener);
      this.listener = listener;
      return this;
    }

    /**
     * Specifies an algorithm to determine how many the units of capacity a
     * value consumes. The default algorithm bounds the map by the number of
     * key-value pairs by giving each entry a weight of <tt>1</tt>.
     *
     * @param weigher the algorithm to determine a value's weight
     * @throws NullPointerException if the weigher is null
     */
    public Builder<K, V> weigher(Weigher<? super V> weigher) {
      this.weigher = (weigher == Weighers.singleton())
          ? Weighers.entrySingleton()
          : new BoundedEntryWeigher<>(Weighers.asEntryWeigher(weigher));
      return this;
    }

    /**
     * Specifies an algorithm to determine how many the units of capacity an
     * entry consumes. The default algorithm bounds the map by the number of
     * key-value pairs by giving each entry a weight of <tt>1</tt>.
     *
     * @param weigher the algorithm to determine an entry's weight
     * @throws NullPointerException if the weigher is null
     */
    public Builder<K, V> weigher(EntryWeigher<? super K, ? super V> weigher) {
      this.weigher = (weigher == Weighers.entrySingleton())
          ? Weighers.entrySingleton()
          : new BoundedEntryWeigher<>(weigher);
      return this;
    }

    /**
     * Creates a new {@link ConcurrentLinkedHashMap} instance.
     *
     * @throws IllegalStateException if the maximum weighted capacity was
     *     not set
     */
    public ConcurrentLinkedHashMap<K, V> build() {
      checkState(capacity >= 0);
      return new ConcurrentLinkedHashMap<>(this);
    }
  }
}

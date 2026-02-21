/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hbm.lib.maps;

import com.hbm.lib.Library;
import com.hbm.util.ObjectIntPair;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.LongConsumer;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

public class NonBlockingHashSetLong extends AbstractLongSet implements LongSet, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final int REPROBE_LIMIT = 10;
    private static final int MIN_SIZE_LOG = 4;
    private static final int MIN_SIZE = 1 << MIN_SIZE_LOG;

    private static final long NO_KEY = 0L;
    private static final int ST_EMPTY = 0;
    private static final int ST_PRESENT = 1;
    private static final int ST_TOMB = 2;
    private static final int PRIME_BIT = 0x8000_0000;

    private static final int EXP_ANY = -1;
    private static final int EXP_PRESENT = -2;
    private static final int EXP_ABSENT = -3;
    private static final int EXP_EMPTY_COPY = -4;

    private static final long L_BASE = U.arrayBaseOffset(long[].class);
    private static final int L_SCALE = U.arrayIndexScale(long[].class);
    private static final int L_SHIFT = Integer.numberOfTrailingZeros(L_SCALE);

    private static final long _chs_offset = fieldOffset(NonBlockingHashSetLong.class, "_chs");
    private static final long _zero_offset = fieldOffset(NonBlockingHashSetLong.class, "_zeroState");
    private static final int SNAPSHOT_LEAF_SLOTS = 1 << 12; // 4096
    private static final int SNAPSHOT_BLOCK_SIZE = 1 << 10; // 1024
    private final boolean _opt_for_space;
    private transient volatile CHS _chs;
    private transient volatile int _zeroState;
    private transient volatile long _last_resize_milli;

    public NonBlockingHashSetLong() {
        this(MIN_SIZE, true);
    }

    public NonBlockingHashSetLong(int initialCapacity) {
        this(initialCapacity, true);
    }

    public NonBlockingHashSetLong(boolean optForSpace) {
        this(1, optForSpace);
    }

    public NonBlockingHashSetLong(int initialCapacity, boolean optForSpace) {
        _opt_for_space = optForSpace;
        initialize(initialCapacity);
    }

    protected int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long offLong(int physIdx) {
        return L_BASE + ((long) physIdx << L_SHIFT);
    }

    private static long st32(int st) {
        return ((long) st) & 0xFFFF_FFFFL;
    }

    private static boolean isPrime(int st) {
        return (st & PRIME_BIT) != 0;
    }

    private static int unprime(int st) {
        return st & ~PRIME_BIT;
    }

    private static int primePresent() {
        return ST_PRESENT | PRIME_BIT;
    }

    private static int primeTomb() {
        return ST_TOMB | PRIME_BIT;
    }

    private static int reprobe_limit(int len) {
        return REPROBE_LIMIT + (len >> 4);
    }

    private static ObjectIntPair<long[]> toArraySequentialScanPair(final CHS chs, final boolean includeZero, long[] out) {
        int size = 0;
        if (out == null) out = new long[0];

        if (includeZero) {
            if (size == out.length) out = Arrays.copyOf(out, Math.max(1, size << 1));
            out[size++] = NO_KEY;
        }

        final long[] tab = chs._table;
        final int end = (chs.capacity() << 1);

        for (int i = 0; i < end; i += 2) {
            final long k = tab[i];
            if (k == NO_KEY) continue;

            int st = (int) tab[i + 1];
            if ((st & PRIME_BIT) != 0) st &= ~PRIME_BIT;

            if (st == ST_PRESENT) {
                if (size == out.length) out = Arrays.copyOf(out, Math.max(1, size << 1));
                out[size++] = k;
            }
        }

        return new ObjectIntPair<>(out, size);
    }

    private boolean CAS_chs(final CHS old, final CHS nnn) {
        return U.compareAndSetReference(this, _chs_offset, old, nnn);
    }

    private boolean CAS_zero(final int old, final int nnn) {
        return U.compareAndSetInt(this, _zero_offset, old, nnn);
    }

    private void initialize(final int initialCapacity) {
        RangeUtil.checkPositiveOrZero(initialCapacity, "initialCapacity");
        int log;
        for (log = MIN_SIZE_LOG; (1 << log) < initialCapacity; log++) { /*empty*/ }
        _chs = new CHS(this, new ConcurrentAutoTable(), log);
        _zeroState = ST_TOMB;
        _last_resize_milli = System.currentTimeMillis();
    }

    private CHS stableTop() {
        CHS top;
        while (true) {
            top = _chs;
            if (top._newchs == null) return top;
            top.help_copy_impl(true);
        }
    }

    @Override
    public int size() {
        return (_zeroState == ST_PRESENT ? 1 : 0) + _chs.size();
    }

    @Override
    public boolean isEmpty() {
        return (_zeroState != ST_PRESENT) && _chs.size() == 0;
    }

    @Override
    public boolean contains(long k) {
        if (k == NO_KEY) return _zeroState == ST_PRESENT;
        return _chs.get_impl(k);
    }

    @Override
    public boolean add(long k) {
        if (k == NO_KEY) {
            int z;
            while ((z = _zeroState) != ST_PRESENT) {
                if (CAS_zero(z, ST_PRESENT)) return true;
                Library.onSpinWait();
            }
            return false;
        }
        int old = _chs.putIfMatch(k, ST_PRESENT, EXP_ABSENT);
        return old != ST_PRESENT;
    }

    @Override
    public boolean remove(long k) {
        if (k == NO_KEY) {
            while (_zeroState == ST_PRESENT) {
                if (CAS_zero(ST_PRESENT, ST_TOMB)) return true;
                Library.onSpinWait();
            }
            return false;
        }
        return _chs.putIfMatch(k, ST_TOMB, EXP_PRESENT) == ST_PRESENT;
    }

    @Override
    public void clear() {
        CHS newchs = new CHS(this, new ConcurrentAutoTable(), MIN_SIZE_LOG);
        while (!CAS_chs(_chs, newchs)) {
            Library.onSpinWait();
        }
        int z;
        while ((z = _zeroState) != ST_TOMB) {
            if (CAS_zero(z, ST_TOMB)) break;
        }
    }

    public void clear(boolean large) {
        _chs.clear();
        int z;
        while ((z = _zeroState) != ST_TOMB) {
            if (CAS_zero(z, ST_TOMB)) break;
            Library.onSpinWait();
        }
    }

    public void help_copy() {
        CHS top = _chs;
        if (top._newchs == null) return;
        top.help_copy_impl(false);
    }

    @Override
    public LongIterator iterator() {
        return new Itr();
    }

    public ObjectIntPair<long[]> toArrayParallel(long[] out) {
        final ForkJoinPool p = ForkJoinTask.inForkJoinPool() ? ForkJoinTask.getPool() : ForkJoinPool.commonPool();
        return toArrayParallel(out, p);
    }

    public ObjectIntPair<long[]> toArrayParallel(long[] out, ForkJoinPool pool) {
        if (pool == null) pool = ForkJoinPool.commonPool();

        // snapshot refs once
        final CHS chs = _chs;
        final int cap = chs.capacity();
        final boolean includeZero = (_zeroState == ST_PRESENT);

        if (cap <= SNAPSHOT_LEAF_SLOTS || pool.getParallelism() <= 1) {
            return toArraySequentialScanPair(chs, includeZero, out);
        }

        final SnapshotTask task = new SnapshotTask(chs, 0, cap);
        final SnapshotBlocks blocks;
        if (ForkJoinTask.inForkJoinPool() && ForkJoinTask.getPool() == pool) {
            blocks = task.invoke();
        } else {
            blocks = pool.invoke(task);
        }

        final int size = blocks.count + (includeZero ? 1 : 0);

        long[] dst = out;
        if (dst == null || dst.length < size) dst = new long[size];

        int p = 0;
        if (includeZero) dst[p++] = NO_KEY;
        blocks.copyTo(dst, p);

        return new ObjectIntPair<>(dst, size);
    }

    public ObjectIntPair<long[]> toArrayParallel(long[] out, ForkJoinPool pool, LongConsumer onPresent) {
        if (pool == null) pool = ForkJoinPool.commonPool();

        // snapshot refs once
        final CHS chs = _chs;
        final int cap = chs.capacity();
        final boolean includeZero = (_zeroState == ST_PRESENT);

        if (cap <= SNAPSHOT_LEAF_SLOTS || pool.getParallelism() <= 1) {
            // sequential scan + callback
            int size = 0;
            long[] dst = (out == null) ? new long[0] : out;

            if (includeZero) {
                if (size == dst.length) dst = Arrays.copyOf(dst, 1);
                dst[size++] = NO_KEY;
                if (onPresent != null) onPresent.accept(NO_KEY);
            }

            final long[] tab = chs._table;
            final int end = (cap << 1);
            for (int i = 0; i < end; i += 2) {
                final long k = tab[i];
                if (k == NO_KEY) continue;

                int st = (int) tab[i + 1];
                if ((st & PRIME_BIT) != 0) st &= ~PRIME_BIT;
                if (st != ST_PRESENT) continue;

                if (size == dst.length) dst = Arrays.copyOf(dst, Math.max(1, size << 1));
                dst[size++] = k;
                if (onPresent != null) onPresent.accept(k);
            }

            return new ObjectIntPair<>(dst, size);
        }

        final SnapshotTask task = new SnapshotTask(chs, 0, cap, onPresent);
        final SnapshotBlocks blocks;
        if (ForkJoinTask.inForkJoinPool() && ForkJoinTask.getPool() == pool) {
            blocks = task.invoke();
        } else {
            blocks = pool.invoke(task);
        }

        final int size = blocks.count + (includeZero ? 1 : 0);

        long[] dst = out;
        if (dst == null || dst.length < size) dst = new long[size];

        int p = 0;
        if (includeZero) {
            dst[p++] = NO_KEY;
            if (onPresent != null) onPresent.accept(NO_KEY);
        }
        blocks.copyTo(dst, p);

        return new ObjectIntPair<>(dst, size);
    }


    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        final CHS snap = stableTop();
        if (_zeroState == ST_PRESENT) {
            s.writeLong(NO_KEY);
            s.writeBoolean(true);
        }
        final int cap = snap.capacity();
        for (int i = 0; i < cap; i++) {
            long k = snap.keyPlain(i);
            if (k == NO_KEY) continue;
            if (contains(k)) {
                s.writeLong(k);
                s.writeBoolean(true);
            }
        }

        s.writeLong(NO_KEY);
        s.writeBoolean(false);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        initialize(MIN_SIZE);
        while (true) {
            long k = s.readLong();
            boolean more = s.readBoolean();
            if (!more) break;
            add(k);
        }
    }

    @Override
    public NonBlockingHashSetLong clone() {
        NonBlockingHashSetLong c;
        try {
            c = (NonBlockingHashSetLong) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        final CHS snap = stableTop();
        c.initialize(this.size());

        if (this._zeroState == ST_PRESENT) c.add(NO_KEY);

        final int cap = snap.capacity();
        for (int i = 0; i < cap; i++) {
            long k = snap.keyPlain(i);
            if (k == NO_KEY) continue;
            if (this.contains(k)) c.add(k);
        }

        return c;
    }

    private static final class SnapshotTask extends RecursiveTask<SnapshotBlocks> {
        private final CHS chs;
        private final int lo, hi;
        private final LongConsumer onPresent;

        SnapshotTask(final CHS chs, final int lo, final int hi) {
            this.chs = chs;
            this.lo = lo;
            this.hi = hi;
            this.onPresent = null;
        }

        SnapshotTask(final CHS chs, final int lo, final int hi, final LongConsumer onPresent) {
            this.chs = chs;
            this.lo = lo;
            this.hi = hi;
            this.onPresent = onPresent;
        }

        @Override
        protected SnapshotBlocks compute() {
            final int len = hi - lo;
            if (len <= SNAPSHOT_LEAF_SLOTS) {
                return SnapshotBlocks.scanRange(chs, lo, hi, onPresent);
            }

            final int mid = (lo + hi) >>> 1;
            final SnapshotTask left  = new SnapshotTask(chs, lo, mid, onPresent);
            final SnapshotTask right = new SnapshotTask(chs, mid, hi, onPresent);

            left.fork();
            final SnapshotBlocks r = right.compute();
            final SnapshotBlocks l = left.join();

            return SnapshotBlocks.concat(l, r);
        }
    }


    private static final class SnapshotBlocks {
        Block head;
        Block tail;
        long[] cur;
        int curPos;
        int count;

        static SnapshotBlocks scanRange(final CHS chs, final int lo, final int hi, final LongConsumer onPresent) {
            final SnapshotBlocks sb = new SnapshotBlocks();
            final long[] tab = chs._table;

            int i = lo << 1;
            final int end = hi << 1;

            for (; i < end; i += 2) {
                final long k = tab[i];
                if (k == NO_KEY) continue;

                int st = (int) tab[i + 1];
                if ((st & PRIME_BIT) != 0) st &= ~PRIME_BIT;
                if (st != ST_PRESENT) continue;

                sb.add(k);
                if (onPresent != null) onPresent.accept(k);
            }

            sb.finish();
            return sb;
        }


        static SnapshotBlocks concat(final SnapshotBlocks a, final SnapshotBlocks b) {
            if (a.head == null) return b;
            if (b.head == null) return a;
            a.tail.next = b.head;
            a.tail = b.tail;
            a.count += b.count;
            return a;
        }

        private void add(final long v) {
            if (cur == null) {
                final Block b = new Block(SNAPSHOT_BLOCK_SIZE);
                head = tail = b;
                cur = b.a;
                curPos = 0;
            } else if (curPos == cur.length) {
                tail.len = curPos;
                final Block b = new Block(SNAPSHOT_BLOCK_SIZE);
                tail.next = b;
                tail = b;
                cur = b.a;
                curPos = 0;
            }

            cur[curPos++] = v;
            count++;
        }

        private void finish() {
            if (tail != null) tail.len = curPos;
        }

        void copyTo(final long[] dst, int p) {
            for (Block b = head; b != null; b = b.next) {
                System.arraycopy(b.a, 0, dst, p, b.len);
                p += b.len;
            }
        }

        private static final class Block {
            final long[] a;
            int len;
            Block next;

            Block(final int cap) {
                this.a = new long[cap];
            }
        }
    }

    private static final class CHS implements Serializable {
        private static final long serialVersionUID = 1L;

        private static final long _newchs_offset = fieldOffset(CHS.class, "_newchs");
        private static final long _resizers_offset = fieldOffset(CHS.class, "_resizers");
        private static final long _copyIdx_offset = fieldOffset(CHS.class, "_copyIdx");
        private static final long _copyDone_offset = fieldOffset(CHS.class, "_copyDone");

        final NonBlockingHashSetLong _set;

        final long[] _table;

        volatile CHS _newchs;
        volatile long _resizers;
        volatile long _copyIdx;
        volatile long _copyDone;

        private transient ConcurrentAutoTable _size;
        private transient ConcurrentAutoTable _slots;

        CHS(final NonBlockingHashSetLong set, final ConcurrentAutoTable size, final int logsize) {
            _set = set;
            _size = size;
            _slots = new ConcurrentAutoTable();
            _table = new long[1 << (logsize + 1)];
        }

        private static boolean matches(final int exp, final int cur) {
            return switch (exp) {
                case EXP_ANY -> true;
                case EXP_PRESENT -> cur == ST_PRESENT;
                case EXP_ABSENT -> cur == ST_EMPTY || cur == ST_TOMB;
                case EXP_EMPTY_COPY -> cur == ST_EMPTY;
                default -> cur == exp;
            };
        }

        private static int keyPhys(final int idx) {
            return idx << 1;
        }

        private static int statePhys(final int idx) {
            return (idx << 1) + 1;
        }

        int size() {
            return (int) _size.get();
        }

        int capacity() {
            return _table.length >> 1;
        }

        void clear() {
            _size = new ConcurrentAutoTable();
            _slots = new ConcurrentAutoTable();
            Arrays.fill(_table, 0L);
            _newchs = null;
            _resizers = 0;
            _copyIdx = 0;
            _copyDone = 0;
        }

        long keyPlain(final int idx) {
            return _table[keyPhys(idx)];
        }

        int statePlain(final int idx) {
            return (int) _table[statePhys(idx)];
        }

        long keyVolatile(final int idx) {
            return U.getLongVolatile(_table, offLong(keyPhys(idx)));
        }

        int stateVolatile(final int idx) {
            return (int) U.getLongVolatile(_table, offLong(statePhys(idx)));
        }

        private boolean CAS_key(final int idx, final long old, final long key) {
            return U.compareAndSetLong(_table, offLong(keyPhys(idx)), old, key);
        }

        private boolean CAS_state(final int idx, final int old, final int st) {
            return U.compareAndSetLong(_table, offLong(statePhys(idx)), st32(old), st32(st));
        }

        private boolean CAS_newchs(final CHS n) {
            return U.compareAndSetReference(this, _newchs_offset, null, n);
        }

        private boolean CAS_resizers(final long old, final long nnn) {
            return U.compareAndSetLong(this, _resizers_offset, old, nnn);
        }

        private boolean CAS_copyIdx(final long old, final long nnn) {
            return U.compareAndSetLong(this, _copyIdx_offset, old, nnn);
        }

        private boolean CAS_copyDone(final long old, final long nnn) {
            return U.compareAndSetLong(this, _copyDone_offset, old, nnn);
        }

        boolean get_impl(final long key) {
            final int h = _set.hash(key);
            final int cap = capacity();
            final int mask = cap - 1;
            int idx = (h & mask);

            int reprobe = 0;
            while (true) {
                final long K = keyVolatile(idx);

                if (K == NO_KEY) {
                    final CHS n = _newchs;
                    return n != null && n.get_impl(key);
                }

                if (K == key) {
                    final int st = stateVolatile(idx);
                    if (isPrime(st)) {
                        return copy_slot_and_check(idx, true).get_impl(key);
                    }
                    return unprime(st) == ST_PRESENT;
                }

                if (++reprobe >= reprobe_limit(cap)) {
                    return _newchs != null && copy_slot_and_check(idx, true).get_impl(key);
                }

                idx = (idx + 1) & mask;
            }
        }

        private boolean tableFull(int reprobeCnt, int cap) {
            return reprobeCnt >= REPROBE_LIMIT && (reprobeCnt >= reprobe_limit(cap) || _slots.estimate_get() >= (cap >> 1));
        }

        int putIfMatch(final long key, final int putState, final int exp) {
            final int h = _set.hash(key);
            final int cap = capacity();
            final int mask = cap - 1;
            int idx = (h & mask);

            int reprobe = 0;
            long K;

            while (true) {
                K = keyVolatile(idx);

                if (K == NO_KEY) {
                    final CHS n = _newchs;
                    if (n != null) {
                        if (exp != EXP_EMPTY_COPY) _set.help_copy();
                        return n.putIfMatch(key, putState, exp);
                    }

                    if (putState == ST_TOMB) return ST_EMPTY;

                    if (CAS_key(idx, NO_KEY, key)) {
                        _slots.add(1);
                        break;
                    }
                    K = keyVolatile(idx);
                }

                if (K == key) break;

                if (++reprobe >= reprobe_limit(cap)) {
                    final CHS n = resize();
                    if (exp != EXP_EMPTY_COPY) _set.help_copy();
                    return n.putIfMatch(key, putState, exp);
                }
                idx = (idx + 1) & mask;
            }

            while (true) {
                int st = stateVolatile(idx);
                int ust = unprime(st);

                if (!isPrime(st) && putState == ust) return ust;

                if ((ust == ST_EMPTY && tableFull(reprobe, cap)) || isPrime(st)) {
                    resize();
                    return copy_slot_and_check(idx, exp != EXP_EMPTY_COPY).putIfMatch(key, putState, exp);
                }

                if (!matches(exp, ust)) return ust;

                if (CAS_state(idx, st, putState)) {
                    if (exp != EXP_EMPTY_COPY) {
                        if ((ust == ST_EMPTY || ust == ST_TOMB) && putState == ST_PRESENT) _size.add(1);
                        else if (ust == ST_PRESENT && putState != ST_PRESENT) _size.add(-1);
                    }
                    return ust;
                }

                st = stateVolatile(idx);
                if (isPrime(st)) {
                    return copy_slot_and_check(idx, exp != EXP_EMPTY_COPY).putIfMatch(key, putState, exp);
                }
            }
        }

        private CHS resize() {
            CHS n = _newchs;
            if (n != null) return n;

            final int oldCap = capacity();
            final int sz = size();
            int newCap = sz;

            if (_set._opt_for_space) {
                if (sz >= (oldCap >> 1)) newCap = oldCap << 1;
            } else {
                if (sz >= (oldCap >> 2)) {
                    newCap = oldCap << 1;
                    if (sz >= (oldCap >> 1)) newCap = oldCap << 2;
                }
            }

            long tm = System.currentTimeMillis();
            if (newCap <= oldCap && tm <= _set._last_resize_milli + 10_000L) newCap = oldCap << 1;
            if (newCap < oldCap) newCap = oldCap;

            int log2;
            for (log2 = MIN_SIZE_LOG; (1 << log2) < newCap; log2++) { /*empty*/ }

            long capL = 1L << log2;
            if ((capL << 1) > Integer.MAX_VALUE) {
                log2 = 29;
                capL = 1L << log2;
                if (sz > (capL >> 1)) throw new RuntimeException("Table is full.");
            }

            long r = _resizers;
            while (!CAS_resizers(r, r + 1)) r = _resizers;

            long megs = (((capL << 1) + 8) << 3) >> 20;
            if (r >= 2 && megs > 0) {
                n = _newchs;
                if (n != null) return n;
                try {
                    Thread.sleep(megs);
                } catch (Exception ignored) { /*empty*/ }
            }

            n = _newchs;
            if (n != null) return n;

            n = new CHS(_set, _size, log2);
            if (_newchs != null) return _newchs;
            if (!CAS_newchs(n)) n = _newchs;
            return n;
        }

        private void help_copy_impl(final boolean copyAll) {
            final CHS n = _newchs;
            assert n != null;

            final int oldCap = capacity();
            final int MIN_COPY_WORK = Math.min(oldCap, 1024);

            final long mask = ((long) oldCap) - 1L;
            final long panicBound = ((long) oldCap) << 1;

            boolean panic = false;
            long copyidx = 0L;

            while (_copyDone < (long) oldCap) {
                if (!panic) {
                    long start = _copyIdx;
                    while (start < panicBound && !CAS_copyIdx(start, start + MIN_COPY_WORK)) {
                        start = _copyIdx;
                    }
                    if (start >= panicBound) {
                        panic = true;
                        copyidx = start;
                    } else {
                        copyidx = start;
                    }
                }

                int workdone = 0;
                for (int i = 0; i < MIN_COPY_WORK; i++) {
                    final int slot = (int) ((copyidx + (long) i) & mask);
                    if (copy_slot(slot)) workdone++;
                }
                if (workdone > 0) copy_check_and_promote(workdone);

                copyidx += MIN_COPY_WORK;

                if (!copyAll && !panic) return;
            }

            copy_check_and_promote(0);
        }

        private CHS copy_slot_and_check(final int idx, final boolean shouldHelp) {
            assert _newchs != null;
            if (copy_slot(idx)) copy_check_and_promote(1);
            if (shouldHelp) _set.help_copy();
            return _newchs;
        }

        private void copy_check_and_promote(final int workdone) {
            final int oldCap = capacity();
            long copyDone = _copyDone;
            long nowDone = copyDone + (long) workdone;

            if (workdone > 0) {
                while (!CAS_copyDone(copyDone, nowDone)) {
                    copyDone = _copyDone;
                    nowDone = copyDone + (long) workdone;
                }
            }

            if (nowDone == (long) oldCap && _set._chs == this && _set.CAS_chs(this, _newchs)) {
                _set._last_resize_milli = System.currentTimeMillis();
            }
        }

        private boolean copy_slot(final int idx) {
            final CHS n = _newchs;
            assert n != null;

            final int tomb = primeTomb();

            final long key = keyVolatile(idx);
            if (key == NO_KEY) {
                int st = stateVolatile(idx);
                while (true) {
                    if (st == tomb) return false;
                    if (CAS_state(idx, st, tomb)) return true;
                    st = stateVolatile(idx);
                }
            }

            int st = stateVolatile(idx);
            while (true) {
                if (st == tomb) return false;
                if (isPrime(st)) break;

                final int ust = unprime(st);
                final int pst = (ust == ST_PRESENT) ? primePresent() : tomb;

                if (CAS_state(idx, st, pst)) {
                    st = pst;
                    if (pst == tomb) return true;
                    break;
                }
                st = stateVolatile(idx);
            }

            if (unprime(st) == ST_PRESENT) {
                n.putIfMatch(key, ST_PRESENT, EXP_EMPTY_COPY);
            }
            int cur = st;
            while (cur != tomb) {
                if (CAS_state(idx, cur, tomb)) return true;
                cur = stateVolatile(idx);
            }
            return false;
        }
    }

    // weakly consistent
    private final class Itr implements LongIterator {
        private final CHS chs;
        private final int cap;
        private int idx;

        private boolean zeroPending;

        private long next;
        private boolean hasNext;

        private long lastReturned;
        private boolean canRemove;

        Itr() {
            this.chs = _chs;
            this.cap = chs.capacity();
            this.idx = 0;
            this.zeroPending = (_zeroState == ST_PRESENT);

            this.lastReturned = 0L;
            this.canRemove = false;
            advance();
        }

        private void advance() {
            if (zeroPending) {
                zeroPending = false;
                next = NO_KEY;
                hasNext = true;
                return;
            }

            while (idx < cap) {
                final int i = idx++;
                final long k = chs.keyPlain(i);
                if (k == NO_KEY) continue;

                int st = chs.statePlain(i);
                if (isPrime(st)) st = unprime(st);

                if (st == ST_PRESENT) {
                    next = k;
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Long next() {
            return nextLong();
        }

        @Override
        public long nextLong() {
            if (!hasNext) throw new NoSuchElementException();
            final long r = next;
            lastReturned = r;
            canRemove = true;
            advance();
            return r;
        }

        @Override
        public void remove() {
            if (!canRemove) throw new IllegalStateException();
            NonBlockingHashSetLong.this.remove(lastReturned);
            canRemove = false;
        }

        @Override
        public int skip(int n) {
            int i = 0;
            while (i < n && hasNext) {
                nextLong();
                i++;
            }
            return i;
        }
    }
}

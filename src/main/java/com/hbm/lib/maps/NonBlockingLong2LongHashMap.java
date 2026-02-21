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
import com.hbm.lib.internal.UnsafeHolder;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

/**
 * This is a derivative work of {@link NonBlockingHashMapLong} by Cliff Click, licensed under Apache 2.0
 * [Long.MIN_VALUE, Long.MIN_VALUE + 3] are reserved for internal use, users must not store them as values.
 * TODO: Find a way to allow all long as values
 */
public class NonBlockingLong2LongHashMap extends AbstractLong2LongMap implements Long2LongMap, ConcurrentMap<Long, Long>, Cloneable {

    private static final int REPROBE_LIMIT = 10;
    private static final long VALUE_XOR = 0x8000_0000_0000_0000L;
    private static final long VNULL = 0L, VTOMB = 1L, VPRIME = 2L, VTOMBPRIME = 3L;
    private static final long _Lbase = U.arrayBaseOffset(long[].class);
    private static final int _Lscale = U.arrayIndexScale(long[].class);
    private static final long _chm_offset = fieldOffset(NonBlockingLong2LongHashMap.class, "_chm");
    private static final long _val_1_offset = fieldOffset(NonBlockingLong2LongHashMap.class, "_val_1");
    private static final int MIN_SIZE_LOG = 4;
    private static final int MIN_SIZE = (1 << MIN_SIZE_LOG);
    private static final long NO_KEY = 0L;
    private static final int EXP_NO_MATCH = 0, EXP_MATCH_ANY = 1, EXP_MATCH_ABSENT = 2, EXP_MATCH_EXACT = 3;
    private final boolean _opt_for_space;
    protected transient volatile CHM _chm;
    protected transient volatile long _val_1;
    protected transient long _last_resize_milli;
    protected transient ConcurrentAutoTable _reprobes = new ConcurrentAutoTable();
    protected transient ObjectSet<Long2LongMap.Entry> entries;
    protected transient LongSet keys;
    protected transient LongCollection values;

    public NonBlockingLong2LongHashMap() {
        this(MIN_SIZE, true);
    }

    public NonBlockingLong2LongHashMap(final int initial_sz) {
        this(initial_sz, true);
    }

    public NonBlockingLong2LongHashMap(final boolean opt_for_space) {
        this(1, opt_for_space);
    }

    public NonBlockingLong2LongHashMap(final int initial_sz, final boolean opt_for_space) {
        _opt_for_space = opt_for_space;
        initialize(initial_sz);
    }

    protected int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static boolean isDataEnc(long enc) {
        return (enc & ~VTOMBPRIME) != 0; // anything except 0,1,2,3
    }

    private static boolean isPrimeEnc(long enc) {
        return enc == VPRIME || enc == VTOMBPRIME;
    }

    private static long encodeValue(final long v) {
        final long enc = v ^ VALUE_XOR;
        if (!isDataEnc(enc)) {
            throw new UnsupportedOperationException("Value is reserved for internal sentinels: " + v);
        }
        return enc;
    }

    private static long decodeValue(final long enc) {
        return enc ^ VALUE_XOR;
    }

    private static long rawIndex(final long[] ary, final int idx) {
        assert idx >= 0 && idx < ary.length;
        return _Lbase + ((long) idx * _Lscale);
    }

    private static int reprobe_limit(int len) {
        return REPROBE_LIMIT + (len >> 4);
    }

    private static boolean matches(final long curEnc, final int expKind, final long expEnc) {
        return switch (expKind) {
            case EXP_NO_MATCH -> true;
            case EXP_MATCH_ANY -> isDataEnc(curEnc);
            case EXP_MATCH_ABSENT -> curEnc == VNULL || curEnc == VTOMB;
            case EXP_MATCH_EXACT -> curEnc == expEnc;
            default -> throw new AssertionError("bad expKind: " + expKind);
        };
    }

    private boolean CAS_ref(final long offset, final Object old, final Object nnn) {
        return U.compareAndSetReference(this, offset, old, nnn);
    }

    private boolean CAS_long(final long offset, final long old, final long nnn) {
        return U.compareAndSetLong(this, offset, old, nnn);
    }

    public long reprobes() {
        long r = _reprobes.get();
        _reprobes = new ConcurrentAutoTable();
        return r;
    }

    private void initialize(final int initial_sz) {
        RangeUtil.checkPositiveOrZero(initial_sz, "initial_sz");
        int i = MIN_SIZE_LOG;
        while ((1 << i) < initial_sz) {
            i++;
        }
        _chm = new CHM(this, new ConcurrentAutoTable(), i);
        _val_1 = VTOMB; // key0 absent
        _last_resize_milli = System.currentTimeMillis();
    }

    @Override
    public int size() {
        return (isDataEnc(_val_1) ? 1 : 0) + _chm.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(long key) {
        return getEncoded(key) != VNULL;
    }

    @Override
    public boolean containsValue(long value) {
        final long enc;
        try {
            enc = encodeValue(value);
        } catch (IllegalArgumentException disallowed) {
            return false;
        }
        if (_val_1 == enc) return true;
        for (long v : values()) {
            if (v == value) return true;
        }
        return false;
    }

    private long getEncoded(final long key) {
        if (key == NO_KEY) {
            final long v = _val_1;
            return isDataEnc(v) ? v : VNULL;
        }
        final long enc = _chm.get_impl(key);
        return isDataEnc(enc) ? enc : VNULL;
    }

    @Override
    public long get(final long key) {
        final long enc = getEncoded(key);
        return (enc == VNULL) ? defaultReturnValue() : decodeValue(enc);
    }

    public long getOrDefault(final long key, final long defaultValue) {
        final long enc = getEncoded(key);
        return (enc == VNULL) ? defaultValue : decodeValue(enc);
    }

    private long putIfMatch0(final long putEnc, final int expKind, final long expEnc) {
        while (true) {
            final long cur = _val_1;
            if (!matches(cur, expKind, expEnc)) return cur;
            if (CAS_long(_val_1_offset, cur, putEnc)) return cur;
        }
    }

    private long putIfMatch(final long key, final long putEnc, final int expKind, final long expEnc) {
        if (key == NO_KEY) {
            return putIfMatch0(putEnc, expKind, expEnc);
        }
        return _chm.putIfMatch(key, putEnc, expKind, expEnc, true);
    }

    @Override
    public long put(final long key, final long value) {
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_NO_MATCH, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : defaultReturnValue();
    }

    public long putIfAbsent(final long key, final long value) {
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_ABSENT, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : defaultReturnValue();
    }

    @Override
    public long remove(final long key) {
        final long oldEnc = putIfMatch(key, VTOMB, EXP_NO_MATCH, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : defaultReturnValue();
    }

    public boolean remove(final long key, final long value) {
        final long expEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, VTOMB, EXP_MATCH_EXACT, expEnc);
        return oldEnc == expEnc;
    }

    public long replace(final long key, final long value) {
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_ANY, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : defaultReturnValue();
    }

    public boolean replace(final long key, final long oldValue, final long newValue) {
        final long expEnc = encodeValue(oldValue);
        final long putEnc = encodeValue(newValue);
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_EXACT, expEnc);
        return oldEnc == expEnc;
    }

    @Override
    public void clear() {
        final CHM newchm = new CHM(this, new ConcurrentAutoTable(), MIN_SIZE_LOG);
        while (!CAS_ref(_chm_offset, _chm, newchm)) {
            Library.onSpinWait();
        }
        _val_1 = VTOMB;
    }

    public void clear(boolean large) {
        _chm.clear();
        _val_1 = VTOMB;
    }

    private void help_copy() {
        CHM topchm = _chm;
        if (topchm._newchm == null) return;
        topchm.help_copy_impl(false);
    }

    @Override
    public LongSet keySet() {
        if (keys == null) keys = new KeySet();
        return keys;
    }

    @Override
    public LongCollection values() {
        if (values == null) values = new AbstractLongCollection() {
            @Override
            public void clear() {
                NonBlockingLong2LongHashMap.this.clear();
            }

            @Override
            public int size() {
                return NonBlockingLong2LongHashMap.this.size();
            }

            @Override
            public boolean contains(long v) {
                return NonBlockingLong2LongHashMap.this.containsValue(v);
            }

            @Override
            public LongIterator iterator() {
                return new SnapshotV();
            }
        };
        return values;
    }

    @Override
    public ObjectSet<Long2LongMap.Entry> long2LongEntrySet() {
        if (entries == null) entries = new EntrySet();
        return entries;
    }

    public void forEachFast(final LongLongConsumer action) {
        if (action == null) throw new NullPointerException();

        final long v0 = _val_1;
        if (isDataEnc(v0)) action.accept(NO_KEY, decodeValue(v0));

        CHM top = _chm;
        if (top._newchm != null) {
            top.help_copy_impl(true);
            top = _chm;
        }

        final long[] keys = top._keys;
        final long[] vals = top._vals;

        for (int i = 0, len = keys.length; i < len; i++) {
            final long k = keys[i];
            if (k == NO_KEY) continue;

            final long enc = U.getLongVolatile(vals, rawIndex(vals, i));
            if (!isDataEnc(enc)) {
                if (isPrimeEnc(enc)) {
                    final long got = getEncoded(k);
                    if (got != VNULL) action.accept(k, decodeValue(got));
                }
                continue;
            }
            action.accept(k, decodeValue(enc));
        }
    }

    public void replaceAll(final LongLongBiFunction function) {
        if (function == null) throw new NullPointerException();

        while (true) {
            final long cur = _val_1;
            if (!isDataEnc(cur)) break;
            final long newV = function.apply(NO_KEY, decodeValue(cur));
            final long putEnc = encodeValue(newV);
            if (CAS_long(_val_1_offset, cur, putEnc)) break;
        }

        CHM top;
        while (true) {
            top = _chm;
            if (top._newchm == null) break;
            top.help_copy_impl(true);
        }

        for (int i = 0; i < top._keys.length; i++) {
            final long k = top._keys[i];
            if (k == NO_KEY) continue;

            while (true) {
                final long oldEnc = getEncoded(k);
                if (oldEnc == VNULL) break;
                final long oldV = decodeValue(oldEnc);
                final long newV = function.apply(k, oldV);
                if (replace(k, oldV, newV)) break;
            }
        }
    }

    public long computeIfAbsent(final long key, final LongUnaryOperator mappingFunction) {
        if (mappingFunction == null) throw new NullPointerException();

        final long gotEnc = getEncoded(key);
        if (gotEnc != VNULL) return decodeValue(gotEnc);

        final long newV = mappingFunction.applyAsLong(key);
        final long putEnc = encodeValue(newV);

        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_ABSENT, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : newV;
    }

    public long merge(final long key, final long value, final LongBinaryOperator remappingFunction) {
        if (remappingFunction == null) throw new NullPointerException();
        final long valueEnc = encodeValue(value);

        while (true) {
            final long oldEnc = getEncoded(key);
            if (oldEnc == VNULL) {
                final long priorEnc = putIfMatch(key, valueEnc, EXP_MATCH_ABSENT, 0L);
                if (!isDataEnc(priorEnc)) return value; // inserted
                continue; // raced with insert, retry
            }

            final long oldV = decodeValue(oldEnc);
            final long newV = remappingFunction.applyAsLong(oldV, value);
            final long newEnc = encodeValue(newV);

            final long priorEnc = putIfMatch(key, newEnc, EXP_MATCH_EXACT, oldEnc);
            if (priorEnc == oldEnc) return newV;
        }
    }

    @Deprecated
    @Override
    public Long get(final Object key) {
        if (!(key instanceof Long)) return null;
        final long k = (Long) key;
        final long enc = getEncoded(k);
        return (enc == VNULL) ? null : decodeValue(enc);
    }

    @Deprecated
    @Override
    public Long put(final Long key, final Long value) {
        if (key == null || value == null) throw new NullPointerException();
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_NO_MATCH, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : null;
    }

    @Deprecated
    @Override
    public Long remove(final Object key) {
        if (!(key instanceof Long)) return null;
        final long k = (Long) key;
        final long oldEnc = putIfMatch(k, VTOMB, EXP_MATCH_ANY, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : null;
    }

    @Deprecated
    @Override
    public boolean remove(final Object key, final Object value) {
        if (!(key instanceof Long) || !(value instanceof Long)) return false;
        final long k = (Long) key;
        final long v = (Long) value;
        final long expEnc;
        try {
            expEnc = encodeValue(v);
        } catch (IllegalArgumentException disallowed) {
            return false;
        }
        final long oldEnc = putIfMatch(k, VTOMB, EXP_MATCH_EXACT, expEnc);
        return oldEnc == expEnc;
    }

    @Deprecated
    @Override
    public Long putIfAbsent(final Long key, final Long value) {
        if (key == null || value == null) throw new NullPointerException();
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_ABSENT, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : null;
    }

    @Deprecated
    @Override
    public Long replace(final Long key, final Long value) {
        if (key == null || value == null) throw new NullPointerException();
        final long putEnc = encodeValue(value);
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_ANY, 0L);
        return isDataEnc(oldEnc) ? decodeValue(oldEnc) : null;
    }

    @Deprecated
    @Override
    public boolean replace(final Long key, final Long oldValue, final Long newValue) {
        if (key == null || oldValue == null || newValue == null) throw new NullPointerException();
        final long expEnc, putEnc;
        try {
            expEnc = encodeValue(oldValue);
            putEnc = encodeValue(newValue);
        } catch (IllegalArgumentException disallowed) {
            return false;
        }
        final long oldEnc = putIfMatch(key, putEnc, EXP_MATCH_EXACT, expEnc);
        return oldEnc == expEnc;
    }

    @Deprecated
    @Override
    public Long getOrDefault(final Object key, final Long defaultValue) {
        if (!(key instanceof Long)) return defaultValue;
        final long k = (Long) key;
        final long enc = getEncoded(k);
        return (enc == VNULL) ? defaultValue : decodeValue(enc);
    }

    @Deprecated
    @Override
    public void forEach(final BiConsumer<? super Long, ? super Long> action) {
        if (action == null) throw new NullPointerException();
        forEachFast((k, v) -> action.accept(k, v));
    }

    @Deprecated
    @Override
    public void replaceAll(final BiFunction<? super Long, ? super Long, ? extends Long> function) {
        if (function == null) throw new NullPointerException();
        replaceAll((LongLongBiFunction) (k, v) -> {
            final Long nv = function.apply(k, v);
            if (nv == null) throw new NullPointerException();
            return nv;
        });
    }

    @Override
    public NonBlockingLong2LongHashMap clone() {
        NonBlockingLong2LongHashMap t;
        try {
            t = (NonBlockingLong2LongHashMap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        t.clear();
        for (LongIterator it = keySet().iterator(); it.hasNext(); ) {
            final long k = it.nextLong();
            if (containsKey(k)) t.put(k, get(k));
        }
        return t;
    }

    @FunctionalInterface
    public interface LongLongConsumer {
        void accept(long k, long v);
    }

    @FunctionalInterface
    public interface LongLongBiFunction {
        long apply(long k, long v);
    }

    protected static final class CHM implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final long NEW_CHM_OFF = UnsafeHolder.fieldOffset(CHM.class, "_newchm");
        private static final long RESIZERS_OFF = UnsafeHolder.fieldOffset(CHM.class, "_resizers");
        private static final long COPY_IDX_OFF = UnsafeHolder.fieldOffset(CHM.class, "_copyIdx");
        private static final long COPY_DONE_OFF = UnsafeHolder.fieldOffset(CHM.class, "_copyDone");
        final NonBlockingLong2LongHashMap _nbhmll;
        final long[] _keys;
        final long[] _vals;
        final long[] _primes;
        volatile CHM _newchm;
        @SuppressWarnings("unused")
        volatile long _resizers;
        volatile long _copyIdx = 0;
        volatile long _copyDone = 0;
        private ConcurrentAutoTable _size;
        private ConcurrentAutoTable _slots;

        CHM(final NonBlockingLong2LongHashMap nbhmll, final ConcurrentAutoTable size, final int logsize) {
            _nbhmll = nbhmll;
            _size = size;
            _slots = new ConcurrentAutoTable();
            _keys = new long[1 << logsize];
            _vals = new long[1 << logsize];
            _primes = new long[1 << logsize];
        }

        private static int reprobe_limit(int len) {
            return NonBlockingLong2LongHashMap.reprobe_limit(len);
        }

        public int size() {
            return (int) _size.get();
        }

        public int slots() {
            return (int) _slots.get();
        }

        boolean CAS_newchm(final CHM newchm) {
            return U.compareAndSetReference(this, NEW_CHM_OFF, null, newchm);
        }

        private boolean CAS_key(final int idx, final long old, final long key) {
            return U.compareAndSetLong(_keys, rawIndex(_keys, idx), old, key);
        }

        private boolean CAS_val(final int idx, final long old, final long val) {
            return U.compareAndSetLong(_vals, rawIndex(_vals, idx), old, val);
        }

        void clear() {
            _size = new ConcurrentAutoTable();
            _slots = new ConcurrentAutoTable();
            Arrays.fill(_keys, 0L);
            Arrays.fill(_vals, VNULL);
            Arrays.fill(_primes, 0L);
        }

        private long get_impl(final long key) {
            final int h = _nbhmll.hash(key);
            final int len = _keys.length;
            int idx = (h & (len - 1));

            int reprobe_cnt = 0;
            while (true) {
                final long K = U.getLongVolatile(_keys, rawIndex(_keys, idx));
                final long V = U.getLongVolatile(_vals, rawIndex(_vals, idx));

                if (K == NO_KEY) {
                    final CHM newchm = _newchm;
                    return newchm == null ? VNULL : copy_slot_and_check(idx, true).get_impl(key);
                }

                if (key == K) {
                    if (isPrimeEnc(V)) {
                        return copy_slot_and_check(idx, true).get_impl(key);
                    }
                    if (!isDataEnc(V)) return VNULL;

                    @SuppressWarnings("unused") final CHM newchm = _newchm; // volatile read before returning V
                    return V;
                }

                if (++reprobe_cnt >= reprobe_limit(len)) {
                    final CHM newchm = _newchm;
                    return newchm == null ? VNULL : copy_slot_and_check(idx, true).get_impl(key);
                }

                idx = (idx + 1) & (len - 1);
            }
        }

        private boolean tableFull(final int reprobe_cnt, final int len) {
            return reprobe_cnt >= REPROBE_LIMIT && (reprobe_cnt >= reprobe_limit(len) || _slots.estimate_get() >= (len >> 1));
        }

        private CHM resize() {
            CHM newchm = _newchm;
            if (newchm != null) return newchm;

            final int oldlen = _keys.length;
            final int sz = size();
            int newsz = sz;

            if (_nbhmll._opt_for_space) {
                if (sz >= (oldlen >> 1)) newsz = oldlen << 1;
            } else {
                if (sz >= (oldlen >> 2)) {
                    newsz = oldlen << 1;
                    if (sz >= (oldlen >> 1)) newsz = oldlen << 2;
                }
            }

            final long tm = System.currentTimeMillis();
            if (newsz <= oldlen && tm <= _nbhmll._last_resize_milli + 10_000L) {
                newsz = oldlen << 1;
            }
            if (newsz < oldlen) newsz = oldlen;

            int log2;
            for (log2 = MIN_SIZE_LOG; (1 << log2) < newsz; log2++) { /*empty*/ }

            long len = ((1L << log2) << 1) + 2;
            if ((int) len != len) {
                log2 = 30;
                len = (1L << log2) + 2;
                if (sz > ((len >> 2) + (len >> 1))) throw new RuntimeException("Table is full.");
            }

            long r = U.getAndAddLong(this, RESIZERS_OFF, 1L);

            long megs = ((((1L << log2) << 1) + 8) << 3) >> 20;
            if (r >= 2 && megs > 0) {
                newchm = _newchm;
                if (newchm != null) return newchm;
                try {
                    Thread.sleep(megs);
                } catch (Exception ignored) {
                }
            }

            newchm = _newchm;
            if (newchm != null) return newchm;

            newchm = new CHM(_nbhmll, _size, log2);

            if (_newchm != null) return _newchm;

            if (!CAS_newchm(newchm)) newchm = _newchm;
            return newchm;
        }

        private void help_copy_impl(final boolean copy_all) {
            final CHM newchm = _newchm;
            assert newchm != null;

            final int oldlen = _keys.length;
            final int MIN_COPY_WORK = Math.min(oldlen, 1024);

            int panic_start = -1;
            int copyidx = -9999;

            while (_copyDone < oldlen) {
                if (panic_start == -1) {
                    copyidx = (int) _copyIdx;
                    while (copyidx < (oldlen << 1) && !U.compareAndSetLong(this, COPY_IDX_OFF, copyidx, copyidx + MIN_COPY_WORK)) {
                        copyidx = (int) _copyIdx;
                    }
                    if (!(copyidx < (oldlen << 1))) panic_start = copyidx;
                }

                int workdone = 0;
                for (int i = 0; i < MIN_COPY_WORK; i++) {
                    if (copy_slot((copyidx + i) & (oldlen - 1))) workdone++;
                }
                if (workdone > 0) copy_check_and_promote(workdone);

                copyidx += MIN_COPY_WORK;
                if (!copy_all && panic_start == -1) return;
            }

            copy_check_and_promote(0);
        }

        private CHM copy_slot_and_check(final int idx, final boolean should_help) {
            assert _newchm != null;
            if (copy_slot(idx)) copy_check_and_promote(1);
            if (should_help) _nbhmll.help_copy();
            return _newchm;
        }

        private void copy_check_and_promote(final int workdone) {
            final int oldlen = _keys.length;

            long copyDone = _copyDone;
            long nowDone = copyDone + workdone;
            assert nowDone <= oldlen;

            if (workdone > 0) {
                while (!U.compareAndSetLong(this, COPY_DONE_OFF, copyDone, nowDone)) {
                    copyDone = _copyDone;
                    nowDone = copyDone + workdone;
                    assert nowDone <= oldlen;
                }
            }

            if (nowDone == oldlen && _nbhmll._chm == this && _nbhmll.CAS_ref(_chm_offset, this, _newchm)) {
                _nbhmll._last_resize_milli = System.currentTimeMillis();
            }
        }

        private boolean copy_slot(final int idx) {
            final CHM newchm = _newchm;
            assert newchm != null;

            final long key = U.getLongVolatile(_keys, rawIndex(_keys, idx));

            if (key == NO_KEY) {
                while (true) {
                    final long v = U.getLongVolatile(_vals, rawIndex(_vals, idx));
                    if (v == VTOMBPRIME) return false;
                    if (v == VNULL || v == VTOMB) {
                        if (CAS_val(idx, v, VTOMBPRIME)) return true;
                        continue;
                    }
                    if (v == VPRIME) {
                        if (CAS_val(idx, VPRIME, VTOMBPRIME)) return true;
                        continue;
                    }
                    if (CAS_val(idx, v, VTOMBPRIME)) return true;
                }
            }

            long oldv = U.getLongVolatile(_vals, rawIndex(_vals, idx));

            while (!isPrimeEnc(oldv)) {
                if (oldv == VNULL || oldv == VTOMB) {
                    if (CAS_val(idx, oldv, VTOMBPRIME)) return true;
                    oldv = U.getLongVolatile(_vals, rawIndex(_vals, idx));
                    continue;
                }
                U.putLongVolatile(_primes, rawIndex(_primes, idx), oldv);
                if (CAS_val(idx, oldv, VPRIME)) {
                    oldv = VPRIME;
                    break;
                }
                oldv = U.getLongVolatile(_vals, rawIndex(_vals, idx));
            }

            if (oldv == VTOMBPRIME) return false;

            long boxed = U.getLongVolatile(_primes, rawIndex(_primes, idx));
            if (!isDataEnc(boxed)) {
                int spins = 0;
                while (U.getLongVolatile(_vals, rawIndex(_vals, idx)) == VPRIME && !isDataEnc(boxed) && spins++ < 10_000) {
                    boxed = U.getLongVolatile(_primes, rawIndex(_primes, idx));
                    Library.onSpinWait();
                }
                if (!isDataEnc(boxed)) boxed = VTOMB;
            }

            newchm.putIfMatch(key, boxed, EXP_MATCH_EXACT, VNULL, false);

            while (true) {
                final long v = U.getLongVolatile(_vals, rawIndex(_vals, idx));
                if (v == VTOMBPRIME) return false;
                if (v != VPRIME) return false;
                if (CAS_val(idx, VPRIME, VTOMBPRIME)) return true;
            }
        }

        private long putIfMatch(final long key, final long putEnc, final int expKind, final long expEnc, final boolean adjustSize) {

            final int h = _nbhmll.hash(key);
            final int len = _keys.length;
            int idx = (h & (len - 1));

            int reprobe_cnt = 0;
            long K;
            long V;

            while (true) {
                V = U.getLongVolatile(_vals, rawIndex(_vals, idx));
                K = U.getLongVolatile(_keys, rawIndex(_keys, idx));

                if (K == NO_KEY) {
                    if (isPrimeEnc(V)) {
                        resize();
                        return copy_slot_and_check(idx, adjustSize).putIfMatch(key, putEnc, expKind, expEnc, adjustSize);
                    }

                    if (putEnc == VTOMB) return VTOMB;
                    if (expKind == EXP_MATCH_ANY) return VTOMB;

                    if (CAS_key(idx, NO_KEY, key)) {
                        _slots.add(1);
                        break;
                    }
                    continue;
                }

                if (K == key) break;

                if (++reprobe_cnt >= reprobe_limit(len)) {
                    final CHM newchm = resize();
                    if (adjustSize) _nbhmll.help_copy();
                    return newchm.putIfMatch(key, putEnc, expKind, expEnc, adjustSize);
                }

                idx = (idx + 1) & (len - 1);
            }

            while (true) {
                V = U.getLongVolatile(_vals, rawIndex(_vals, idx));
                if (putEnc == V) return V;

                if ((V == VNULL && tableFull(reprobe_cnt, len)) || isPrimeEnc(V)) {
                    resize();
                    return copy_slot_and_check(idx, adjustSize).putIfMatch(key, putEnc, expKind, expEnc, adjustSize);
                }

                if (!matches(V, expKind, expEnc)) {
                    return (V == VNULL) ? VTOMB : V;
                }

                if (CAS_val(idx, V, putEnc)) break;

                V = U.getLongVolatile(_vals, rawIndex(_vals, idx));

                if (isPrimeEnc(V)) {
                    return copy_slot_and_check(idx, adjustSize).putIfMatch(key, putEnc, expKind, expEnc, adjustSize);
                }
            }

            if (adjustSize) {
                final boolean wasPresent = isDataEnc(V);
                final boolean willPresent = isDataEnc(putEnc);
                if (!wasPresent && willPresent) _size.add(1);
                if (wasPresent && !willPresent) _size.add(-1);
            }

            return V;
        }
    }

    private final class SnapshotK implements LongIterator {
        final CHM _sschm;
        int _idx;
        long _nextK;
        boolean _hasNext;

        SnapshotK() {
            CHM top;
            while (true) {
                top = _chm;
                if (top._newchm == null) break;
                top.help_copy_impl(true);
            }
            _sschm = top;
            _idx = -1;
            advance();
        }

        private void advance() {
            _hasNext = false;

            if (_idx == -1) {
                _idx = 0;
                if (getEncoded(NO_KEY) != VNULL) {
                    _nextK = NO_KEY;
                    _hasNext = true;
                    return;
                }
            }

            final long[] keys = _sschm._keys;
            final int len = keys.length;
            while (_idx < len) {
                final long k = keys[_idx++];
                if (k == NO_KEY) continue;
                if (getEncoded(k) != VNULL) {
                    _nextK = k;
                    _hasNext = true;
                    return;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return _hasNext;
        }

        @Override
        public Long next() {
            return nextLong();
        }

        @Override
        public long nextLong() {
            if (!_hasNext) throw new NoSuchElementException();
            final long k = _nextK;
            advance();
            return k;
        }

        @Override
        public int skip(int n) {
            int i = n;
            while (i-- > 0 && hasNext()) nextLong();
            return n - (i + 1);
        }
    }

    private final class SnapshotV implements LongIterator {
        final SnapshotK _ks = new SnapshotK();

        @Override
        public boolean hasNext() {
            return _ks.hasNext();
        }

        @Override
        public Long next() {
            return nextLong();
        }

        @Override
        public long nextLong() {
            final long k = _ks.nextLong();
            return get(k);
        }

        @Override
        public int skip(int n) {
            return _ks.skip(n);
        }
    }

    private final class KeySet extends AbstractLongSet {
        @Override
        public LongIterator iterator() {
            return new SnapshotK();
        }

        @Override
        public int size() {
            return NonBlockingLong2LongHashMap.this.size();
        }

        @Override
        public boolean contains(long k) {
            return NonBlockingLong2LongHashMap.this.containsKey(k);
        }

        @Override
        public boolean remove(long k) {
            final long oldEnc = putIfMatch(k, VTOMB, EXP_MATCH_ANY, 0L);
            return isDataEnc(oldEnc);
        }

        @Override
        public void clear() {
            NonBlockingLong2LongHashMap.this.clear();
        }
    }

    private final class NBHMLEntry implements Long2LongMap.Entry {
        private final long k;
        private long v;

        NBHMLEntry(long k, long v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public long getLongKey() {
            return k;
        }

        @Override
        public long getLongValue() {
            return v;
        }

        @Override
        public long setValue(long value) {
            final long old = this.v;
            this.v = value;
            NonBlockingLong2LongHashMap.this.put(k, value);
            return old;
        }

        @Override
        @Deprecated
        public Long getKey() {
            return k;
        }

        @Override
        @Deprecated
        public Long getValue() {
            return v;
        }

        @Override
        @Deprecated
        public Long setValue(Long value) {
            if (value == null) throw new NullPointerException();
            return setValue((long) value);
        }
    }

    private final class SnapshotE implements ObjectIterator<Long2LongMap.Entry> {
        final SnapshotK _ks = new SnapshotK();

        @Override
        public boolean hasNext() {
            return _ks.hasNext();
        }

        @Override
        public Long2LongMap.Entry next() {
            final long k = _ks.nextLong();
            final long v = get(k);
            return new NBHMLEntry(k, v);
        }

        @Override
        public int skip(int n) {
            return _ks.skip(n);
        }
    }

    private final class EntrySet extends AbstractObjectSet<Long2LongMap.Entry> {
        @Override
        public ObjectIterator<Long2LongMap.Entry> iterator() {
            return new SnapshotE();
        }

        @Override
        public int size() {
            return NonBlockingLong2LongHashMap.this.size();
        }

        @Override
        public void clear() {
            NonBlockingLong2LongHashMap.this.clear();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Long2LongMap.Entry e)) return false;
            final long k = e.getLongKey();
            final long v = e.getLongValue();
            return containsKey(k) && get(k) == v;
        }

        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Long2LongMap.Entry e)) return false;
            final long kk = e.getLongKey();
            final long vv = e.getLongValue();
            return NonBlockingLong2LongHashMap.this.remove(kk, vv);
        }
    }
}

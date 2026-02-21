/*
 * TrustedLookupAccessor
 * Copyright (c) 2025 Burning_TNT<pangyl08@163.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hbm.lib.internal;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Get ready for <code>--sun-misc-unsafe-memory-access=deny</code>! <p>
 * This class <strong>must not</strong> be loaded on java 22 or earlier versions. <p>
 * JVMDG allows us to reference modern APIs while still emitting version 52 bytecode, but they will throw NoClassDefFoundError during Linkage.<p>
 *
 * @see <a href="https://gist.github.com/burningtnt/c188e65f048c2cf096db095e5858b5af">TrustedLookupAccessor</a>
 * @author Burning_TNT
 */
/*
final class TrustedLookupAccessor {
    private static MethodHandles.Lookup lookup;

    static {
        try {
            SequenceLayout VL_JNIInvokeInterface = MemoryLayout.sequenceLayout(8L, ValueLayout.ADDRESS);
            AddressLayout VL_P_JNIInvokeInterface = ValueLayout.ADDRESS.withTargetLayout(VL_JNIInvokeInterface);
            AddressLayout VL_PP_JNIInvokeInterface = ValueLayout.ADDRESS.withTargetLayout(VL_P_JNIInvokeInterface);

            SequenceLayout VL_JNINativeInterface = MemoryLayout.sequenceLayout(233L, ValueLayout.ADDRESS);
            AddressLayout VL_P_JNINativeInterface = ValueLayout.ADDRESS.withTargetLayout(VL_JNINativeInterface);
            AddressLayout VL_PP_JNINativeInterface = ValueLayout.ADDRESS.withTargetLayout(VL_P_JNINativeInterface);

            MemoryLayout VL_JVALUE = MemoryLayout.unionLayout(ValueLayout.JAVA_BOOLEAN.withName("z"), ValueLayout.JAVA_BYTE.withName("b"), ValueLayout.JAVA_CHAR.withName("c"), ValueLayout.JAVA_SHORT.withName("s"), ValueLayout.JAVA_INT.withName("i"), ValueLayout.JAVA_LONG.withName("j"), ValueLayout.JAVA_FLOAT.withName("f"), ValueLayout.JAVA_DOUBLE.withName("d"), ValueLayout.ADDRESS.withName("l"));

            Linker LINKER = Linker.nativeLinker();

            try (Arena ARENA = Arena.ofConfined()) {
                MethodHandle JNI_GetCreatedJavaVMs = LINKER.downcallHandle(SymbolLookup.libraryLookup(System.mapLibraryName("jvm"), ARENA)
                                                                                       .find("JNI_GetCreatedJavaVMs")
                                                                                       .orElseThrow(() -> new IllegalStateException("JNI_GetCreatedJavaVMs must exist.")), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

                MemorySegment pVM = ARENA.allocate(VL_PP_JNIInvokeInterface);
                MemorySegment nVMs = ARENA.allocate(ValueLayout.JAVA_INT);
                int ec = (int) JNI_GetCreatedJavaVMs.invokeExact(pVM, 1, nVMs);
                if (ec != 0) {
                    throw new IllegalStateException("JNI_GetCreatedJavaVMs returned error code " + ec);
                }
                if (nVMs.get(ValueLayout.JAVA_INT, 0L) != 1) {
                    throw new IllegalStateException("There must be one VM.");
                }

                MethodHandle GetEnv = LINKER.downcallHandle(pVM.get(VL_PP_JNIInvokeInterface, 0L).get(VL_P_JNIInvokeInterface, 0L)
                                                               .getAtIndex(ValueLayout.ADDRESS, 6L), FunctionDescriptor.of(ValueLayout.JAVA_INT, VL_PP_JNIInvokeInterface, VL_PP_JNINativeInterface, ValueLayout.JAVA_INT));

                MemorySegment ppEnv = ARENA.allocate(VL_PP_JNINativeInterface);
                ec = (int) GetEnv.invokeExact(pVM, ppEnv, 0x00010008);
                if (ec != 0) {
                    throw new IllegalStateException("GetEnv returned error code " + ec);
                }

                MemorySegment pEnv = ppEnv.get(VL_PP_JNINativeInterface, 0L);
                MemorySegment pJNINativeInterface = pEnv.get(VL_P_JNINativeInterface, 0L);
                MethodHandle FindClass = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 6L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                               .bindTo(pEnv);
                MethodHandle NewGlobalRef = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 21L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                  .bindTo(pEnv);
                MethodHandle GetMethodID = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 33L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                 .bindTo(pEnv);
                MethodHandle DeleteGlobalRef = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 22L), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                     .bindTo(pEnv);
                MethodHandle CallObjectMethodA = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 36L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                       .bindTo(pEnv);
                MethodHandle GetStaticMethodID = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 113L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                       .bindTo(pEnv);
                MethodHandle CallStaticObjectMethodA = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 116L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                             .bindTo(pEnv);
                MethodHandle CallStaticVoidMethodA = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 143L), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                           .bindTo(pEnv);
                MethodHandle GetStaticFieldID = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 144L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                      .bindTo(pEnv);
                MethodHandle GetStaticObjectField = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 145L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                          .bindTo(pEnv);
                MethodHandle NewStringUTF = LINKER.downcallHandle(pJNINativeInterface.getAtIndex(ValueLayout.ADDRESS, 167L), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
                                                  .bindTo(pEnv);

                MemorySegment lookupJClass = (MemorySegment) FindClass.invokeExact(ARENA.allocateFrom("java/lang/invoke/MethodHandles$Lookup"));
                MemorySegment lookupJClassRef = (MemorySegment) NewGlobalRef.invokeExact(lookupJClass);
                MemorySegment lookupJFieldID = (MemorySegment) GetStaticFieldID.invokeExact(lookupJClassRef, ARENA.allocateFrom("IMPL_LOOKUP"), ARENA.allocateFrom("Ljava/lang/invoke/MethodHandles$Lookup;"));
                MemorySegment lookupJObject = (MemorySegment) GetStaticObjectField.invokeExact(lookupJClassRef, lookupJFieldID);
                MemorySegment lookupJObjectRef = (MemorySegment) NewGlobalRef.invokeExact(lookupJObject);

                MemorySegment threadJClass = (MemorySegment) FindClass.invokeExact(ARENA.allocateFrom("java/lang/Thread"));
                MemorySegment threadJClassRef = (MemorySegment) NewGlobalRef.invokeExact(threadJClass);
                MemorySegment getCurrentThreadJMethodID = (MemorySegment) GetStaticMethodID.invokeExact(threadJClassRef, ARENA.allocateFrom("currentThread"), ARENA.allocateFrom("()Ljava/lang/Thread;"));
                MemorySegment threadJObject = (MemorySegment) CallStaticObjectMethodA.invokeExact(threadJClassRef, getCurrentThreadJMethodID, MemorySegment.NULL);
                MemorySegment threadJObjectRef = (MemorySegment) NewGlobalRef.invokeExact(threadJObject);

                MemorySegment getContextClassLoaderJMethodID = (MemorySegment) GetMethodID.invokeExact(threadJClassRef, ARENA.allocateFrom("getContextClassLoader"), ARENA.allocateFrom("()Ljava/lang/ClassLoader;"));
                MemorySegment classLoaderJObject = (MemorySegment) CallObjectMethodA.invokeExact(threadJObjectRef, getContextClassLoaderJMethodID, MemorySegment.NULL);
                MemorySegment classLoaderJObjectRef = (MemorySegment) NewGlobalRef.invokeExact(classLoaderJObject);

                MemorySegment classJClass = (MemorySegment) FindClass.invokeExact(ARENA.allocateFrom("java/lang/Class"));
                MemorySegment classJClassRef = (MemorySegment) NewGlobalRef.invokeExact(classJClass);
                MemorySegment forNameJMethodID = (MemorySegment) GetStaticMethodID.invokeExact(classJClassRef, ARENA.allocateFrom("forName"), ARENA.allocateFrom("(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"));

                MemorySegment targetJClassArguments = ARENA.allocate(VL_JVALUE, 3L);
                MemorySegment targetJClassArgument0 = ARENA.allocateFrom(TrustedLookupAccessor.class.getName());
                MemorySegment targetJClassArgument0PlatformString = (MemorySegment) NewStringUTF.invokeExact(targetJClassArgument0);
                MemorySegment targetJClassArgument0Ref = (MemorySegment) NewGlobalRef.invokeExact(targetJClassArgument0PlatformString);
                targetJClassArguments.set(ValueLayout.JAVA_LONG, 0L, targetJClassArgument0Ref.address());
                targetJClassArguments.set(ValueLayout.JAVA_BOOLEAN, VL_JVALUE.byteSize(), true);
                targetJClassArguments.set(ValueLayout.JAVA_LONG, VL_JVALUE.byteSize() * 2L, classLoaderJObjectRef.address());

                MemorySegment targetJClass = (MemorySegment) CallStaticObjectMethodA.invokeExact(classJClassRef, forNameJMethodID, targetJClassArguments);
                MemorySegment targetJClassRef = (MemorySegment) NewGlobalRef.invokeExact(targetJClass);
                MemorySegment targetJMethodID = (MemorySegment) GetStaticMethodID.invokeExact(targetJClassRef, ARENA.allocateFrom("callback"), ARENA.allocateFrom("(Ljava/lang/invoke/MethodHandles$Lookup;)V"));
                MemorySegment targetJClassArguments2 = ARENA.allocate(VL_JVALUE, 1L);
                targetJClassArguments2.set(ValueLayout.JAVA_LONG, 0L, lookupJObjectRef.address());
                CallStaticVoidMethodA.invokeExact(targetJClassRef, targetJMethodID, targetJClassArguments2);

                DeleteGlobalRef.invoke(lookupJClassRef);
                DeleteGlobalRef.invoke(lookupJObjectRef);
                DeleteGlobalRef.invoke(threadJClassRef);
                DeleteGlobalRef.invoke(threadJObjectRef);
                DeleteGlobalRef.invoke(classLoaderJObjectRef);
                DeleteGlobalRef.invoke(classJClassRef);
                DeleteGlobalRef.invoke(targetJClassArgument0Ref);
                DeleteGlobalRef.invoke(targetJClassRef);
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private TrustedLookupAccessor() {
    }

    @SuppressWarnings("unused")
    private static void callback(MethodHandles.Lookup lookup) {
        TrustedLookupAccessor.lookup = lookup;
    }

    static MethodHandles.Lookup lookup() {
        return lookup;
    }
}
*/

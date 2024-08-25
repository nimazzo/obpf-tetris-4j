// Generated by jextract

package com.example.autogenerated;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

/**
 * {@snippet lang = c:
 * typedef void (*ObpfActionHandler)(ObpfAction, void *)
 *}
 */
public class ObpfActionHandler {

    ObpfActionHandler() {
        // Should not be called directly
    }

    /**
     * The function pointer signature, expressed as a functional interface
     */
    public interface Function {
        void apply(int action, MemorySegment user_data);
    }

    private static final FunctionDescriptor $DESC = FunctionDescriptor.ofVoid(
            ObpfNativeInterface.C_INT,
            ObpfNativeInterface.C_POINTER
    );

    /**
     * The descriptor of this function pointer
     */
    public static FunctionDescriptor descriptor() {
        return $DESC;
    }

    private static final MethodHandle UP$MH = ObpfNativeInterface.upcallHandle(ObpfActionHandler.Function.class, "apply", $DESC);

    /**
     * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
     * The lifetime of the returned segment is managed by {@code arena}
     */
    public static MemorySegment allocate(ObpfActionHandler.Function fi, Arena arena) {
        return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
    }

    private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

    /**
     * Invoke the upcall stub {@code funcPtr}, with given parameters
     */
    public static void invoke(MemorySegment funcPtr, int action, MemorySegment user_data) {
        try {
            DOWN$MH.invokeExact(funcPtr, action, user_data);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}


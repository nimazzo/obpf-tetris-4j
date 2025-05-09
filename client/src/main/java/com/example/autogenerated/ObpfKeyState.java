// Generated by jextract

package com.example.autogenerated;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct {
 *     uint8_t bitmask;
 * }
 * }
 */
public class ObpfKeyState {

    ObpfKeyState() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        ObpfNativeInterface.C_CHAR.withName("bitmask")
    ).withName("$anon$34:13");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfByte bitmask$LAYOUT = (OfByte)$LAYOUT.select(groupElement("bitmask"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t bitmask
     * }
     */
    public static final OfByte bitmask$layout() {
        return bitmask$LAYOUT;
    }

    private static final long bitmask$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t bitmask
     * }
     */
    public static final long bitmask$offset() {
        return bitmask$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t bitmask
     * }
     */
    public static byte bitmask(MemorySegment struct) {
        return struct.get(bitmask$LAYOUT, bitmask$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t bitmask
     * }
     */
    public static void bitmask(MemorySegment struct, byte fieldValue) {
        struct.set(bitmask$LAYOUT, bitmask$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}


// Generated by jextract

package com.example;

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
 *     ObpfVec2 positions[4];
 * }
 * }
 */
public class ObpfMinoPositions {

    ObpfMinoPositions() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(4, ObpfVec2.layout()).withName("positions")
    ).withName("$anon$31:13");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout positions$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("positions"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static final SequenceLayout positions$layout() {
        return positions$LAYOUT;
    }

    private static final long positions$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static final long positions$offset() {
        return positions$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static MemorySegment positions(MemorySegment struct) {
        return struct.asSlice(positions$OFFSET, positions$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static void positions(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, positions$OFFSET, positions$LAYOUT.byteSize());
    }

    private static long[] positions$DIMS = { 4 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static long[] positions$dimensions() {
        return positions$DIMS;
    }
    private static final MethodHandle positions$ELEM_HANDLE = positions$LAYOUT.sliceHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static MemorySegment positions(MemorySegment struct, long index0) {
        try {
            return (MemorySegment)positions$ELEM_HANDLE.invokeExact(struct, 0L, index0);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * ObpfVec2 positions[4]
     * }
     */
    public static void positions(MemorySegment struct, long index0, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, positions(struct, index0), 0L, ObpfVec2.layout().byteSize());
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


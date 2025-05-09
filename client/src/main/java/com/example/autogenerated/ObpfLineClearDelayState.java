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
 *     uint8_t count;
 *     uint8_t lines[4];
 *     uint64_t countdown;
 *     uint64_t delay;
 * }
 * }
 */
public class ObpfLineClearDelayState {

    ObpfLineClearDelayState() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        ObpfNativeInterface.C_CHAR.withName("count"),
        MemoryLayout.sequenceLayout(4, ObpfNativeInterface.C_CHAR).withName("lines"),
        MemoryLayout.paddingLayout(3),
        ObpfNativeInterface.C_LONG_LONG.withName("countdown"),
        ObpfNativeInterface.C_LONG_LONG.withName("delay")
    ).withName("$anon$19:13");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfByte count$LAYOUT = (OfByte)$LAYOUT.select(groupElement("count"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t count
     * }
     */
    public static final OfByte count$layout() {
        return count$LAYOUT;
    }

    private static final long count$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t count
     * }
     */
    public static final long count$offset() {
        return count$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t count
     * }
     */
    public static byte count(MemorySegment struct) {
        return struct.get(count$LAYOUT, count$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t count
     * }
     */
    public static void count(MemorySegment struct, byte fieldValue) {
        struct.set(count$LAYOUT, count$OFFSET, fieldValue);
    }

    private static final SequenceLayout lines$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("lines"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static final SequenceLayout lines$layout() {
        return lines$LAYOUT;
    }

    private static final long lines$OFFSET = 1;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static final long lines$offset() {
        return lines$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static MemorySegment lines(MemorySegment struct) {
        return struct.asSlice(lines$OFFSET, lines$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static void lines(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, lines$OFFSET, lines$LAYOUT.byteSize());
    }

    private static long[] lines$DIMS = { 4 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static long[] lines$dimensions() {
        return lines$DIMS;
    }
    private static final VarHandle lines$ELEM_HANDLE = lines$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static byte lines(MemorySegment struct, long index0) {
        return (byte)lines$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * uint8_t lines[4]
     * }
     */
    public static void lines(MemorySegment struct, long index0, byte fieldValue) {
        lines$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final OfLong countdown$LAYOUT = (OfLong)$LAYOUT.select(groupElement("countdown"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t countdown
     * }
     */
    public static final OfLong countdown$layout() {
        return countdown$LAYOUT;
    }

    private static final long countdown$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t countdown
     * }
     */
    public static final long countdown$offset() {
        return countdown$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t countdown
     * }
     */
    public static long countdown(MemorySegment struct) {
        return struct.get(countdown$LAYOUT, countdown$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t countdown
     * }
     */
    public static void countdown(MemorySegment struct, long fieldValue) {
        struct.set(countdown$LAYOUT, countdown$OFFSET, fieldValue);
    }

    private static final OfLong delay$LAYOUT = (OfLong)$LAYOUT.select(groupElement("delay"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t delay
     * }
     */
    public static final OfLong delay$layout() {
        return delay$LAYOUT;
    }

    private static final long delay$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t delay
     * }
     */
    public static final long delay$offset() {
        return delay$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t delay
     * }
     */
    public static long delay(MemorySegment struct) {
        return struct.get(delay$LAYOUT, delay$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t delay
     * }
     */
    public static void delay(MemorySegment struct, long fieldValue) {
        struct.set(delay$LAYOUT, delay$OFFSET, fieldValue);
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


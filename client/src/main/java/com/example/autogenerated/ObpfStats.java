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
 *     uint64_t score;
 *     uint32_t lines_cleared;
 *     uint32_t level;
 * }
 * }
 */
public class ObpfStats {

    ObpfStats() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        ObpfNativeInterface.C_LONG_LONG.withName("score"),
        ObpfNativeInterface.C_INT.withName("lines_cleared"),
        ObpfNativeInterface.C_INT.withName("level")
    ).withName("$anon$9:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong score$LAYOUT = (OfLong)$LAYOUT.select(groupElement("score"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t score
     * }
     */
    public static final OfLong score$layout() {
        return score$LAYOUT;
    }

    private static final long score$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t score
     * }
     */
    public static final long score$offset() {
        return score$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t score
     * }
     */
    public static long score(MemorySegment struct) {
        return struct.get(score$LAYOUT, score$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t score
     * }
     */
    public static void score(MemorySegment struct, long fieldValue) {
        struct.set(score$LAYOUT, score$OFFSET, fieldValue);
    }

    private static final OfInt lines_cleared$LAYOUT = (OfInt)$LAYOUT.select(groupElement("lines_cleared"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t lines_cleared
     * }
     */
    public static final OfInt lines_cleared$layout() {
        return lines_cleared$LAYOUT;
    }

    private static final long lines_cleared$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t lines_cleared
     * }
     */
    public static final long lines_cleared$offset() {
        return lines_cleared$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t lines_cleared
     * }
     */
    public static int lines_cleared(MemorySegment struct) {
        return struct.get(lines_cleared$LAYOUT, lines_cleared$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t lines_cleared
     * }
     */
    public static void lines_cleared(MemorySegment struct, int fieldValue) {
        struct.set(lines_cleared$LAYOUT, lines_cleared$OFFSET, fieldValue);
    }

    private static final OfInt level$LAYOUT = (OfInt)$LAYOUT.select(groupElement("level"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t level
     * }
     */
    public static final OfInt level$layout() {
        return level$LAYOUT;
    }

    private static final long level$OFFSET = 12;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t level
     * }
     */
    public static final long level$offset() {
        return level$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t level
     * }
     */
    public static int level(MemorySegment struct) {
        return struct.get(level$LAYOUT, level$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t level
     * }
     */
    public static void level(MemorySegment struct, int fieldValue) {
        struct.set(level$LAYOUT, level$OFFSET, fieldValue);
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


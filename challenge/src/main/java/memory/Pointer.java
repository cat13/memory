package memory;

/**
 * Handler of reserved memory. instance of this class represented a slot reserved memory.
 * A live instance of Pointer is required to operate memory through instance of MemoryManager
 * Note: the instance of Pointer is dead once MemoryManager.free(point) called and using dead
 * pointer will throw runtimeexception.
 */
public final class Pointer {
    public final int size;

    /**
     *
     * @param size size of memory wanted to reserve.
     */
    Pointer(int size) {
        this.size = size;
    }
}

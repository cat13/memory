package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryManagerTest {
    @Test
    public void shouldNotAllowAllocateBiggerThanBufferSize(){
        MemoryManager manager = new MemoryManager(10);

       assertThrows(NoEnoughSpaceException.class,
               ()-> manager.allocate(12));
    }
    @Test
    public void shouldNotAllowAllocateBiggerThanBufferSize2(){
        MemoryManager manager = new MemoryManager(10);
        manager.allocate(3);
        manager.allocate(3);
        manager.allocate(3);
        assertThrows(NoEnoughSpaceException.class, ()->manager.allocate(3));
    }

    @Test
    public void shouldAllowAllocateAfterFree(){
        MemoryManager manager = new MemoryManager(10);
        manager.allocate(3);
        Pointer p = manager.allocate(3);
        manager.allocate(3);
        manager.free(p);
        manager.allocate(3);
    }

    @Test
    public void shouldReadBackAfterReAllocate(){
        MemoryManager manager = new MemoryManager(10);
        manager.allocate(3);
        Pointer p = manager.allocate(3);
        Pointer writeHandler = manager.allocate(3);
        manager.write(writeHandler, "hel".toCharArray());
        manager.free(p);
        manager.allocate(3);
        char[] readBack = manager.read(writeHandler, 3);
        assertEquals('h', readBack[0]);
        assertEquals('e', readBack[1]);
        assertEquals('l', readBack[2]);
    }

    @Test
    public void shouldAllowContinueAllocate(){
        MemoryManager manager = new MemoryManager(10);
        Pointer p1 = manager.allocate(1);
        Pointer p2 = manager.allocate(2);
        Pointer p3 = manager.allocate(3);
        assertNotEquals(p1, p2);
        assertNotEquals(p2, p3);
    }

    @Test
    public void shouldGetAPoint(){
        MemoryManager manager = new MemoryManager(20);
        assertNotNull(manager.allocate(5));
    }

    @Test
    public void shouldAllowWrite(){
        MemoryManager manager = new MemoryManager(20);
        Pointer p = manager.allocate(5);
        assertNotNull(p);
        char[] data = "hello".toCharArray();
        manager.write(p, data);
    }

    @Test
    public void shouldGetDataBack(){
        MemoryManager manager = new MemoryManager(20);
        Pointer p = manager.allocate(5);
        assertNotNull(p);
        char[] data = "hello".toCharArray();
        manager.write(p, data);
        char[] read = manager.read(p, 2);
        assertNotNull(read);
        assertEquals('h', read[0]);
        assertEquals('e', read[1]);
    }

}

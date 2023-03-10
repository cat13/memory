package memory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/*
Instances of this class represent a memory manager. A instance of the class will allocate 2 * size of bytes
of storage by calling constructor new MemoryManager(int size). the memory will be initialized to default char
value '\d0000'

This class is NOT threadsafe.
 */
public class MemoryManager {
    private final int initSize;
    private char[] memory;
    private int capacity;

    private HashMap<Pointer, Integer> point2Index = new HashMap<>();
    private HashMap<Pointer, Range> point2Range = new HashMap<>();
    private ArrayList<Pointer> pointers = new ArrayList<>();

    /**
     * pre-allocate 2 * size of storage from memory heap. the memory will be initialized to default value '\d0000'
     * the capacity will not change once allocated.
     * @param size size of memory pre-allocate. 2 * size bytes of memory will be occupied.
     */
    public MemoryManager(int size){
        initSize = size;
        memory = new char[initSize];
        capacity = initSize;
    }

    /**
     * reserve 2 * size of bytes memory from the pre-allocated storage for further use.
     *
     * @param the size to reserve.
     * @return handler of reserved memory.
     * @throws NoEnoughSpaceException if no enough space to allocate.
     */
    public Pointer allocate(int size){
        if(size > capacity){
            throw new NoEnoughSpaceException("exceed the capacity of memory");
        }
        int lastIndex = getLastIndex(pointers);
        if(lastIndex + size > initSize){
            refresh();
            lastIndex = getLastIndex(pointers);
        }
        Range range = new Range(lastIndex, lastIndex + size - 1);
        Pointer ret = new Pointer(size);
        point2Range.put(ret, range);
        pointers.add(ret);
        point2Index.put(ret, pointers.size() - 1);
        capacity -= size;
        return ret;
    }

    private int getLastIndex(ArrayList<Pointer> pointers) {
        if(pointers.size() == 0){
            return 0;
        }else{
            Pointer lastPoint = pointers.get(pointers.size() - 1);
            Range r = point2Range.get(lastPoint);
            return r.end + 1;
        }
    }

    /**
     * Release reserved memory represented by the pointer. released memory will put back heap of pre-allocated memory pool.
     * @param pointer the handler represent the reserved memory. the instance of point is dead after free be called,
     *                using dead point will throw runtime exception.
     */
    public void free(Pointer pointer){
        Integer index = point2Index.remove(pointer);
        if(index != null){
            pointers.set(index, null);
            ListIterator<Pointer> it = pointers.listIterator(pointers.size());
            while(it.hasPrevious()){
                Pointer p = it.previous();
                if(p == null){
                    it.remove();
                }else{
                    //only trim last elements
                    break;
                }
            }
        }
        Range range = point2Range.remove(pointer);
        capacity += (range.end - range.begin + 1);
    }

    /**
     * write data into reserved memory which represented by the pointer. the write operation is last write win. previous
     * data will be over write.
     * @param pointer handler of reserved memory. runtime exception throw if dead point is used. see free(Pointer point)
     * @param data data to write
     *
     */
    public void write(Pointer pointer, char[] data){
        Range range = point2Range.get(pointer);
        if(range == null){
            throw new RuntimeException("Invalid pointer");
        }
        if(data.length > (range.end - range.begin + 1)){
            throw new RuntimeException("The size of data exceed the size of allocated memory for the point");
        }
        int begin = range.begin;
        for(int i = 0; i < data.length; i++, begin++){
            memory[begin] = data[i];
        }
    }


    /**
     * read size of data from reserved memory represented by pointer.
     * if reading size bigger than reserved size of memory
     * reserved return size of reserved size. reading memory not write before will not
     * throw exception. default value will return.
     * @param pointer handler of reserved memory.
     * @param size size of data expect to read.
     * @return char of array. if the memory never write before. default element value will be '\u0000'
     */
    public char[] read(Pointer pointer, int size){
        Range range = point2Range.get(pointer);
        if(range == null){
            throw new RuntimeException("Invalid point used.");
        }
        int readSize = Math.min(size, range.size());
        char[] ret = new char[size];
        for(int begin = range.begin, i = 0; i < readSize; i++, begin++){
            ret[i] = memory[begin];
        }
        return ret;
    }

    /**
     * reallocate memory to handle fragment.
     */
    private void refresh(){
        ArrayList<Pointer> temp = new ArrayList<>();
        for(int i = 0; i < pointers.size(); i++){
            Pointer p = pointers.get(i);
            if(p != null){
                Range range = point2Range.remove(p);
                point2Range.remove(p);
                if(range == null){
                    throw new RuntimeException("Invalid status, point to a range not exist");
                }
                int lastIndex = getLastIndex(temp);
                Range newRange = new Range(lastIndex, lastIndex + range.size() - 1);
                for(int j = 0; j < range.size(); j++){
                    memory[newRange.begin + j] = memory[range.begin + j];
                    memory[range.begin + j] = '\u0000';
                }
                point2Range.put(p, newRange);
                temp.add(p);
                point2Index.put(p, temp.size() - 1);
            }
        }
        pointers = temp;
    }

    /**
     * begin(inclusive) and end(inclusive) of address of reserved memory.
     */
    private static class Range{
        private final int begin;
        private final int end;

        public int size(){
            return end - begin + 1;
        }

        private Range(int begin, int end) {
            if(end < begin){
                throw new IllegalArgumentException("the end index should bigger or equals to begin index");
            }
            this.begin = begin;
            this.end = end;
        }
    }

    int getCapacity(){
        return capacity;
    }
}

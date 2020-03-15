package cpen221.mp3.cache;

import java.util.HashMap;
import java.util.Set;

public class Cache<T extends Cacheable> {
    /* the default cache size is 32 objects */
    private static final int DSIZE = 32;
    /* the default timeout value is 3600s */
    private static final int DTIMEOUT = 3600;

    private int capacity;
    private int timeout;
    private final HashMap<String, cacheObject> cacheHashMap;

    //Representation Invariant:
    //  capacity of the cache should be a positive integer
    //  timeout of the cache should be a positive integer

    //Abstraction Function:
    //  AF(capacity, timeout) = a map, map, contains keys of type String and values of type cacheObject, such that
    //                          map.keyset() is a set of identity strings for objects in map.values()
    //                          map.values() is a set of objects of type cacheObject,
    //                          capacity deciding the maximum size of the map,
    //                          timeout indicating how long an item can be stored inside the cache before getting removed

    //Thread safety argument:
    //  This class is thread-safe because it is using thread-safe data type
    //  -cache was built upon using HashMap, which is a thread-safe collection
    //  -this class uses only a single thread and the HashMap cacheHashMap was wrap inside
    //   synchronized keyword to acquire collection's lock

    private class cacheObject {
        long lastAccessed;
        long InsertTime;
        T value;

        //Representation Invariant:
        //  value is java generic type T, which implements Cacheable interface,
        //  value != null

        //Abstraction Function:
        //  AF(value) = an object that would be placed into a cache, which contains
        //              value of type T, type T implements Cacheable interface, and
        //              InsertTime indicating when this object of type cacheObject is put into a cache, and
        //              lastAccessed indicating when this object of type cacheObject is last accessed from the cache

        cacheObject(T value) {
            this.value = value;
        }

        void generateInsertTime(){
            InsertTime = System.currentTimeMillis();
        }

        void generateLastAccessed(){
            lastAccessed = System.currentTimeMillis();
        }
    }

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold
     * @param timeout  the duration an object should be in the cache before it times out
     */
    public Cache(int capacity, int timeout) {

        this.capacity = capacity;
        //convert to mili-seconds
        this.timeout = timeout*1000;

        //create a Hashmap with a fixed capacity
        cacheHashMap = new HashMap<>(capacity, 1);

        //thread that will remove the expired objects
        Thread removeExpired = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception ignored) {
                    }
                    removeAllExpired();
                }
            }
        });

        removeExpired.start();
    }

    /**
     * this is an added method that helps removing all the expired
     * items in the cache after a given timeout
     */
    private void removeAllExpired(){
        synchronized (cacheHashMap) {
            long currentTime = System.currentTimeMillis();

            Set<String> keySet = cacheHashMap.keySet();
            for (String key : keySet) {
                cacheObject object = cacheHashMap.get(key);

                if (currentTime > timeout + object.InsertTime) {
                    cacheHashMap.remove(key);
                }
            }
        }
    }

    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE,DTIMEOUT);
    }

    /**
     * Add a value to the cache.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     *
     * @param object value that will be added to the cache
     */
    public boolean put(T object) {
        // TODO: implement this method
        synchronized (cacheHashMap) {
            //create a cacheObject for T object
            cacheObject newElement = new cacheObject(object);
            newElement.generateInsertTime();
            newElement.generateLastAccessed();

            //if the cache is not full
            if (cacheHashMap.size() < capacity) {
                cacheHashMap.put(object.id(), newElement);
            }
            //if its full
            else if (cacheHashMap.size() == capacity) {
                //find the least recently accessed (LRU)
                long currentTime = System.currentTimeMillis();
                long longestTime = -1;
                Set<String> keySet = cacheHashMap.keySet();
                String IDofTheLRU = "";

                for (String key : keySet) {
                    cacheObject c = cacheHashMap.get(key);
                    if (currentTime - c.lastAccessed > longestTime) {
                        longestTime = currentTime - c.lastAccessed;
                        IDofTheLRU = key;
                    }
                }
                //remove the LRU and add the new object
                cacheHashMap.remove(IDofTheLRU);
                cacheHashMap.put(object.id(), newElement);
            }
            return true;
        }
    }

    /**
     * Search inside the cache for the provided id key and
     * return the value of type T that matches with the key found.
     * If the key id is not found, throw IllegalArgumentException
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws IllegalArgumentException when object not found
     */
    public T get(String id) {
        synchronized (cacheHashMap) {
            if (cacheHashMap.containsKey(id)) {
                //regenerate the last accessed time
                cacheHashMap.get(id).generateLastAccessed();
                //return the content to the requester
                return cacheHashMap.get(id).value;
            } else {
                throw new IllegalArgumentException("object is not in the cache");
            }
        }
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed. Update the last recently accessed time of the object with
     * the provided id
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    public boolean touch(String id) {
        /* TODO: Implement this method */
        synchronized (cacheHashMap) {
            //refresh the inset time by regenerate the time the object is inserted
            //so that the object won't be removed from the list
            cacheHashMap.get(id).generateInsertTime();
            cacheHashMap.get(id).generateLastAccessed();
            return true;
        }
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public boolean update(T t) {
        /* TODO: implement this method */
        synchronized (cacheHashMap) {
            cacheObject updateElement = new cacheObject(t);
            //reset the timer
            updateElement.generateInsertTime();
            //update the LRU
            updateElement.generateLastAccessed();
            cacheHashMap.replace(t.id(), updateElement);
            return true;
        }
    }

    /**
     * This added method will return the size of the cache
     *
     * @return the size of the cache
     */
    public int size() {
        synchronized (cacheHashMap) {
            return cacheHashMap.size();
        }
    }

}

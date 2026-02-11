package com.example.juegosostenibilidad;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pool de objetos genérico para reutilización
 */
public class ObjectPool<T> {

    private final List<T> pool;
    private final List<T> activeObjects;
    private final ObjectFactory<T> factory;
    private final int maxSize;

    public interface ObjectFactory<T> {
        T create();
    }

    public ObjectPool(ObjectFactory<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ArrayList<>(initialSize);
        this.activeObjects = new CopyOnWriteArrayList<>();

        for (int i = 0; i < initialSize; i++) {
            pool.add(factory.create());
        }
    }

    public synchronized T obtain() {
        T obj;

        if (pool.isEmpty()) {
            if (activeObjects.size() < maxSize) {
                obj = factory.create();
            } else {
                return null;
            }
        } else {
            obj = pool.remove(pool.size() - 1);
        }

        activeObjects.add(obj);
        return obj;
    }

    public synchronized void free(T obj) {
        activeObjects.remove(obj);
        pool.add(obj);
    }

    public synchronized void freeAll() {
        pool.addAll(activeObjects);
        activeObjects.clear();
    }

    public List<T> getActiveObjects() {
        return activeObjects;
    }

    public int getActiveCount() {
        return activeObjects.size();
    }

    public int getPoolSize() {
        return pool.size();
    }

    public boolean canObtain() {
        return !pool.isEmpty() || activeObjects.size() < maxSize;
    }
}

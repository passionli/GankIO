package com.liguang.imageloaderdemo.album.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * GC
 */
public class GCWeakReference extends WeakReference {
    public GCWeakReference(Object referent) {
        super(referent);
    }

    public GCWeakReference(Object referent, ReferenceQueue q) {
        super(referent, q);
    }
}

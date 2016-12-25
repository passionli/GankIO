package com.liguang.gankio.album.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * OutOfMemory Error
 */
public class OOMSoftReference<B> extends SoftReference {
    public OOMSoftReference(Object referent) {
        super(referent);
    }

    public OOMSoftReference(Object referent, ReferenceQueue q) {
        super(referent, q);
    }
}

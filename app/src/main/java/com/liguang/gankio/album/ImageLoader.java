package com.liguang.gankio.album;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.liguang.gankio.R;
import com.liguang.gankio.album.cache.DiskLruCache;
import com.liguang.gankio.album.cache.OOMSoftReference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private Drawable mDefaultBitmapDrawable;

    private static final int DISK_CACHE_INDEX = 0;
    public static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static final int MESSAGE_POST_RESULT = 0;
    /**
     * 8k 网络IO BUFFER
     */
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static ImageLoader sInstance;
    private ImageResizer mImageResizer;
    private Context mContext;
    //为兼容Android 2.2版本，此处采用support-V4兼容包中的LruCache
    private LruCache<String, BitmapDrawable> mMemoryCache;
    private Set<OOMSoftReference<Bitmap>> mReusableBitmaps;
    //磁盘缓存总大小 50M
    private static final long DISK_LRU_CACHE_SIZE = 50 * 1024 * 1024;
    private DiskLruCache mDiskLruCache;
    private boolean mIsDiskLruCacheCreated;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg);

        }
    };


    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            //工厂负责创建线程对象
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };
    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            //链表实现的阻塞式队列
            new LinkedBlockingQueue<Runnable>(), sThreadFactory);

    private ImageLoader(Context context) {
        mContext = context;
        mDefaultBitmapDrawable = mContext.getResources().getDrawable(R.drawable.image_default);
        mImageResizer = new ImageResizer(this);
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d(TAG, "ImageLoader: maxMemory=" + maxMemory / 1024 + " MB");
        //可用内存的1/8
        int cacheSize = maxMemory / 8;
//        cacheSize = 4;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmap) {
                final int size = getBitmapSize(bitmap) / 1024;
                return size == 0 ? 1 : size;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                if (CountingBitmapDrawable.class.isInstance(oldValue)) {
                    //这部分Bitmap不会移入OOMSoftReference中
                    //3.0之前Bitmap像素内存在Native层的Heap中，在这里及时回收可避免GC等待Native层回收暂停这个世界，UI卡顿。
                    // 2.2以下的GC是会停止所有线程，2.3才引入并发GC
                    ((CountingBitmapDrawable) oldValue).setIsCached(false);
                } else {
                    if (Utils.hasHoneycomb()) {
                        //3.0以上Bitmap的像素在Java Heap中，会导致Java堆内存被像素内存占着。所以Fresco使用Ashmem
                        mReusableBitmaps.add(new OOMSoftReference<Bitmap>(oldValue.getBitmap()));
                        Log.d(TAG, "entryRemoved: add bitmap to OOMSoftReference set");
                    }
                }
            }
        };

        if (Utils.hasHoneycomb()) {
            mReusableBitmaps =
                    Collections.synchronizedSet(new HashSet<OOMSoftReference<Bitmap>>());
        }

        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (getUsableSpace(diskCacheDir) > DISK_LRU_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_LRU_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
                mIsDiskLruCacheCreated = false;
            }
        } else {
            Log.d(TAG, "ImageLoader: 空间不足，磁盘缓存创建失败");
            mIsDiskLruCacheCreated = false;
        }
    }

    private static int getBitmapSize(BitmapDrawable bitmapDrawable) {
        Bitmap bitmap = bitmapDrawable.getBitmap();

        if (Utils.hasKitKat()) {
            //4.4以上分配的字节可能比实际的大
            // 因为可能是复用的之前大的同类型 RGB_565 的Bitmap像素内存
            return bitmap.getAllocationByteCount();
        }

        if (Utils.hasHoneycombMR1()) {
            //3.1以上
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    private long getUsableSpace(File diskCacheDir) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return diskCacheDir.getUsableSpace();
        else {
            //Android 2.2之前的版本
            StatFs statFs = new StatFs(diskCacheDir.getPath());
            return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        }
    }

    private File getDiskCacheDir(Context context, String path) {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String cachePath;
        if (externalStorageAvailable) {
            cachePath = Environment.getExternalStorageDirectory()
                    + File.separator + path;
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        File folder = new File(cachePath);
        if (!folder.exists()) {
            //permission callback to high level activity

            boolean mkdirSucc = folder.mkdirs();
            Log.d(TAG, "getDiskCacheDir: mkdirs result = " + mkdirSucc);
        }
        return folder;
    }

    public static ImageLoader getInstance(Context context) {
        Log.d(TAG, "getInstance: ");
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    private String hashKeyFormUrl(String url) {
        Log.d(TAG, "hashKeyFormUrl: ");
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        Log.d(TAG, "bytesToHexString: ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private void putBitmapToMemoryCache(String key, BitmapDrawable bitmap) {
        Log.d(TAG, "putBitmapToMemoryCache: ");
        if (CountingBitmapDrawable.class.isInstance(bitmap)) {
            ((CountingBitmapDrawable) bitmap).setIsCached(true);
        }
        mMemoryCache.put(key, bitmap);
    }

    private BitmapDrawable getBitmapFromMemoryCache(String key) {
        Log.d(TAG, "getBitmapFromMemoryCache: ");
        BitmapDrawable memValue = null;

        memValue = mMemoryCache.get(key);

        if (memValue != null) {
            Log.d(TAG, "getBitmapFromMemoryCache: Memory cache hit");
        }

        return memValue;
    }

    /**
     * 从网络上下载一张图片
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private BitmapDrawable loadBitmapFromNetwork(String url, int reqWidth, int reqHeight) throws IOException {
        Log.d(TAG, "loadBitmapFromNetwork: ");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不允许在主线程访问网络");
        }
        if (mDiskLruCache == null) {
            Log.d(TAG, "loadBitmapFromNetwork: 本地文件系统空间不足，无法从网络下载图片到本地");
            return null;
        }

        String key = hashKeyFormUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            //给Data模块指定其输出流
            if (downloadData(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }

        //从网络下载数据到DiskLruCache完毕
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    /**
     * 从HTTP下载数据到DiskLruCache的输出流
     *
     * @param urlString    输入参数，URI，资源定位符
     * @param outputStream 数据输出流，流向DiskLruCache
     * @return 是否下载成功
     */
    private boolean downloadData(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "downloadData: HTTP 下载图片失败", e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            close(out);
            close(in);
        }

        return false;
    }

    private void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private BitmapDrawable loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "loadBitmapFromDiskCache: 不推荐从UI线程访问本地文件系统");
        }
        if (mDiskLruCache == null) {
            Log.e(TAG, "loadBitmapFromDiskCache: 文件系统空间不足");
            return null;
        }

        BitmapDrawable bitmapDrawable = null;
        String key = hashKeyFormUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            //从外存到内存需要进行图片压缩进内存
            Bitmap bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
            //Bitmap从外存加载到内存时，添加到内存管理模块
            if (bitmap != null) {
                if (Utils.hasHoneycomb()) {
                    bitmapDrawable = new BitmapDrawable(mContext.getResources(),bitmap);
                } else {
                    bitmapDrawable = new CountingBitmapDrawable(mContext.getResources(), bitmap);
                }
                putBitmapToMemoryCache(key, bitmapDrawable);
            }
        }

        return bitmapDrawable;
    }

    /**
     * 同步加载图片
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public BitmapDrawable loadBitmap(String url, int reqWidth, int reqHeight) {
        BitmapDrawable bitmap = loadBitmapFromMemoryCache(url);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmap: from cache");
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmap: from disk");
                return bitmap;
            }

            bitmap = loadBitmapFromNetwork(url, reqWidth, reqHeight);
            Log.d(TAG, "loadBitmap: from network");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "loadBitmap: DiskLruCache in not created");
            bitmap = downloadBitmapFromUrl(url);
        }

        return bitmap;
    }

    private BitmapDrawable downloadBitmapFromUrl(String url) {
        return null;
    }

    private BitmapDrawable loadBitmapFromMemoryCache(String url) {
        Log.d(TAG, "loadBitmapFromMemoryCache: url " + url);
        String key = hashKeyFormUrl(url);
        Log.d(TAG, "loadBitmapFromMemoryCache: key " + key);
        return getBitmapFromMemoryCache(key);
    }

    /**
     * 异步加载接口
     *
     * @param uri
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight, final ImageLoadingListener listener) {
//        String lastUri = (String) imageView.getTag(ImageLoader.TAG_KEY_URI);
//        //ImageView对象的当前请求Uri可能已经由于复用原因被上层修改
//        if (!uri.equals(lastUri)) {
//            //这行代码需要在主线程中执行
//            imageView.setImageDrawable(mDefaultBitmapDrawable);
//        }

        //内部需要在每个ImageView对象上记住最新的URI
        //GCWeakReference<ImageView> mViewRef;//下层对上层持有弱引用，防止ImageView无法回收
        imageView.setTag(TAG_KEY_URI, uri);
        listener.onLoadStart(imageView);
        //先从内存找Bitmap对象
        BitmapDrawable value = loadBitmapFromMemoryCache(uri);
        if (value != null) {
            Log.d(TAG, "bindBitmap: get bitmap from memory cache level");
            //直接在这里给ImageView设置Bitmap对象。
            // 之前可能下面的任务队列中还有该ImageView对象的引用，
            // 但是他们都已经旧了，故他们在完成时都会被丢弃
            imageView.setImageDrawable(value);
            listener.onLoadComplete(imageView);
            return;
        }

        //TODO 取消之前ImageView的加载任务
        // 遍历一遍任务集合，设置为取消状态?

        AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(),
                //TODO optimal
                BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_default));
        imageView.setImageDrawable(asyncDrawable);

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                BitmapDrawable bitmap = loadBitmap(uri, reqWidth, reqHeight);
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap, listener);
                    //把结果数据交给UI线程
                    mUIHandler.post(new Display(result));
//                    mUIHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };

        //把任务提交给任务队列，有下层线程池并行处理
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    /**
     * 任务线程传递给主线程的数据
     */
    class LoaderResult {
        ImageView imageView;
        ImageLoadingListener listener;
        /**
         * 下层Uri，用于异步结果的匹配
         */
        String lastUri;
        BitmapDrawable bitmap;

        public LoaderResult(ImageView imageView, String lastUri, BitmapDrawable bitmap, ImageLoadingListener listener) {
            this.imageView = imageView;
            this.lastUri = lastUri;
            this.bitmap = bitmap;
            this.listener = listener;
        }
    }

    public interface ImageLoadingListener {
        void onLoadStart(ImageView imageView);

        void onLoadComplete(ImageView imageView);
    }

    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        //多线程会从Set中取Bitmap
        if (!mReusableBitmaps.isEmpty()) synchronized (mReusableBitmaps) {
            Iterator<OOMSoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
            Bitmap item;
            while (iterator.hasNext()) {
                item = (Bitmap) iterator.next().get();

                if (item != null && item.isMutable()) {
                    if (canUseForInBitmap(item, options)) {
                        Log.i(TAG, "getBitmapFromReusableSet: hit in OOMSoftReference Set");
                        bitmap = item;
                        iterator.remove();
                        break;
                    }
                } else {
                    //移除被回收的OOMSoft对象
                    iterator.remove();
                }
            }
        }

        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options options) {
        if (!Utils.hasKitKat()) {
            return candidate.getWidth() == options.outWidth
                    && candidate.getHeight() == options.outHeight
                    && options.inSampleSize == 1;
        }

        int width = options.outWidth / options.inSampleSize;
        int height = options.outHeight / options.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        //从缓存中拿出来的位图内存比需要使用的内存大才可用
        return byteCount <= candidate.getAllocationByteCount();
    }

    /**
     * 每个像素点占用多少字节
     *
     * @param config
     * @return
     */
    private int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }


    private static class AsyncDrawable extends BitmapDrawable {

        public AsyncDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }
    }
}

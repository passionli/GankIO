package com.liguang.imageloaderdemo.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//TODO 1.void d(String message, Object... args); 2.log StackTraceElement.

/**
 * A wrapper of {@link Log Log}. Use
 * {@link #enableWriteLogFile(boolean)} to enable logging to files.
 */
public class LGLog {
    private static final String TAG = LGLog.class.getSimpleName();

    public static final boolean LOGGING_ENABLED = true;

    public static final boolean LOGGING_D_ENABLED = true;
    public static final boolean LOGGING_V_ENABLED = true;
    public static final boolean LOGGING_I_ENABLED = true;
    public static final boolean LOGGING_W_ENABLED = true;
    public static final boolean LOGGING_E_ENABLED = true;

    private static final char LEVEL_D = 'D';
    private static final char LEVEL_V = 'V';
    private static final char LEVEL_I = 'I';
    private static final char LEVEL_W = 'W';
    private static final char LEVEL_E = 'E';

    private static volatile LogWriterHandler sLogWriterHandler;

    // private static final Object sLock = new Object();

    public static void enableWriteLogFile(boolean enable) {
        LogWriterThread.LOGGING__WRITE_FILE_ENABLED = enable;
    }

    /**
     * 目前上层不需要主动destroy
     *
     * @hide
     */
    @SuppressWarnings("unused")
    private static void destroy() {
        if (sLogWriterHandler != null) {
            sLogWriterHandler.sendEmptyMessage(LogWriterHandler.MSG_DESTROY);
            sLogWriterHandler = null;
        }
    }

    private static void write(char level, String tag, String message) {
        if (LogWriterThread.LOGGING__WRITE_FILE_ENABLED) {
            ensureHandler();

            // if you want to make sure log message's time is serial,just add a
            // lock
            // synchronized (sLock) {
            Message msg = sLogWriterHandler.obtainMessage(
                    LogWriterHandler.MSG_WRITE,
                    new LogMessage(level, System.currentTimeMillis(), Process
                            .myPid(), Thread.currentThread().getId(), tag,
                            message));
            sLogWriterHandler.sendMessage(msg);
            // }
        }
    }

    public static void d(String tag, String message) {
        if (LOGGING_ENABLED) {
            if (LOGGING_D_ENABLED) {
                Log.d(tag, message);
                write(LEVEL_D, tag, message);
            }
        }
    }

    private static void ensureHandler() {
        if (sLogWriterHandler == null) {
            synchronized (LGLog.class) {
                if (sLogWriterHandler == null) {
                    LogWriterThread logWriterThread = new LogWriterThread(
                            LogWriterThread.TAG);
                    logWriterThread.start();
                    sLogWriterHandler = new LogWriterHandler(
                            logWriterThread.getLooper(), logWriterThread);
                }
            }
        }
    }

    public static void d(String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (LOGGING_D_ENABLED) {
                Log.d(tag, message, cause);
                write(LEVEL_D, tag,
                        message + '\n' + Log.getStackTraceString(cause));
            }
        }
    }

    public static void v(String tag, String message) {
        if (LOGGING_ENABLED) {
            if (LOGGING_V_ENABLED) {
                Log.v(tag, message);
                write(LEVEL_V, tag, message);
            }
        }
    }

    public static void v(String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (LOGGING_V_ENABLED) {
                Log.v(tag, message, cause);
                write(LEVEL_V, tag,
                        message + '\n' + Log.getStackTraceString(cause));
            }
        }
    }

    public static void i(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (LOGGING_I_ENABLED) {
                Log.i(tag, message);
                write(LEVEL_I, tag, message);
            }
        }
    }

    public static void i(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (LOGGING_I_ENABLED) {
                Log.i(tag, message, cause);
                write(LEVEL_I, tag,
                        message + '\n' + Log.getStackTraceString(cause));
            }
        }
    }

    public static void w(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (LOGGING_W_ENABLED) {
                Log.w(tag, message);
                write(LEVEL_W, tag, message);
            }
        }
    }

    public static void w(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (LOGGING_W_ENABLED) {
                Log.w(tag, message, cause);
                write(LEVEL_W, tag,
                        message + '\n' + Log.getStackTraceString(cause));
            }
        }
    }

    public static void e(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (LOGGING_E_ENABLED) {
                Log.e(tag, message);
                write(LEVEL_E, tag, message);
            }
        }
    }

    public static void e(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (LOGGING_E_ENABLED) {
                Log.e(tag, message, cause);
                write(LEVEL_E, tag,
                        message + '\n' + Log.getStackTraceString(cause));
            }
        }
    }

    private LGLog() {
    }

    private static final class LogMessage {
        public char level;
        public long time;
        public int pid;
        public long tid;
        // public String application;
        public String tag;
        public String text;

        @Override
        public String toString() {
            return "LogMessage [level=" + level + ", time=" + time + ", pid="
                    + pid + ", tid=" + tid + ", tag=" + tag + ", text=" + text
                    + "]";
        }

        public LogMessage(char level, long time, int pid, long tid, String tag,
                          String text) {
            this.level = level;
            this.time = time;
            this.pid = pid;
            this.tid = tid;
            this.tag = tag;
            this.text = text;
        }
    }

    private static final class LogWriterThread extends HandlerThread {
        private static final String TAG = LogWriterThread.class.getSimpleName();

        public static boolean LOGGING__WRITE_FILE_ENABLED = true;
        private static final String LOG_FOLDER = "/liguang/log";
        private static final String LOG_POSTFIX = ".log";

        @SuppressLint("SimpleDateFormat")
        private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
                "MM-dd HH:mm:ss.SSS");
        private Date mDate = new Date();

        private BufferedWriter mWriter;

        public LogWriterThread(String name) {
            super(name);
        }

        private void close() {
            quit();

            if (mWriter != null) {
                try {
                    mWriter.close();
                    mWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void write(LogMessage msg) {
            prepareWriter();

            if (mWriter == null) {
                Log.w(TAG, "Writer is null");
                return;
            }

            CharSequence csq = prepareLog(msg);

            BufferedWriter bufferedWriter = mWriter;
            try {
                bufferedWriter.append(csq);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void prepareWriter() {
            if (mWriter == null) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // /storage/emulated/0/TPWearable/log
                    File folder = new File(
                            Environment.getExternalStorageDirectory()
                                    + LOG_FOLDER);
                    boolean exists = true;
                    if (!folder.exists()) {
                        exists = folder.mkdirs();
                    }

                    if (exists) {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyy-MM-dd HHmmss");
                        Date date = new Date();

                        File logFile = new File(folder, sdf.format(date)
                                + LOG_POSTFIX);
                        if (!logFile.exists()) {
                            try {
                                logFile.createNewFile();
                                mWriter = new BufferedWriter(new FileWriter(
                                        logFile, true));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.w(TAG, "log folder is not exists");
                    }
                } else {
                    Log.w(TAG, "media mouted state is " + state);
                }
            }
        }

        private CharSequence prepareLog(LogMessage msg) {
            mDate.setTime(msg.time);
            String formatString = String.format("%s: %s p(%s:%s) %s: %s",
                    mSimpleDateFormat.format(mDate), msg.level, msg.pid,
                    msg.tid, msg.tag, msg.text);
            return formatString;
        }
    }

    private static final class LogWriterHandler extends Handler {
        private LogWriterThread mLogWriter;
        private static final int MSG_WRITE = 0;
        private static final int MSG_DESTROY = 1;

        public LogWriterHandler(Looper looper, LogWriterThread logWriterThread) {
            super(looper);
            mLogWriter = logWriterThread;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_WRITE) {
                mLogWriter.write((LogMessage) msg.obj);
            } else if (what == MSG_DESTROY) {
                mLogWriter.close();
            }
        }
    }
}

package com.wragony.library.executor;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * @description: 基于线程池的全局任务执行器
 * @author: wragony
 * @email: wragony@163.com
 * @createDate: 2020/8/13
 */
public class AppExecutors {

    private ExecutorService mDiskIO;

    private ExecutorService mNewThread;

    private Executor mMainThread;

    /**
     * cpu核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 核心线程数的区间是[2,4]
     */
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    /**
     * 线程池最大容量
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    /**
     * 当一个线程空闲20秒后就会被取消
     */
    private static final int KEEP_ALIVE_SECONDS = 20;

    private volatile static AppExecutors mCustomInstance;

    private static class SingleTonHoler {

        private static final AppExecutors INSTANCE = new AppExecutors();

    }

    /**
     * 推荐：默认线程池唯一实例获取
     *
     * @return
     */
    public static AppExecutors get() {
        return SingleTonHoler.INSTANCE;
    }

    /**
     * 自定义线程池
     *
     * @param diskIO
     * @param newThread
     * @return
     */
    public static AppExecutors get(ExecutorService diskIO, ExecutorService newThread) {
        if (null == mCustomInstance) {
            synchronized (AppExecutors.class) {
                if (null == mCustomInstance) {
                    mCustomInstance = new AppExecutors(diskIO, newThread, new MainThreadExecutor());
                }
            }
        }
        return mCustomInstance;
    }

    /**
     * 自定义线程池
     *
     * @param diskIO
     * @param newThread
     * @param mainThread
     * @return
     */
    public static AppExecutors get(ExecutorService diskIO, ExecutorService newThread, Executor mainThread) {
        if (null == mCustomInstance) {
            synchronized (AppExecutors.class) {
                if (null == mCustomInstance) {
                    mCustomInstance = new AppExecutors(diskIO, newThread, mainThread);
                }
            }
        }
        return mCustomInstance;
    }

    private AppExecutors(ExecutorService diskIO, ExecutorService newThread, Executor mainThread) {
        this.mDiskIO = diskIO;
        this.mNewThread = newThread;
        this.mMainThread = mainThread;
    }

    private AppExecutors() {
        this(new ThreadPoolExecutor(1, 1,
                        1L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(),
                        new PriorityNamedThreadFactory("exec#io", null)),
                new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                        KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(1024),
                        new PriorityNamedThreadFactory("exec#worker", Process.THREAD_PRIORITY_BACKGROUND),
                        new AbortPolicy()),
                new MainThreadExecutor());
    }

    public synchronized void shutdownNow() {
        try {
            if (mDiskIO != null && !mDiskIO.isShutdown()) {
                mDiskIO.shutdownNow();
            }
            if (mNewThread != null && !mNewThread.isShutdown()) {
                mNewThread.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IO 线程池中执行
     *
     * @param mTask
     */
    public void io(Runnable mTask) {
        if (null == mTask) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        mDiskIO.submit(mTask);
    }

    /**
     * IO 线程池中执行
     *
     * @param mTask
     * @param <Result>
     */
    public <Result> void io(final Worker<Result> mTask) {
        if (null == mTask) {
            mTask.onWorkException(raiseException());
            return;
        }
        Future<Result> submit = mDiskIO.submit(mTask);
        final Result result;
        try {
            result = submit.get();
        } catch (ExecutionException | InterruptedException e) {
            mMainThread.execute(new Runnable() {
                @Override
                public void run() {
                    mTask.onWorkException(e);
                }
            });
            submit.cancel(true);
            return;
        }
        mMainThread.execute(new Runnable() {
            @Override
            public void run() {
                mTask.onWorkExecute(result);
            }
        });
    }

    /**
     * 子线程中执行
     *
     * @param mTask
     */
    public void worker(Runnable mTask) {
        if (null == mTask) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        mNewThread.execute(mTask);
    }

    /**
     * 子线程中执行
     *
     * @param mTask
     * @param <Result>
     */
    public <Result> void worker(final Worker<Result> mTask) {
        if (null == mTask) {
            mTask.onWorkException(raiseException());
            return;
        }
        Future<Result> submit = mNewThread.submit(mTask);
        final Result result;
        try {
            result = submit.get();
        } catch (ExecutionException | InterruptedException e) {
            mMainThread.execute(new Runnable() {
                @Override
                public void run() {
                    mTask.onWorkException(e);
                }
            });
            submit.cancel(true);
            return;
        }
        mMainThread.execute(new Runnable() {
            @Override
            public void run() {
                mTask.onWorkExecute(result);
            }
        });
    }

    /**
     * UI线程中执行
     *
     * @param mTask
     */
    public void ui(final Runnable mTask) {
        if (null == mTask) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        mMainThread.execute(mTask);
    }

    private Exception raiseException() {
        return new IllegalArgumentException("Task cannot be null");
    }

    private static class MainThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }

}

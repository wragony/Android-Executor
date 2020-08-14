package com.wragony.library.executor;

import java.util.concurrent.Callable;

/**
 * @description: 任务封装
 * @author: wragony
 * @email: wragony@163.com
 * @createDate: 2020/8/13
 */
public abstract class Worker<Result> implements Callable<Result> {

    /**
     * 任务的具体执行
     * 所在线程：工作线程
     *
     * @return 执行结果
     */
    public abstract Result doWork();


    /**
     * 任务执行完成
     * 所在线程：ui线程
     *
     * @param result
     */
    public abstract void onWorkExecute(Result result);

    /**
     * 任务执行出现异常
     * 所在线程：ui线程
     */
    public void onWorkException(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public Result call() throws Exception {
        return doWork();
    }

}

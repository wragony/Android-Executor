# Android-Executor

## Features

- **一个基于java.util.concurrent的异步执行器**
- **提供统一的线程管理**
- **支持自定义线程池**
- **工作线程和UI线程自动切换**


## Background

在Android R中将不推荐使用AsyncTask。Android参考建议使用标准的java.util.concurrent类作为替代[（更多在这里） ](https://developer.android.com/reference/android/os/AsyncTask) ，线程池默认按 FIFO 队列实现，可自定义策略；

## Supported SDK

Android API level 14 及以上版本

## Getting Started

- gradle 依赖

```
repositories {
  ...
  jcenter()
}

implementation 'com.wragony.android.executor:executor:1.0.0'
```

- maven 依赖


```
<dependency>
  <groupId>com.wragony.android.executor</groupId>
  <artifactId>executor</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```


- jar 依赖


下载 [the latest JAR](./jars) 


## Examples

更多例子参考 [app](./app/src/main/java/com/wragony/app/executor/MainActivity.java) 

##### 工作线程

工作任务继承自 Worker，在doWoker中进行你要做的耗时操作，执行完自动切换为ui线程 onWorkExecute ，执行出现异常则会回调 onWorkException

```
 AppExecutors.get().worker(new Worker<String>() {
 
    @Override
    public String doWork() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());
    }

    @Override
    public void onWorkExecute(String s) {
        appendConsole(s);
    }

    @Override
    public void onWorkException(Throwable e) {
        super.onWorkException(e);
    }
    
});
```

不关心结果的任务


```
AppExecutors.get().worker(new Runnable() {
   
    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final String msg = String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());
        Log.d(TAG, msg);
    }
    
});
```


##### IO线程


```
 AppExecutors.get().io(new Worker<String>() {
    
    @Override
    public String doWork() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());
    }

    @Override
    public void onWorkExecute(String s) {
        appendConsole(s);
    }

    @Override
    public void onWorkException(Throwable e) {
        super.onWorkException(e);
    }
    
});

```

不关心执行结果

```
AppExecutors.get().io(new Runnable() {
    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final String msg = String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());
         Log.d(TAG, msg);
    }
});
```


##### 主线程


```
 AppExecutors.get().ui(new Runnable() {
    
    @Override
    public void run() {
        final String msg = String.format("线程名:%s", Thread.currentThread().getName());
        appendConsole(msg);
    }
    
});
```

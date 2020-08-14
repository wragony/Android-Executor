package com.wragony.app.executor;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wragony.library.executor.AppExecutors;
import com.wragony.library.executor.Worker;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * @description: Demo
 * @author: wragony
 * @email: wragony@163.com
 * @createDate: 2020/8/14
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView tvConsole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvConsole = findViewById(R.id.tv_console);
        tvConsole.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_worker1:
                testOnWorkerThread(1000, true);
                break;
            case R.id.btn_worker2:
                testOnWorkerThread(1000, false);
                break;
            case R.id.btn_io1:
                testOnIoThread(500, true);
                break;
            case R.id.btn_io2:
                testOnIoThread(500, false);
                break;
            case R.id.btn_main:
                testOnMainThread();
                break;
            case R.id.btn_custom:
                testCustomThread(1000);
                break;
            default:
                break;
        }
    }

    private void testCustomThread(final int time) {
        AppExecutors executors = AppExecutors.get(new ThreadPoolExecutor(1, 1,
                        1L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>()),
                new ThreadPoolExecutor(3, 5,
                        2, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(1024), new AbortPolicy()));
        executors.worker(new Worker<String>() {
            @Override
            public String doWork() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //模拟异常
//                System.out.println(1 / 0);
                return String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());
            }

            @Override
            public void onWorkExecute(String s) {
                appendConsole(s);
            }

            @Override
            public void onWorkException(Throwable e) {
                super.onWorkException(e);
                appendConsole(e.getMessage());
            }

        });
    }

    private void testOnWorkerThread(final int time, boolean result) {
        if (result) {
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
        } else {
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

                    AppExecutors.get().ui(new Runnable() {
                        @Override
                        public void run() {
                            appendConsole(msg);
                        }
                    });
                }
            });
        }
    }

    private void testOnIoThread(final int time, boolean result) {
        if (result) {
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
        } else {
            AppExecutors.get().io(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final String msg = String.format("模拟耗时:%s---线程名:%s", time, Thread.currentThread().getName());

                    AppExecutors.get().ui(new Runnable() {
                        @Override
                        public void run() {
                            appendConsole(msg);
                        }
                    });
                }
            });
        }
    }

    private void testOnMainThread() {
        AppExecutors.get().ui(new Runnable() {
            @Override
            public void run() {
                final String msg = String.format("线程名:%s", Thread.currentThread().getName());
                appendConsole(msg);
            }
        });
    }

    private void appendConsole(CharSequence text) {
        tvConsole.append(text + "\n");
    }

}
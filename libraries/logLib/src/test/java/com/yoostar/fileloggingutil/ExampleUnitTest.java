package com.yoostar.fileloggingutil;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        File file = new File("D:\\UtilsLibs\\CollectLogUtil\\LoggingUtil\\src\\test\\java\\com\\yoostar\\fileloggingutil\\ijkLog_0.log");
        ZipFileHelper.executeZipLogFile(file, new ZipFileHelper.IZipObserver() {
            @Override
            public void onResult(File zipFile) {
                System.out.println("zipFile=>" + zipFile);
            }
        });
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void addition_isCorrect2() throws Exception {
        assertEquals(4, 2 + 2);
        File file = new File("D:\\UtilsLibs\\CollectLogUtil\\LoggingUtil\\src\\test\\java\\com\\yoostar\\fileloggingutil\\CollectFile.zip");
        FileUtils.unZipFile(file);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTryCatch() {
        while (true) {
            try {
                System.out.println("testTryCatch");
                throw new Exception("我挂了");
            } catch (Exception e) {
                if (!isSolve()) {
                    return;
                }
            }
        }
    }

    int solveTime = 0;

    public boolean isSolve() {
        solveTime++;
        if (solveTime > 10) {
            return false;
        }
        return true;
    }

    @Test
    public void a() {
        int print = 1;
        while (true) {
            try {
                while (true) {
               /* try {
                    System.out.println("xixix");
                    throw new Exception("出错了");
                } catch (Exception e) {
                    System.out.println("deal it" + e.getMessage());
                }*/

                    try {
                        System.out.println("哈哈哈");
                        print++;
                        if (print > 10) {

                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("deal it" + e.getMessage());
                    }
                }
            } catch (Exception e) {

            }
        }
           /* print++;
            while (print > 1000) {
                System.out.println("sdf");
                print = 0;
            }
            while (print == 0) {
                System.out.println("000");
                try {
                    throw new Exception("出错了");
                } catch (Exception e) {
                    System.out.println("deal it");
                }
                print++;
            }*/
    }

    @Test
    public void testExecutors() {
        final ExecutorService executors = Executors.newSingleThreadExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("hhh");
            }
        };
        executors.execute(runnable);
        executors.shutdown();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("开始监听");
                    /*if (executors.isTerminated()) {
                        System.out.println("它停止了");
                        return;
                    }*/
                    System.out.println(executors.isTerminated());

                }
            }
        });
    }
}
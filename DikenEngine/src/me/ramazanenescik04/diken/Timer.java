
package me.ramazanenescik04.diken;

import java.util.concurrent.TimeUnit;

/**
 * Represents the Timer type within the DikenEngine core package.
 */
public class Timer {
    private TimerRunnable runnable;

    public long currentTime, maxTime;

    private volatile boolean isRunnable = false;
    private Thread timerThread;

    public Timer(TimerRunnable r, long cT, long mT) {
        this.runnable = r;
        this.currentTime = cT;
        this.maxTime = mT;
    }

    public synchronized void start() {
        if (isRunnable) {
            return;
        }

        isRunnable = true;

        timerThread = Thread.startVirtualThread(() -> {
            long startTime = System.nanoTime();
            long duration = TimeUnit.MILLISECONDS.toNanos(maxTime);
            long endTime = startTime + duration;

            try {
                while (isRunnable) {
                    long now = System.nanoTime();

                    if (now >= endTime) {
                        break;
                    }

                    currentTime = TimeUnit.NANOSECONDS.toMillis(now - startTime);

                    if (runnable != null) {
                        runnable.run(currentTime, maxTime,
                                TimeUnit.NANOSECONDS.toMillis(startTime),
                                TimeUnit.NANOSECONDS.toMillis(endTime), false);
                    }

                    Thread.sleep(10);
                }

                if (isRunnable && runnable != null) {
                    currentTime = maxTime;
                    runnable.run(currentTime, maxTime,
                            TimeUnit.NANOSECONDS.toMillis(startTime),
                            TimeUnit.NANOSECONDS.toMillis(endTime), true);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                isRunnable = false;
            }
        });
    }

    public synchronized void stop() {
        isRunnable = false;

        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }

    public boolean isRunning() {
        return isRunnable;
    }

    public double getProgress() {
        return maxTime <= 0 ? 1.0 : (double) currentTime / maxTime;
    }
    
    public static Timer delay(Runnable runnable, long delay) {
        Timer timer = new Timer((_, _, _, _, finished) -> {
            if (finished) {
                runnable.run();
            }
        }, 0, delay);

        timer.start();
        return timer;
    }

    public interface TimerRunnable {
        void run(long currentTime, long maxTime,
                 long startTime, long endTime, boolean isFinished);
    }
}

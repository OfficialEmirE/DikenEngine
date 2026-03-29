package me.ramazanenescik04.diken;

/**
 * Represents the `Timer` type within the DikenEngine `core` package.
 */
public class Timer {
    private TimerRunnable runnable;
    public long currentTime, maxTime;
    private boolean isRunnable = false;
    private Thread timerThread;

    /**
     * 
     * @param r Runnable to be executed on each tick of the timer
     * @param cT Current time in milliseconds
     * @param mT Maximum time in milliseconds
     */
    public Timer(TimerRunnable r, long cT, long mT) {
        this.runnable = r;
        this.currentTime = cT;
        this.maxTime = mT;
    }

    public void start() {
        if (isRunnable) {
            return;
        }

        isRunnable = true;

        timerThread = new Thread(() -> {
            long startTime = System.currentTimeMillis(); // Başlangıç zamanını al
            long endTime = startTime + maxTime;

            while (isRunnable && System.currentTimeMillis() < endTime) {
                currentTime = System.currentTimeMillis() - startTime;
                
                if (runnable != null) {
                    runnable.run(currentTime, maxTime, startTime, endTime, false);
                }
                
                try {
                    Thread.sleep(10); // CPU yükünü azaltmak için küçük bir gecikme
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Son kez çalıştır (tamamlanma zamanında)
            if (isRunnable && runnable != null) {
                currentTime = maxTime;
                runnable.run(currentTime, maxTime, startTime, endTime, true);
            }
            
            isRunnable = false;
        });
        
        timerThread.start();
    }

    public void stop() {
        isRunnable = false;
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }
    
    public boolean isRunning() {
        return isRunnable;
    }
    
    public double getProgress() {
        return (double) currentTime / maxTime;
    }
    
    public static interface TimerRunnable {
    	/**
    	 * 
    	 * Bu metot, zamanlayıcı durana kadar her 10 milisaniyede bir çağrılır.
    	 * 
    	 * @param currentTime Geçerli zaman milis olarak alır (Gerçek zaman milis'i eklemez)
    	 * @param maxTime Kaç milis sonra bitmesi gerektiğini söyler (Gerçek zaman milis'i eklemez)
    	 * @param startTime Başlangıç zamanını milis olarak alır
    	 * @param endTime Gerçek zaman milis ile maxTime'i birleştirir
    	 * @param isFinished Bitip bitmediğini söyler
    	 */
    	public abstract void run(long currentTime, long maxTime, long startTime, long endTime, boolean isFinished);
    }
}

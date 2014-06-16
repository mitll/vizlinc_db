package edu.mit.ll.vizlincdb.util;

/**
 * Simple class for measuring and printing elapsed wall-clock time.
 */
public class ElapsedTime {
    
    private long start;
    
    public ElapsedTime() {
        reset();
    }
    
    public final void reset() {
        start = System.nanoTime();
    }
    public float secondsElapsed() {
        return (System.nanoTime() - start) / 1e9f;
    }
    
    public void done(String label) {
        System.out.println(label + ": " + String.format("%.2f", secondsElapsed())); 
        reset();
    }
    
}

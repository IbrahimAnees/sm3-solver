package solution;

public class Idle {
    private int idleTime;
    private Task from;
    private Task to;
    private int processor;

    public Idle(int idleTime, Task from, Task to, int processor) {
        this.idleTime = idleTime;
        this.from = from;
        this.to = to;
        this.processor = processor;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public Task getFrom() {
        return from;
    }

    public Task getTo() {
        return to;
    }

    public int getProcessor() {
        return processor;
    }
}

package pyre.manager;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExplosionWorkScheduler {
    private final Map<String, Runnable> endOfTickTasks = new LinkedHashMap<>();

    // Pyre only schedules its own cleanup and reporting work here. Vanilla explosion logic
    // still runs synchronously in-place, so gameplay timing stays untouched.
    public void scheduleOnce(String taskKey, Runnable task) {
        this.endOfTickTasks.putIfAbsent(taskKey, task);
    }

    public void flushEndOfTick() {
        if (this.endOfTickTasks.isEmpty()) {
            return;
        }

        for (Runnable task : this.endOfTickTasks.values()) {
            task.run();
        }

        this.endOfTickTasks.clear();
    }

    public void clear() {
        this.endOfTickTasks.clear();
    }
}

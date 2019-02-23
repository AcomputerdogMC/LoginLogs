package net.acomputerdog.loginlogs.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class AsyncTask<T> implements Runnable {
    private final Plugin plugin;
    private final AsyncBlock<T> task;
    private final SyncBlock<T> callback;

    private boolean fail = false;
    private Exception exception;
    private T result;

    public AsyncTask(Plugin plugin, AsyncBlock<T> task, SyncBlock<T> callback) {
        this.plugin = plugin;
        this.task = task;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            task.run(this);
        } catch (Exception e) {
            this.fail = true;
            this.exception = e;
        }

        Bukkit.getScheduler().runTask(plugin, () -> callback.run(this));
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public interface AsyncBlock<T> {
        void run(AsyncTask<T> task) throws Exception;
    }

    public interface SyncBlock<T> {
        void run(AsyncTask<T> task);
    }

}

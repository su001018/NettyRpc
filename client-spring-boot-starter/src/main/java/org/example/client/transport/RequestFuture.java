package org.example.client.transport;



import java.util.concurrent.*;

public class RequestFuture<V> implements Future<V> {
    private CountDownLatch countDownLatch=new CountDownLatch(1);
    private V result=null;
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return result!=null;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout,unit);
        return result;
    }

    public void setResult(V result){
        this.result=result;
        countDownLatch.countDown();
    }
}

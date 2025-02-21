public class BlockingMailbox<T> {
    private T mail;

    public BlockingMailbox() {
        mail = null;
    }

    public synchronized void put(T o) {
        while (!isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        mail = o;
        notifyAll();
    }

    public synchronized T get() {
        while (isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        T ret = mail;
        mail = null;
        notifyAll();
        return ret;
    }

    public synchronized boolean isEmpty() {
        return mail == null;
    }
}
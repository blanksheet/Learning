/**
 * @Author: tianze
 * @Date: 2019/3/13 22:38
 */
public class Learn3 {
    public static void main(String[] args) {
        Resource resource = new Resource();
        //生产者线程
        ProducerThread p1 = new ProducerThread(resource);
        ProducerThread p2 = new ProducerThread(resource);
        ProducerThread p3 = new ProducerThread(resource);
        //消费者线程
        ConsumerThread c1 = new ConsumerThread(resource);
        ConsumerThread c2 = new ConsumerThread(resource);
        ConsumerThread c3 = new ConsumerThread(resource);

        p1.start();
        p2.start();
        p3.start();
        c1.start();
        //c2.start();
        //c3.start();
    }
}

/**
 * 资源类 加锁这里易于修改
 */
class Resource{
    private int num = 0;
    private int size = 10;

    public synchronized void remove(){
        if(num > 0){
            num--;
            System.out.println("消费者" + Thread.currentThread().getName() +
                    "消耗一件资源，" + "当前线程池有" + num + "个");
            notifyAll();
        }
        else {
            try{
                wait();
                System.out.println("消费者" + Thread.currentThread().getName() + "线程进入等待状态");

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void add(){
        if(num < size){
            num++;
            System.out.println(Thread.currentThread().getName() + "生产一件资源，当前资源池有" + num + "个");
            notifyAll();
        }
        else {
            try{
                wait();
                System.out.println(Thread.currentThread().getName()+"线程进入等待");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

/**
 * 消费者线程
 */

class ConsumerThread extends Thread{
    private Resource resource;
    public ConsumerThread(Resource resource){
        this.resource = resource;
    }
    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resource.remove();
        }
    }
}
/**
 * 生产者线程
 */
class ProducerThread extends Thread {
    private Resource resource;

    public ProducerThread(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resource.add();
        }
    }
}




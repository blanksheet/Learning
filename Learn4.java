import java.util.ArrayList;
import java.util.List;

/**
 * @Author: tianze
 * @Date: 2019/3/15 22:37
 */
public class Learn4 {
    public static void main(String[] args){
       Store store = new Store();

       Consumer consumer = new Consumer(store);
       Producer producer = new Producer(store);

       consumer.start();
       producer.start();

       try {
           consumer.join();
           producer.join();
       }catch (InterruptedException e){
           e.printStackTrace();
       }
    }
}

/**
 * 资源类 将锁加至这里易于修改
 */
class Store{
    private List<String> goods = new ArrayList<String>();

    public void put(String one) throws InterruptedException {
        synchronized (goods){
            try {
                while (goods.size() > 10){
                    wait();
                }
                goods.add(one);
            }
            finally {
                notifyAll();
            }
        }
    }

    public String remove() throws InterruptedException{
        synchronized (goods){
            try {
                while (goods.size() <= 0){
                    wait();
                }
                return goods.remove(0);
            }
            finally {
                notifyAll();
            }
        }
    }
}

/**
 * 消费者线程
 */
class  Consumer extends Thread{
    private Store store;

    public Consumer(Store store){
        this.store = store;
    }

    @Override
    public void run(){
        System.out.println("start consumer goods");
        try {
            //Thread.sleep(1000);
            String one = store.remove();
            System.out.println("finish consume " + one);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}

/**
 * 生产者线程
 */
class Producer extends Thread{
    private Store store;

    public Producer(Store store){
        this.store = store;
    }

    @Override
    public void run(){
        for(int i = 0; i < 100; i++){
            System.out.println("start produce " + i);
            try {
                //Thread.sleep(1000);
                store.put("goods" + i);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("finish produce " + i);
        }
    }
}
import java.util.HashMap;

/**
 * @Author: tianze
 * @Date: 2019/3/9 17:15
 */
public class Learn2 {
    class Node{
        Node pre, next;
        int key;
        int value;
        public Node(int key, int value){
            this.key = key;
            this.value = value;
        }
    }

    private int capacity;
    private int num;
    private HashMap<Integer, Node> map;
    private Node first, last;   //first为队头 为删除的第一个元素 last为队尾 最后删除的元素

    public void LRUCache(int capacity) {
        this.capacity = capacity;
        num = 0;
        map = new HashMap<Integer, Node>();
        first = null;
        last = null;
    }

    public int get(int key) {
        Node node = map.get(key);

        //该密钥不存在
        if(node == null){
            return -1;
        }
        else if(node != last){
            if(node == first){  //获取节点为当前下一个被删除的节点
                first = first.next;
            }
            else {              //获取节点为队列中节点
                node.pre.next = node.next;
            }

            node.next.pre = node.pre;   //修改反向队列顺序 形成删除节点
            last.next = node;    //将改节点插入last
            node.pre = last;
            node.next = null;
            last = node;
        }

        return node.value;
    }

    public void put(int key, int value) {
        Node node = map.get(key);

        if(node == null){
            Node newNode = new Node(key,value);
            if(num >= capacity){   //超出资源 删除节点
                map.remove(first.key);   //移除第一个元素
                first = first.next;
                if(first != null){
                    first.pre = null;
                }
                else {
                    last = null;
                }
                num--;
            }

            if(first == null || last == null){
                first = newNode;
            }
            else {
                last.next = newNode;
            }

            newNode.pre = last;
            last = newNode;      //更新last
            map.put(key, newNode);
            num++;
        }
        else {
            node.value = value;
            if(node != last){
                if(node == first){
                    first = first.next;
                }
                else {
                    node.pre.next = node.next;
                }

                node.next.pre = node.pre;
                last.next = node;
                node.pre = last;
                node.next = null;
                last = node;
            }
        }
    }
}

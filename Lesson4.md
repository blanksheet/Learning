```java
    public static void main(String[] args){
        HashMap hashMap = new HashMap();
    }
```
数据结构采用Node<K,V>[],是一个链表结构的对象  
JDK10采用的数组+链表+红黑树的结构
```java
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        HashMap.Node<K,V> next;

        Node(int hash, K key, V value, HashMap.Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
```
静态变量  
DEFAULT_INITIAL_CAPACITY 初始容量 2^4 = 16  
MAXIMUM_CAPACITY 哈希表最大容量 2^30  
DEFAULT_LOAD_FACTOR 装载因子 默认0.75 控制容量和查找时间的权衡，如hash函数很好，可以设置高；设置低，容易浪费空间  
TREEIFY_THRESHOLD 箱子中链表长度大于8，转换成红黑树   
UNTREEIFY_THRESHOLD  箱子中链表长度小于6，红黑树退化为链表  
MIN_TREEIFY_CAPACITY  转换成树之前进行判断，必须键值对总数大于64，避免在哈希表建立初期多个键值对恰好放在一个链表中而导致    没有必要的转换
```java
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
static final int MAXIMUM_CAPACITY = 1 << 30;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
static final int TREEIFY_THRESHOLD = 8;
static final int UNTREEIFY_THRESHOLD = 6;
static final int MIN_TREEIFY_CAPACITY = 64;
```
HashMap定位数组索引的位置，直接决定hash方法的离散性能。希望HashMap里面每个元素尽量分布的均匀一些，hash方法得到的值尽量不同
在这里 先拿到Key的hashcode 然后取高16位参与运算  
h = key.hashCode() ^ (h >>> 16)  
在getNode方法内 获取数组索引继续进行运算  
first = tab[(n - 1) & hash  
这一步 相当于对hash取模运算，因取模运算消耗很大，而计算机做位运算较快  
基于公式 x mod 2^n = x & (2^n - 1) HashMap底层数组的长度是2^n，所以是tab[(n - 1) & hash    

```java
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }
  
final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```

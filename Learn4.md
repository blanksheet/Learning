**HashMap源码分析**  
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
自这里用hashCode与其高16位进行异或运算 主要是为了在table的length较小时，让高位参与运算 并不会有很大的开销   
在getNode方法内 获取数组索引继续进行运算  
first = tab[(n - 1) & hash  
这一步 相当于对hash取模运算，因取模运算消耗很大，而计算机做位运算较快  
基于公式 x mod 2^n = x & (2^n - 1) HashMap底层数组的长度是2^n，所以是tab[(n - 1) & hash    

```java
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
  
    first = tab[(n - 1) & hash  
```
get 方法详解  
1.判读 table不为空 && table.length 大于0  
2.使用 table.length - 1 与 hash 相与求模计算出来table的index 将该index对应的节点赋值给first 并校验非空  
3.检查first的 hash 和 key 和入参是否相同 相同则返回first  
4.如 first.next 不为空，则继续向下遍历  
5.如 fist 是 TreeNode 节点，调用 getTreeNode 方法  
6.如 first 是链表，则一直遍历链表到找到节点并返回  
7.未找到该节点 返回 null

```java
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
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

getTreeNode 方法详解  
1.找到调用该方法的父节点 如该节点为空 则为root节点 如非空 调用该父节点的find方法  

```java
    final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
    }
  
```
find 方法详解  
1.将调用该方法的节点赋值为p  
2.如传入的hash值小于p点的hash值 则向左子树遍历  
3.如传入的hash值大于p点的hash值，则向右子树遍历
4.如传入的key值等于p点的key值或者 或者key对象非空 equals相等 则返回该节点  
5.p的左节点为空 则向右子树遍历  
6.p的右节点为空 则向左子树遍历  
7.如key所属的类实现了comparable 则将dir赋值为传入的key和节点的key进行比较，如dir小于0 则代表k < pk 向p的左子树遍历 反之向右子树  
8.递归调用右子树查找 如结果非空 返回q  
9.前面查找失败 向左子树遍历  
10.查找失败 返回null

```java
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }
```
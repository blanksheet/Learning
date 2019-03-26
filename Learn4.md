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
put 方法详解  
1.table为空或者table.length等于0，这样将调用resize操作初始化  
2.如果计算出来该key对应table的index为null 则将该key-value存入该节点  
3.如该table的index非空，说明该点已经有data 则将该data插入链表或者红黑树  
4.如相等，则将p点赋值给e点  
5.如果p点是红黑树节点，则调用putTreeVal函数插入  
6.走到这里就是链表入口，遍历链表，并插入到链表末端  
7.判断链表长度是否大于TREEIFY_THRESHOLD - 1,大于执行treeifyBin转化为红黑树  
8.将p节点赋值给e点
9.如果e非空，则说明查找到了节点 将新节点的value赋值给旧节点 返回就节点的value  
10.插入节点后超出阈值，则调用resize方法扩容  
11.这里的afterNodeAccess(e);afterNodeInsertion(evict);都是LinkedHashMap的方法  
12.transient int size;transient int modCount; 这里transient关键字修饰是为了反序列化 元素本身无意义或者容易复现 为了节省空间

```java
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    } 
```
putTreeVal 方法详解  
1.如parent非空，调用root函数计算root，反之说明树为空，指向当前节点 换而言之，索引位置的根节点不是红黑树的根节点  
2.如传入的hash值小于p点的hash，置dir为-1 代表向左查询  
3.如传入的hash值大于p点的hash，置dir为1 代表向右查询  
4.如当前节点与节点p相等 返回p节点  
5.如k所属类没有实现comparable接口或者k和节点p的key相等，第一次会从p节点的左子树和右子树分别调用find方法，如找到返回节点q 如不是第一次查找，调用tieBreakOrder方法赋值给dir 根据dir正负决定查找方向  
6.如dir为负数，则向p.left查找，如dir为正数，则向p.right查找。进行下一次循环  
7.如当前节点查找后为空，代表当前位置为红黑树插入位置  
8.保存xp为当前查找最后一个节点 保存xpn为xp的next节点（这里是链表关系）  根据dir的值决定存储在xp节点的左节点或右节点 同时维护链表结构 将xp.next赋值为x 将x的prev和parent设置为xp 如xpn非空则会这里需要设置xpn的prev为x 维护链表结构  
9.进行红黑树的插入平衡调整 moveRootToFront(tab, balanceInsertion(root, x));  
注：xp节点在此处可能是叶子节点、没有左节点的节点、没有右节点的节点三种情况，即使它是叶子节点，它也可能有next节点，红黑树的结构跟链表的结构是互不影响的，不会因为某个节点是叶子节点就说它没有next节点，红黑树在进行操作时会同时维护红黑树结构和链表结构，next属性就是用来维护链表结构的

```java
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

```
tieBreakOrder方法详解  
当hashcode相等且没有实现comparable接口时，提供一个一致性插入规则维护重定位的等价性
```java
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }
```
treeifyBin方法详解   
1.如果table为空或者table.length小于64，调用resize方法进行扩容   
2.根据hash计算索引值，并遍历该索引上的全部链表   
3.调用replacementTreeNode方法(return new TreeNode<>(p.hash, p.key, p.value, next);)将该节点转化成红黑树节点   
4.将头节点赋值给hd，对之后遍历的节点进行链表操作(p.prev = tl;tl.next = p;)   
5.将树的头节点hd赋值给链表的索引位置上，如非空，调用treeify构建红黑树
```java
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

```

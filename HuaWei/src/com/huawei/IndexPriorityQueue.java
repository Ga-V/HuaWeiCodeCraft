package com.huawei;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author Ga_Vin
 * @create 2019-03-10-12:36
 */
public class IndexPriorityQueue<Key extends Comparable<Key>> {
    private int N;
    private int[] pq; // 索引二叉堆,从1开始
    private int[] qp; // 逆序,满足qp[pq[i]] = pq[qp[i]] = i
    private Key[] keys;

    public IndexPriorityQueue(int maxN) {
        // 可存放范围为[0, maxN]
        keys = (Key[]) new Comparable[maxN + 1];
        // 索引二叉堆,存放范围为[1, maxN]
        pq = new int[maxN + 1];
        // 可存放范围为[0, maxN]
        qp = new int[maxN + 1];
        // 刚开始没有关联任何整数，都设置为-1
        Arrays.fill(qp, -1);
    }

    // 针对是pq中的索引i、j，但是实际引用的是keys中对应的元素
    private boolean greater(int i, int j) {
        return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
    }

    public boolean isEmpty() {
        return N == 0;
    }

    public int size() {
        return N;
    }

    public boolean contains(int k) {
        return qp[k] != -1;
    }

    public void insert(int k, Key key) {
        if (!contains(k)) {
            N++;
            pq[N] = k;
            qp[k] = N;
            keys[k] = key;
            swim(N);
        }
    }

    // 给整数k重新关联一个对象
    public void replace(int k, Key key) {
        keys[k] = key;
        // 由于和k关联的新key可能大于原来的key（此时需要下沉），也有可能小于原来的key（此时需要上浮），为了简化代码，既上浮又下沉，就囊括了这两种可能。
        swim(qp[k]);
        sink(qp[k]);
    }

    // 返回最小元素
    public Key min() {
        return keys[pq[1]];
    }

    // 最小元素的关联整数
    public int minIndex() {
        return pq[1];
    }

    public int delMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("队列已经为空，不能执行删除！");
        }
        int indexOfMin = pq[1];
        // 堆顶和最后一个元素交换
        swap(1, N--);
        sink(1);
        // 最后一个元素置为空
        keys[indexOfMin] = null;
        // 同时关联整数pq[N]在pq中的的索引设置为-1，表示还没有对象与该整数关联
        qp[indexOfMin] = -1;

        return indexOfMin;
    }

    public void delete(int k) {
        if (!contains(k)) {
            throw new NoSuchElementException("没有元素与" + k + "关联！");
        }
        // index为整数k在pq中的位置
        int index = qp[k];

        swap(index, N--);
        // 这里一定要先上浮下沉后再将元素置空，因为swim方法没有N的限制，在没有交换元素的情况下，即删除的就是pq中最后一个元素，如果先置空, 会在greater方法中引发空指针
        // 而sink方法有N的限制，先置空后置空都没有影响，2k <= N会限制它进入循环，避免了空指针
        swim(index);
        sink(index);
        keys[k] = null;
        qp[k] = -1;
    }

    public Key keyOf(int k) {
        if (contains(k)) {
            return keys[k];
        }
        // 没有与k关联就返回null
        return null;
    }

    private void swap(int i, int j) {
        int temp = pq[i];
        pq[i] = pq[j];
        pq[j] = temp;
        // 还要更新qp
        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }

    private void swim(int k) {
        // k = 1说明当前元素浮到了根结点，它没有父结点可以比较，也不能上浮了，所以k <= 1时候推出循环
        while (k > 1 && greater(k / 2, k)) {
            swap(k / 2, k);
            // 上浮后，成为父结点，所以下标变成原父结点
            k = k / 2;
        }
    }

    private void sink(int k) {
        // 父结点的位置k最大值为 N/2,若k有左子结点无右子结点，那么2k = N；若两个子结点都有，那么2k + 1 = N
        // 有可能位置k只有左子结点，依然要比较，用2k + 1 <= N这个的条件不会执行比较，所以用2k <= N条件
        while (2 * k <= N) {
            int j = 2 * k;
            // 可以取j = N -1,greater(N -1, N);由于下标从1开始，所以pq[N]是有元素的
            if (j < N && greater(j, j + 1)) {
                // 右子结点比左子结点大 ，取右子结点的下标
                j++;
            }
            // 左子结点或者右子结点和父结点比较
            // 如果pq[k] >= pq[j]，即父结点大于等于较大子结点时，停止下沉
            if (!greater(k, j)) {
                break;
            }
            // 否则交换
            swap(k, j);
            // 下沉后，下标变成与之交换的元素下标
            k = j;
        }
    }
}
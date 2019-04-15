package com.huawei;

/**
 * 路口类
 *
 * @author Ga_Vin
 * @create 2019-03-11-18:41
 */
public class Node implements Comparable<Node> {
    private int id;
    private Road[] roadsToCurrentNode;//从前一节点指向当前节点的道路数组
    private Road[] roadsFromCurrentNode;//从当前节点指向下一节点的道路数组

    /**
     * 构造器
     */
    public Node() {
        this.roadsToCurrentNode = new Road[4];//顺时针的道路数组
        this.roadsFromCurrentNode = new Road[4];//顺时针的道路数组
    }

    /**
     * 构造器
     *
     * @param id
     */
    public Node(int id) {
        this.id = id;
        this.roadsToCurrentNode = new Road[4];//顺时针的道路数组
        this.roadsFromCurrentNode = new Road[4];//顺时针的道路数组
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 添加到达当前节点的道路到roadsToCurrentNode数组中
     *
     * @param road
     * @param indexInNode
     */
    public void setRoadToCurrentNode(Road road, int indexInNode) {
        this.roadsToCurrentNode[indexInNode] = road;
    }

    /**
     * 添加从当前节点出发的道路到roadsFromCurrentNode数组中
     *
     * @param road
     * @param indexInNode
     */
    public void setRoadFromCurrentNode(Road road, int indexInNode) {
        this.roadsFromCurrentNode[indexInNode] = road;
    }

    /**
     * 返回当前节点的roadsToCurrentNode数组
     *
     * @return
     */
    public Road[] roadsToCurrentNode() {
        return roadsToCurrentNode;
    }

    /**
     * 返回当前节点的roadsFromCurrentNode数组
     *
     * @return
     */
    public Road[] getRoadsFromCurrentNode() {
        return roadsFromCurrentNode;
    }

    /**
     * 返回指定道路roadToCurrentNode
     *
     * @param p
     * @return
     */
    public Road roadToCurrentNode(int p) {
        return roadsToCurrentNode[p];
    }

    /**
     * 返回指定道路roadFromCurrentNode
     *
     * @param p
     * @return
     */
    public Road roadFromCurrentNode(int p) {
        return roadsFromCurrentNode[p];
    }

    /**
     * 返回到达此节点道路在roadsToCurrentNode数组中的index
     *
     * @param road
     * @return
     */
    public Integer roadToIndex(Road road) {
        if (road == null) return -1;
        for (int i = 0; i < 4; i++) {
            if (roadsToCurrentNode[i] != null && road != null && roadsToCurrentNode[i].id() == road.id()) {
                return i;
            }
        }
        System.out.println("寻找roadToIndex失败！");
        return -1;
    }

    /**
     * 返回到达此节点道路在roadsFromCurrentNode数组中的index
     *
     * @param road
     * @return
     */
    public Integer roadFromIndex(Road road) {
        if (road == null) return -1;
        for (int i = 0; i < 4; i++) {
            if (roadsFromCurrentNode[i] != null && road != null && roadsFromCurrentNode[i].id() == road.id()) {
                return i;
            }
        }
        System.out.println("寻找roadFromIndex失败！");
        return -1;
    }

    @Override
    public int compareTo(Node node) {
        return this.id - node.id;
    }
}

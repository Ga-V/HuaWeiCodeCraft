package com.huawei;
import java.util.ArrayList;

/**
 * 道路网络图
 *
 * @author Ga_Vin
 * @create 2019-03-09-17:39
 */
public class Graph implements Cloneable {

    private final int nodeNum;
    private int roadNum;
    private int roadCapacity;
    private boolean isLocked;
    private ArrayList<Node> nodes;

    /**
     * 构造器
     *
     * @param nodeNum
     */
    public Graph(int nodeNum) {
        this.nodeNum = nodeNum;
        this.roadNum = 0;
        this.isLocked = false;
        this.roadCapacity = 0;
        nodes = new ArrayList<>(nodeNum);
        for (int i = 0; i < nodeNum; i++) {
            nodes.add(new Node());
        }
    }

    public int nodeNum() {
        return nodeNum;
    }

    public int roadNum() {
        return roadNum;
    }

    public int roadCapacity() {
        return roadCapacity;
    }

    public ArrayList<Node> nodes() {
        return nodes;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    /**
     * 添加到节点的道路，p为道路在node的顺时针位置
     *
     * @param road
     * @param nodeIndex
     * @param roadIndexInNode
     */
    public void addRoadToCurrentNode(Road road, int nodeIndex, int roadIndexInNode) {
        nodes.get(nodeIndex).setRoadToCurrentNode(road, roadIndexInNode);
        roadCapacity += road.channel() * road.length();
        roadNum++;
    }

    /**
     * 添加从节点出发的道路，p为道路在node的顺时针位置
     *
     * @param road
     * @param indexInNode
     */
    public void addRoadFromCurrentNode(Road road,int nodeIndex, int indexInNode) {
        nodes.get(nodeIndex).setRoadFromCurrentNode(road, indexInNode);
    }

    /**
     * 返回指定位置的节点
     *
     * @param index
     * @return
     */
    public Node getNodeByIndex(int index) {
        return nodes.get(index);
    }

    /**
     * 返回指定id的节点
     * @param id
     * @return
     */
    public Node getNodeById(int id) {
        return nodes.get(findIndexOfNode(id));
    }

    /**
     * 返回所有的roadToCurrentNode道路
     *
     * @return
     */
    public ArrayList<Road> allRoads() {
        ArrayList<Road> roads = new ArrayList<>();
        for (Node node : nodes) {
            for (int i = 0; i < 4; i++) {
                if (node.roadToCurrentNode(i) != null) {
                    roads.add(node.roadToCurrentNode(i));
                }
            }
        }
        return roads;
    }

    /**
     * 寻找节点ID为id的节点在nodes中的index
     * @param id
     * @return
     */
    public  int findIndexOfNode(int id) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.id() == id) {
                return i;
            }
        }
        System.out.println("寻找节点的index失败！");
        return -1;
    }

    @Override
    public Object clone() {
        Graph graph = null;
        try{
            graph = (Graph) super.clone();
            graph.nodes = new ArrayList<>(nodeNum);
            for (int i = 0; i < nodeNum; i++) {
                graph.nodes.add(new Node(getNodeByIndex(i).id()));
            }
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = 0; j < 4; j++) {
                    if (getNodeByIndex(i).roadToCurrentNode(j) != null) {
                        //复制roadToCurrentNode
                        Road roadTo =(Road) getNodeByIndex(i).roadToCurrentNode(j).clone();
                        graph.getNodeByIndex(i).setRoadToCurrentNode(roadTo,j);
                        graph.getNodeById(roadTo.from()).setRoadFromCurrentNode(roadTo, getNodeById(roadTo.from()).roadFromIndex(roadTo));
                        if (roadTo.isDuplex() == 1) {
                            Road roadFrom =(Road) getNodeByIndex(i).roadFromCurrentNode(j).clone();
                            graph.getNodeByIndex(i).setRoadFromCurrentNode(roadFrom,j);
                            graph.getNodeById(roadFrom.to()).setRoadToCurrentNode(roadFrom, getNodeById(roadFrom.to()).roadFromIndex(roadFrom));
                        }
                    }
                }

            }

        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return graph;
    }
}

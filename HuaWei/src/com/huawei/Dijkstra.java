package com.huawei;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author Ga_Vin
 * @create 2019-03-09-21:48
 */
public class Dijkstra {

    private Road[] roadTo;//最小生成树的数组
    private double[] costTo;//到某节点的最小代价的数组
    private IndexPriorityQueue pq;

    public Dijkstra(Graph graph, int from, int to, ArrayList passedNode) {
        from = graph.findIndexOfNode(from);
        to = graph.findIndexOfNode(to);
        roadTo = new Road[graph.nodeNum()];
        costTo = new double[graph.nodeNum()];
        pq = new IndexPriorityQueue(graph.nodeNum());//索引最优队列
        //初始化代价数组为无穷远
        for (int i = 0; i < costTo.length; i++) {
            costTo[i] = Double.POSITIVE_INFINITY;
        }
        //出发节点代价为0
        costTo[from] = 0.0;
        //将出发节点插入最优队列
        pq.insert(from,0.0);

        //开始查找
        while (pq.contains(to) || !pq.isEmpty()) {
            relax(graph, pq.delMin(),passedNode);
        }
    }

    /**
     * 节点的松弛
     * @param graph
     * @param v
     */
    private void relax(Graph graph, int v,ArrayList passedNode) {
        //寻找代价最小的相邻结点
        for (int i = 0; i < 4; i++) {
            double[] turningWeight = {1.0,1.2,1.2};//转向权值数组
            Road road = graph.getNodeByIndex(v).roadFromCurrentNode(i);
            if (road != null) {
                if (passedNode != null && passedNode.contains(road.to())) {
                    continue;
                }
                int w = graph.findIndexOfNode(road.to());
                int index = 0;
                Road preRoad = roadTo[v];
                if (preRoad != null) {
                    index = Dispatch.direction(graph.getNodeByIndex(v),preRoad, road) - 1;
                }
                if (costTo[w] > costTo[v] + road.weight()*turningWeight[index]) {
                    costTo[w] = costTo[v] + road.weight()*turningWeight[index];
                    roadTo[w] = road;
                    if (pq.contains(w)) pq.replace(w, costTo[w]);
                    else pq.insert(w, costTo[w]);
                }
            }
        }
    }

    public Road[] getRoadTo() {
        return roadTo;
    }

    public double[] getCostTo() {
        return costTo;
    }

    public double getCostTo(int v) {
        return costTo[v];
    }

    /**
     * 是否存在到某点的路径
     *
     * @param v
     * @return
     */
    public boolean hasPathTo( int v) {
        return costTo[v] < Double.POSITIVE_INFINITY;
    }

    /**
     * 到某点的路径
     *
     * @param v
     * @return
     */
    public Stack<Road> pathTo(Graph graph, int v) {
        v = graph.nodes().indexOf(graph.getNodeById(v));
        if (!hasPathTo( v)) {
            return null;
        }
        Stack<Road> path = new Stack<>();
        for (Road road = roadTo[v]; road != null; road = roadTo[graph.nodes().indexOf(graph.getNodeById(road.from()))]) {
            path.push(road);
        }
        return path;
    }
}

package com.huawei;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Ga_Vin
 * @create 2019-04-06-12:18
 */
public class Test {
    public static void main(String[] args) {
        String roadPath = "C:\\OneDrive\\code\\map-training\\2-map-training-1\\road.txt";
        String crossPath = "C:\\OneDrive\\code\\map-training\\2-map-training-1\\cross.txt";
        String carPath = "C:\\OneDrive\\code\\map-training\\2-map-training-1\\car.txt";
        String presetAnswerPath = "C:\\OneDrive\\code\\map-training\\2-map-training-1\\presetAnswer.txt";
        String answerPath = "C:\\OneDrive\\code\\map-training\\2-map-training-1\\answer.txt";
        //读取数据
        HashMap<Integer,Road> roadsTo = new HashMap();
        HashMap<Integer,Road> roadsFrom = new HashMap();
        ArrayList<int[]> nodeArray = new ArrayList<>();
        HashMap<Integer,Car> carMap = new HashMap();
        ArrayList<int[]> presetAnswer = new ArrayList<>();
        ArrayList<int[]> answer = new ArrayList<>();
        Utils.readRoad(roadPath, roadsTo,roadsFrom);
        Utils.readNode(crossPath, nodeArray);
        Utils.readCar(carPath, carMap);
        Utils.readAnswer(presetAnswerPath, presetAnswer);
        Utils.readAnswer(answerPath, answer);
        //创建道路网络图
        Graph graph = new Graph(nodeArray.size());
        //根据节点信息加入路径
        for (int i = 0; i < nodeArray.size(); i++) {
            int[] node = nodeArray.get(i);
            graph.getNodeByIndex(i).setId(node[0]);
            for (int j = 1; j < node.length; j++) {
                Road roadTo = roadsTo.get(node[j] * node[0]);
                Road roadFrom = roadsFrom.get(node[j] * node[0]);
                if (node[j] != -1 && roadTo != null) {
                    graph.addRoadToCurrentNode(roadTo, i,j - 1);
                }
                if (node[j] != -1 && roadFrom != null) {
                    graph.addRoadFromCurrentNode(roadFrom,i, j - 1);
                }
            }
        }
        //graph的节点按id升序排列
        Collections.sort(graph.nodes());
        //读取预设车辆的路径
        for (int[] preset: presetAnswer) {
            Car car = carMap.get(preset[0]);
            car.setActualTime(preset[1]);
            ArrayList<Road> pathList = new ArrayList<>();
            for (int i = 2; i < preset.length; i++) {
                int nodeId = car.from();
                if (i > 2) nodeId = pathList.get(i - 3).to();
                Road road = roadsFrom.get(nodeId * preset[i]);
                if (road != null && road.from() == nodeId) {
                    pathList.add(road);
                } else if (road != null && road.to() == nodeId) {
                    pathList.add(roadsTo.get(nodeId * preset[i]));
                }
            }
            car.setPath(pathList);
        }

        //读取答案车辆的路径
        for (int[] a: answer) {
            Car car = carMap.get(a[0]);
            car.setActualTime(a[1]);
            ArrayList<Road> pathList = new ArrayList<>();
            for (int i = 2; i < a.length; i++) {
                int nodeId = car.from();
                if (i > 2) nodeId = pathList.get(i - 3).to();
                Road road = roadsFrom.get(nodeId * a[i]);
                if (road != null && road.from() == nodeId) {
                    pathList.add(road);
                } else if (road != null && road.to() == nodeId) {
                    pathList.add(roadsTo.get(nodeId * a[i]));
                }
            }
            car.setPath(pathList);
        }

        //车辆数组的创建
        ArrayList<Car> carInRoad = new ArrayList<>();
        ArrayList<Car> carArrived = new ArrayList<>();
        ArrayList<Car> carNormal = new ArrayList<>();
        ArrayList<Car> carPriority = new ArrayList<>();
        ArrayList<ArrayList<Car>> carInGarage = new ArrayList<>();
        for (int i = 0; i < graph.nodeNum(); i++) {
            carInGarage.add(new ArrayList<>());
        }
        //将所有车辆都加入到carInGarage对应节点的车库中
        int priorityCarNum = 0;
        int maxSpeed = 0;
        int minSpeed = Integer.MAX_VALUE;
        int priorityMaxSpeed = 0;
        int priorityMinSpeed = Integer.MAX_VALUE;
        int maxPlanTime = 0;
        int minPlanTime = Integer.MAX_VALUE;
        int priorityMaxPlanTime = 0;
        int priorityMinPlanTime = Integer.MAX_VALUE;
        ArrayList<Integer> startPoint = new ArrayList<>();
        ArrayList<Integer> endPoint = new ArrayList<>();
        ArrayList<Integer> priorityStartPoint = new ArrayList<>();
        ArrayList<Integer> priorityEndPoint = new ArrayList<>();
        for (Car car : carMap.values()) {
            //将车辆加入到各个节点车库里
            carInGarage.get(graph.findIndexOfNode(car.from())).add(car);
            //非与预置车辆数组
            //非与预置车辆数组
            if (car.getPreset() != 1 && car.getPriority() != 1) {
                carNormal.add(car);
            }
            if (car.getPreset() != 1 && car.getPriority() == 1) {
                carPriority.add(car);
            }
            //参数计算
            if (car.speed() > maxSpeed) maxSpeed = car.speed();
            if (car.speed() < minSpeed) minSpeed = car.speed();
            if (car.planTime() > maxPlanTime) maxPlanTime = car.planTime();
            if (car.planTime() < minPlanTime) minPlanTime = car.planTime();
            if (!startPoint.contains(car.from())) startPoint.add(car.from());
            if (!endPoint.contains(car.to())) endPoint.add(car.to());
            //当为优先车辆时的参数计算
            if (car.getPriority() == 1) {
                priorityCarNum++;
                if(car.speed()>priorityMaxSpeed) priorityMaxSpeed = car.speed();
                if(car.speed()<priorityMinSpeed) priorityMinSpeed = car.speed();
                if (car.planTime() > priorityMaxPlanTime) priorityMaxPlanTime = car.planTime();
                if (car.planTime() < priorityMinPlanTime) priorityMinPlanTime = car.planTime();
                if (!priorityStartPoint.contains(car.from())) priorityStartPoint.add(car.from());
                if (!priorityEndPoint.contains(car.to())) priorityEndPoint.add(car.to());
            }
        }
        //系数因子的计算
        double[] coe = new double[5];
        coe[0] = (double) carMap.size() / priorityCarNum;
        coe[1] = ((double) maxSpeed / minSpeed) / ((double) priorityMaxSpeed / priorityMinSpeed);
        coe[2] = ((double) maxPlanTime / minPlanTime) / ((double) priorityMaxPlanTime / priorityMinPlanTime);
        coe[3] = (double) startPoint.size() / priorityStartPoint.size();
        coe[4] = (double) endPoint.size() / priorityEndPoint.size();
        for (int i = 0; i < 5; i++) {
            BigDecimal bg = new BigDecimal(coe[i]).setScale(5, BigDecimal.ROUND_HALF_UP);
            coe[i] = bg.doubleValue();
        }
        double alpha = coe[0] * 0.05 + coe[1] * 0.2375 + coe[2] * 0.2375 + coe[3] * 0.2375 + coe[4] * 0.2375;
        double beta = coe[0] * 0.8 + coe[1] * 0.05 + coe[2] * 0.05 + coe[3] * 0.05 + coe[4] * 0.05;
        //对每个结点车库进行排序,以计划出发时间,优先级,id的顺序降序排序
        for (int i = 0; i < carInGarage.size(); i++) {
            Collections.sort(carInGarage.get(i));
        }

        //测试系统
        int time = 0;
        double weightRate = 1.02;
        while (!Dispatch.isRunOver(carInGarage,carInRoad)) {

            time++;
            //第一步,移动道路内的车辆
            Dispatch.moveCarInRoad(graph,carMap);
            //优先上路车辆处理
            Dispatch.driveCarsInGarage(true,graph, carMap, carInGarage, carInRoad, time, weightRate);
            //创建每条道路的车辆优先级队列
            Dispatch.createCarQueue(graph, carMap);
            //第二步,路口的处理
           Dispatch.crossDispatch(graph, carMap, carInGarage, carInRoad, carArrived, time, weightRate);

            //发生死锁
            if (graph.isLocked()) {
                System.out.println("锁死！");
                break;
            }

            //第三步,安排所有车辆上路
            Dispatch.driveCarsInGarage(false, graph, carMap, carInGarage, carInRoad, time, weightRate);
            System.out.println("time:" + time + "  carInRoad:" + carInRoad.size() + "  carArrived:" + carArrived.size());
        }
        if (!graph.isLocked()) {
            Integer t_sum = 0;
            Integer t_pri = 0;
            Integer t_fpt = Integer.MAX_VALUE;
            Integer t_sumPri = 0;
            for (Car car : carArrived) {
                t_sum += car.travelTime();
                if (car.getPriority() == 1) {
                    t_sumPri += car.travelTime();
                    if (car.travelTime()+car.planTime() > t_pri) t_pri = car.travelTime()+car.planTime();
                    if (car.planTime() < t_fpt) t_fpt = car.planTime();
                }
            }
            t_pri -= t_fpt;
            System.out.println("系统调度时间：" + time + " 系统总调度时间:" + t_sum);
            System.out.println("优先车辆调度时间：" + t_pri + "  优先车辆总调度时间:" + t_sumPri);
            System.out.println("最终调度时间：" + Math.round(alpha * t_pri + time) + " 最终总调度时间：" + Math.round(beta * t_sumPri + t_sum));
        }
    }
}

package com.huawei;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.Integer.valueOf;

/**
 * @author Ga_Vin
 * @create 2019-03-09-20:08
 */
public class Main {

    public static void main(String[] args) {
        String roadPath = "C:\\OneDrive\\code\\map-training\\2-map-exam-1\\road.txt";
        String crossPath = "C:\\OneDrive\\code\\map-training\\2-map-exam-1\\cross.txt";
        String carPath = "C:\\OneDrive\\code\\map-training\\2-map-exam-1\\car.txt";
        String presetAnswerPath = "C:\\OneDrive\\code\\map-training\\2-map-exam-1\\presetAnswer.txt";
        String answerPath = "C:\\OneDrive\\code\\map-training\\2-map-exam-1\\answer.txt";
        //读取数据
        HashMap<Integer,Road> roadsTo = new HashMap();
        HashMap<Integer,Road> roadsFrom = new HashMap();
        ArrayList<int[]> nodeArray = new ArrayList<>();
        HashMap<Integer,Car> carMap = new HashMap();
        ArrayList<int[]> presetAnswer = new ArrayList<>();
        Utils.readRoad(roadPath, roadsTo,roadsFrom);
        Utils.readNode(crossPath, nodeArray);
        Utils.readCar(carPath, carMap);
        Utils.readAnswer(presetAnswerPath, presetAnswer);
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

        //车辆数组的创建
        ArrayList<Car> carInRoad = new ArrayList<>();
        ArrayList<Car> carArrived = new ArrayList<>();
        ArrayList<Car> carNormal = new ArrayList<>();
        ArrayList<Car> carPriority = new ArrayList<>();
        ArrayList<Car> carPreset = new ArrayList<>();
        HashMap<Integer, Integer> carPresetStartTime = new HashMap<>();
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
            if (car.getPreset() != 1 && car.getPriority() != 1) {
                carNormal.add(car);
            }
            if (car.getPreset() != 1 && car.getPriority() == 1) {
                carPriority.add(car);
            }
            if (car.getPreset() == 1) {
                carPreset.add(car);
            }
            //统计预置车辆的实际发车时间
            if (car.getPreset() == 1) {
                if (!carPresetStartTime.containsKey(car.actualTime())) {
                    carPresetStartTime.put(car.actualTime(), 1);
                } else {
                    carPresetStartTime.replace(car.actualTime(), carPresetStartTime.get(car.actualTime()) + 1);
                }
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

        //优先级车辆的实际出发时间设置
        Collections.sort(carPriority,
                (Car car1, Car car2) -> car1.speed() > car2.speed() ? 1 :
                        (car1.speed() < car2.speed() ? -1 :
                                valueOf(car2.planTime()).compareTo(car1.planTime())));
        Collections.sort(carNormal,
                (Car car1, Car car2) -> car1.speed() > car2.speed() ? -1 :
                        (car1.speed() < car2.speed() ? 1 :
                                valueOf(car2.planTime()).compareTo(car1.planTime())));
//        Dispatch.randomStartNode(carPriority);
//        Dispatch.randomStartNode(carNormal);
        Collections.sort(carPreset,
                (Car car1, Car car2) -> valueOf(car2.actualTime()).compareTo(car1.actualTime()));
        int maxAllowedResetNum = (int) Math.floor(carPreset.size() * 0.1);
        for (int i = 0; i < maxAllowedResetNum; i++) {
            Car car = carPreset.get(i);
            car.setPreset(0);
            car.removePath();
        }
        //复制当前的初始图
        Graph graphInit = (Graph) graph.clone();
        //复制初始车库数组
        ArrayList<ArrayList<Car>> carInGarageInit = new ArrayList<>();
        for (int i = 0; i < carInGarage.size(); i++) {
            carInGarageInit.add(copyCarArray(carMap, carInGarage.get(i)));
        }
        //测试系统
        int time = 0;
        int deadLockedTime = 0;
        int deadLockedNum = 0;
        double weightRate = 0.04;
        while (!Dispatch.isRunOver(carInGarage,carInRoad)) {
            time++;
            //非预置车辆出发时间设定
            Dispatch.setStartTime(graph, carInRoad, carPresetStartTime, carNormal, carPriority, time, deadLockedTime);
            //对每个节点车库进行排序,以计划出发时间,优先级,id的顺序降序排序
            for (int i = 0; i < carInGarage.size(); i++) {
                Collections.sort(carInGarage.get(i));
            }

            //第一步,移动道路内的车辆
            Dispatch.moveCarInRoad(graph,carMap);
            //优先上路车辆处理
            Dispatch.driveCarsInGarage(true, graph, carMap, carInGarage, carInRoad, time, weightRate);
            //创建每条道路的车辆优先级队列
            Dispatch.createCarQueue(graph, carMap);
            //第二步,路口的处理
            ArrayList<Node> unhandledNodes = Dispatch.crossDispatch(graph, carMap, carInGarage, carInRoad, carArrived, time, weightRate);

            //若发生死锁,则从新规划一部分死锁节点车辆的路径,并返回上一时间片重新开始
            if (graph.isLocked()) {
                boolean isSolved = false;//标志位,判断是否新的路径规划成功
                ArrayList<Road> deadLockedRoads = Dispatch.getDeadLockedRoads(carMap, unhandledNodes);
                for (int i = 0; i < deadLockedRoads.size(); i++) {
                    Road deadLockedRoad = deadLockedRoads.get(i);
                    //重新规划当前deadLockedRoads里的第一优先级的车辆路径
                    if (Dispatch.researchInRoad(carMap, graph, deadLockedRoad)) {
                        isSolved = true; //若成功重新规划路径,标志位置为true
                        System.out.println("重新规划路径成功!");
                    }
                }
                //若重新规划路径成功,则返回上一时间点继续调度
                if (isSolved) {
                    deadLockedNum++;
                    if(deadLockedTime<time) deadLockedTime = time;
                    time = 0;
                    //初始图的复制
                    Graph graphCopy = (Graph) graphInit.clone();
                    //车辆map的复制,只复制了车辆的路径和actualTime
                    HashMap<Integer, Car> carMapCopy = copyCarMap(graphCopy, carMap);
                    //初始车库车辆的复制
                    ArrayList<ArrayList<Car>> carInGarageCopy = new ArrayList<>();
                    for (int i = 0; i < carInGarage.size(); i++) {
                        carInGarageCopy.add(copyCarArray(carMapCopy, carInGarageInit.get(i)));
                    }
                    //当前时刻未安排实际出发时间的车辆数组的复制
                    ArrayList<Car> carNormalCopy = copyCarArray(carMapCopy, carNormal);
                    ArrayList<Car> carPriorityCopy = copyCarArray(carMapCopy, carPriority);

                    graph = graphCopy;
                    carMap = carMapCopy;
                    carInGarage = carInGarageCopy;
                    carInRoad = new ArrayList<>();
                    carArrived = new ArrayList<>();
                    carNormal = carNormalCopy;
                    carPriority = carPriorityCopy;
                    continue;
                }
                //否则,破死锁失败
                else{
                    System.out.println("锁死！");
                    break;
                }
            }

            //第三步,安排所有车辆上路
            Dispatch.driveCarsInGarage(false, graph, carMap, carInGarage, carInRoad, time, weightRate);
            System.out.println(deadLockedNum + "---" + "time:" + time +
                    "  carInRoad:" + carInRoad.size() +
                    "  carArrived:" + carArrived.size());
        }
        if (!graph.isLocked()) {
            int t_sum = 0;
            int t_pri = 0;
            int t_fpt = Integer.MAX_VALUE;
            int t_sumPri = 0;
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

        //输出答案
        StringBuffer answer = new StringBuffer();
        answer.append("#carid, time, roadId1...\n");
        for (Car car : carArrived) {
            if (car.getPreset() != 1) {
                answer.append("(" + car.id() + "," + car.actualTime() + ",");
                for (int i = 0; i < car.path().size(); i++) {
                    Road road = car.path().get(i);
                    answer.append(road.toString());
                    if (i != car.path().size() - 1) {
                        answer.append(",");
                    }
                }
                answer.append(")\n");
            }
        }
        Utils.writeAnswer(answerPath,answer.toString());
        System.out.println("路径输出成功！");
//        //System.out.println(answer);
    }

    /**
     * 车辆数组的复制
     * @param carMapCopy
     * @param CarArray
     * @return
     */
    public static ArrayList<Car> copyCarArray(HashMap<Integer, Car> carMapCopy, ArrayList<Car> CarArray) {
        ArrayList<Car> carArrayCopy = new ArrayList<>();
        for (int k = 0; k < CarArray.size(); k++) {
            carArrayCopy.add(carMapCopy.get(CarArray.get(k).id()));
        }
        return carArrayCopy;
    }

    /**
     * 车辆map的复制,只复制当前车辆的路径和actualTime
     * @param graphCopy
     * @param carMap
     * @return
     */
    public static HashMap<Integer, Car> copyCarMap(Graph graphCopy,HashMap<Integer, Car> carMap) {
        HashMap<Integer, Car> carMapCopy = new HashMap<>();
        for (Map.Entry<Integer,Car> entry : carMap.entrySet()) {
            carMapCopy.put(entry.getKey(), (Car) entry.getValue().clone(graphCopy));
        }
        return carMapCopy;
    }
}

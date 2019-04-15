package com.huawei;

import java.util.*;

/**
 * @author Ga_Vin
 * @create 2019-03-11-22:47
 */
public class Dispatch {

    /**
     * 第一步,移动道路内的车辆,并标记
     * @param graph
     */
    public static void moveCarInRoad(Graph graph, HashMap<Integer, Car> carMap) {
        for (Road road: graph.allRoads()) {
            for (int i = 0; i < road.channel(); i++) {
                Lane lane = road.lane(i);
                int first = lane.firstCarIndex;
                int last = lane.lastCarIndex;
                //若当前车道有车时
                if (first != -1 && last != -1) {
                    //从第一辆车的位置向后处理直到最后一辆车为止
                    for (int j = first; j >= last; j--) {
                        Car car = carMap.get(lane.lane[j]);
                        if (car != null) {
                            //判断是否有前车阻挡,有则返回前车的状态;没有则返回-1
                            int isBlock = isBlock(carMap, car, lane);
                            //判断当前车辆是否驶出当前车道,isMoveOut>=0会驶出当前道路;isMoveOut=-1驶出当前道路且到达终点;isMoveOut=-2则不会驶出道路
                            int isMoveOut = isMoveOut(car,car.nextRoad());
                            car.setIsMoveOut(isMoveOut);
                            //没有阻挡
                            if (isBlock == -1) {
                                //出路口等待(包括到达终点的车辆)
                                if (isMoveOut >= -1) {
                                    car.setStatus(2);
                                }
                                //不出路口且没有阻挡,直接向前移动
                                if (isMoveOut == -2) {
                                    lane.moveForward(car);
                                }
                            }
                            //有阻挡且前车是终止状态,直接向前移动
                            if (isBlock == 0) {
                                lane.moveForward(car);
                            }
                            //有阻挡且前车不出路口等待,标记为不出路口等待
                            if (isBlock == 1) {
                                car.setStatus(1);
                            }
                            //有阻挡且前车出路口等待
                            if (isBlock == 2) {
                                //要出路口,标记为出路口等待
                                if (isMoveOut >= -1) {
                                    car.setStatus(2);
                                }
                                //不出路口,标记为不出路口等待
                                if (isMoveOut == -2) {
                                    car.setStatus(1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 在每个道路中创建车辆优先队列
     * @param graph
     * @param carMap
     */
    public static void createCarQueue(Graph graph, HashMap<Integer, Car> carMap) {
        for (Road road: graph.allRoads()) {
            road.setCarQueue(carQueueInRoad(carMap, road));
        }
    }

    /**
     * 第二步,循环每个node里的道路,处理出路口等待的车辆
     *
     * @param graph
     * @param carInRoad
     * @param carArrived
     */
    public static ArrayList<Node> crossDispatch(Graph graph, HashMap<Integer, Car> carMap,
                                                ArrayList<ArrayList<Car>> carInGarage, ArrayList<Car> carInRoad,
                                                ArrayList<Car> carArrived, int time, double weightRate) {
        ArrayList<Node> unhandledNodes = unhandledNodes(graph);//按照节点id降序排列的未处理的节点数组
        //当还有节点没处理完时
        while (unhandledNodes.size() != 0) {
            boolean hasCarMoved = false;//标志位,判断当前循环中是否有车辆移动
            //循环未处理完的节点,从id较小的开始
            for (int i = unhandledNodes.size() - 1; i >= 0; i--) {
                Node node = unhandledNodes.get(i);
                ArrayList<Road> unhandledRoads = unhandledRoads(carMap, node);//当前节点中未处理完的道路数组,按id大小降序排列
                //若当前节点所有道路都已经处理完时,则把当前节点从unhandledNode中移除
                if (unhandledRoads.size() == 0){
                    unhandledNodes.remove(node);
                    continue;
                }
                //循环未处理的道路,从id较小的开始
                for (int j = unhandledRoads.size() - 1; j >= 0; j--) {
                    Road road = unhandledRoads.get(j);
                    //当前道路等待出路口的车辆优先级队列的数目不为零时
                    while (road.carQueue().size() != 0) {
                        Car car = carMap.get(road.carQueue().get(0));//获取当前道路第一优先权的车辆
                        //判断行驶方向是否冲突,若有冲突则break当前道路,循环到下条道路
                        if (isConflict(carMap, node, unhandledRoads, car)) break;
                        //car.isMoveOut()==-1表示到达终点
                        if (car.isMoveOut() == -1) {
                            carArrivedHandle(carMap, road, car, carInRoad, carArrived, time, weightRate);
                            hasCarMoved = true;
                        }
                        //否则,>=0--表示能驶入下条车道,且car.isMoveOut()值为预计在下条车道行驶的距离
                        else {
                            int isAccessible = isAccessible(carMap, car.nextRoad(), car.isMoveOut());//判断下条道路能否进入
                            //下条道路能进入,isAccessible值为下条道路进入的车道号
                            if (isAccessible >= 0) {
                                carMoveInNextRoad(carMap,road, car, isAccessible, weightRate);
                                hasCarMoved = true;
                            }
                            //下条道路不能进入,当前车辆移动到车道最前端
                            if (isAccessible == -1) {
                                carInLaneMoveForward(carMap, road.carQueue(), road.lane(car.laneIndex()));
                                hasCarMoved = true;
                            }
                            //下条道路最后的车辆在等待不能进入,当前车辆不能移动,跳出循环到下条道路
                            if (isAccessible == -2) break;
                        }
                        //优先车辆上路
                        int nodeFromIndex = graph.findIndexOfNode(road.from());
                        driveCarsInRoad(graph, carMap, road, carInGarage.get(nodeFromIndex), carInRoad, time, weightRate);
                    }
                    if(road.carQueue().size() == 0) unhandledRoads.remove(road);
                }
            }
            //在一次unhandledNode的循环中,若标志位hasCarMovedInGraph没有变动且unhandledNode中没有节点被处理完,则判断为死锁
            if (!hasCarMoved && unhandledNodes.size() != 0) {
                //发生死锁时找出导致死锁的道路和车辆信息
                graph.setLocked(true);
                break;
            }
        }
        return unhandledNodes;
    }

    /**
     * 第三步,初始化上路的车辆
     *
     * @param graph
     * @param carInGarage
     * @param carInRoad
     * @param time
     */
    public static void driveCarsInGarage(Boolean priority, Graph graph, HashMap<Integer, Car> carMap,
                                         ArrayList<ArrayList<Car>> carInGarage, ArrayList<Car> carInRoad, int time,  double weightRate) {
        for (int i = 0; i < carInGarage.size(); i++) {
            ArrayList<Car> carsInNode = carInGarage.get(i);
            ArrayList<Car> allowedCarsInNode = allowedCarInNode(carsInNode, time);
            for (int j = 0; j < allowedCarsInNode.size(); j++) {
                Car car = allowedCarsInNode.get(j);
                //为当前车辆搜寻路径
                if (car.path().size() == 0) {
                    searchPath(graph, car);
                }
                //priority:true只上路优先车辆
                if (priority && car.getPriority() == 1) {
                    runCarToRoad(carMap, car, carsInNode, carInRoad, weightRate);
                }
                //priority:false所有车辆都可以上路,但优先车辆先上路
                if (!priority) {
                    runCarToRoad(carMap, car, carsInNode, carInRoad, weightRate);
                }
            }
        }
    }

    /**
     * 路口调度时,当前道路出发节点的优先车辆出发处理
     * @param graph
     * @param carMap
     * @param carsInNode
     * @param carInRoad
     * @param time
     * @param weightRate
     */
    public static void driveCarsInRoad(Graph graph, HashMap<Integer, Car> carMap, Road road,
                                       ArrayList<Car> carsInNode, ArrayList<Car> carInRoad, int time, double weightRate) {
        ArrayList<Car> allowedCarInNode = allowedCarInNode(carsInNode, time);
        for (int j = 0; j < allowedCarInNode.size(); j++) {
            Car car = allowedCarInNode.get(j);
            //为当前车辆搜寻路径
            if (car.path().size() == 0) {
                searchPath(graph, car);
            }
            //只上路优先车辆
            if (car.getPriority() == 1 && car.nextRoad().id() == road.id()) {
                runCarToRoad(carMap, car, carsInNode, carInRoad, weightRate);
            }
        }
    }

    /**
     * 车辆上路的处理,若成功上路,返回true;否则返回false
     *
     * @param carMap
     * @param car
     * @param carInNode
     * @param carInRoad
     * @param weightRate
     * @return
     */
    public static boolean runCarToRoad(HashMap<Integer, Car> carMap, Car car,
                                       ArrayList<Car> carInNode, ArrayList<Car> carInRoad, double weightRate) {
        int v = Math.min(car.speed(), car.nextRoad().speed());
        int isAccessible = isAccessible(carMap, car.nextRoad(), v);//判断下条道路是否还有剩余位置,有则返回车道号
        //如果道路能进入时
        if (isAccessible >= 0) {
            car.nextRoad().lane(isAccessible).moveIn(car, v - 1);
            car.nextRoad().carNumInRoad++;
            car.setLaneIndex(isAccessible);
            car.moveInNextRoad();
            carInRoad.add(car);
            carInNode.remove(car);
            increaseWeight(car, weightRate);//增加路径道路的权值
            return true;
        }
        return false;
    }

    /**
     * 计算并排序同时刻上路车的数组
     * @param carInNode
     * @param time
     * @return
     */
    public static ArrayList<Car> allowedCarInNode(ArrayList<Car> carInNode, int time) {
        ArrayList<Car> allowedCar = new ArrayList<>();
        //将当前时刻可以出发的车辆加入到allowedCar数组中
        for (int i = carInNode.size() - 1; i >= 0; i--) {
            Car car = carInNode.get(i);
            //若car.actualTime() > time说明后面的车辆出发时间也大于当前时刻了,不用再遍历下去(carInNode是有序的)
            if(car.actualTime() > time) break;
            //如果当前车辆为预置车辆且出发时间为当前时刻或之前时,将其加入allowedCar数组里
            allowedCar.add(car);
        }
        //排序,按优先级降序,实际出发时间升序,车辆id升序
        Collections.sort(allowedCar,
                (Car car1, Car car2) -> car1.getPriority() > car2.getPriority() ? -1 :
                        (car1.getPriority() < car2.getPriority() ? 1 :
                                (car1.actualTime() > car2.actualTime() ? 1 :
                                        (car1.actualTime() < car2.actualTime() ? -1 :
                                                (car1.id() > car2.id() ? 1 : -1)))));
        return allowedCar;
    }

    /**
     * 判断前方是否有车辆阻挡。若有,则返回前车的状态,否则返回-1
     *
     * @param car
     * @param lane
     * @return
     */
    public static int isBlock(HashMap<Integer, Car> carMap, Car car, Lane lane) {
        int p = car.position();
        int q = lane.hasFrontCar(p);//返回与前车的距离。没有则返回-1
        //当前速度是否大于等于与前车的距离
        if (q > 0 && car.currentSpeed() >= q) {
            return carMap.get(lane.lane[p + q]).status();
        }
        //没有前车则返回-1
        return -1;
    }

    /**
     * 判断当前车辆是否驶出当前车道
     * -1--表示行驶到目的地;
     * -2--表示不能驶出当前车道,行驶其最大距离后标记为终止;
     * >=0--表示能驶出当前车道,返回下个车道可行使距离;
     * @param car
     * @return
     */
    public static int isMoveOut(Car car,Road nextRoad) {
        int s1 = car.currentRoad().length() - car.position() - 1;//当前道路可行驶的距离S1
        int v1 = car.currentSpeed();//当前道路的最大行驶速度v1
        //判断是否会驶出道路
        if (v1 > s1) {
            //到达终点
            if (nextRoad == null) {
                return -1;
            } else {
                int v2 = Math.min(nextRoad.speed(), car.speed());//下一条道路的最大行驶速度v2
                int s2 = v2 - s1;//下一条道路的行驶距离s2
                if (s2 < 0) {
                    return 0;
                } else return s2;
            }
        }
        //-2表示不能出路口
        return -2;
    }

    /**
     * 到达目的地的车的处理
     *
     * @param road
     * @param car
     * @param carInRoad
     * @param carArrived
     */
    public static void carArrivedHandle(HashMap<Integer, Car> carMap, Road road, Car car,
                                        ArrayList<Car> carInRoad, ArrayList<Car> carArrived, int reachTime, double weightRate) {
        int laneIndex = car.laneIndex();
        road.carQueue().remove(0);//从当前道路的等待出路口的车辆数组中移除当前车辆
        road.lane(laneIndex).moveOut(car);
        road.carNumInRoad--;
        updateWeight(car, weightRate);
        car.setStatus(0);
        car.setLaneIndex(-1);
        car.setTravelTime(reachTime);
        car.moveInNextRoad();
        carInRoad.remove(car);
        carArrived.add(car);
        laneHandle(carMap, road.lane(laneIndex));//处理当前车道后面的车辆
    }

    /**
     * 降序返回图中的节点到unhandledNode中
     * @param graph
     * @return
     */
    public static ArrayList<Node> unhandledNodes(Graph graph) {
        ArrayList<Node> unhandledNode = new ArrayList<>();
        for (int i = graph.nodeNum()-1; i >= 0; i--) {
            unhandledNode.add(graph.getNodeByIndex(i));
        }
        return unhandledNode;
    }

    /**
     * 返回当前路口待处理的道路,按id降序排列
     *
     * @param node
     * @return
     */
    public static ArrayList<Road> unhandledRoads(HashMap<Integer, Car> carMap, Node node) {
        ArrayList<Road> unhandledRoad = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Road road = node.roadsToCurrentNode()[i];
            if (road != null && road.carQueue().size() != 0) {
                unhandledRoad.add(road);
            }
        }
        //按道路id从大到小排序
        Collections.sort(unhandledRoad);
        return unhandledRoad;
    }

    /**
     * 寻找在一条道路里的车辆优先级队列,里面存储车辆的id
     * @param carMap
     * @param road
     * @return
     */
    public static ArrayList<Integer> carQueueInRoad(HashMap<Integer, Car> carMap, Road road) {
        ArrayList<Integer> carQueueInRoad = new ArrayList<>();
        ArrayList<ArrayList<Car>> tempQueue = new ArrayList<>();
        for (int i = 0; i < road.channel(); i++) {
            tempQueue.add(new ArrayList<>());
        }
        int tempQueueNum = 0;//标志有多少辆要出道路的车辆
        //每个车道要出道路的车辆加入到对应的tempQueue的数组中
        for (int i = 0; i < road.channel(); i++) {
            for (int j = road.length() - 1; j >= road.length() - road.speed(); j--) {
                Car car = carMap.get(road.lane(i).lane[j]);
                //若车辆状态为2,则将其添加临时车辆优先级队列中
                if (car != null && car.status() == 2) {
                    tempQueue.get(i).add(car);
                    tempQueueNum++;
                }
            }
        }
        flag:
        while (tempQueueNum != 0) {
            ArrayList<Car> fpc = new ArrayList<>();
            //将每个车道要出路口的第一辆车加入到fpc数组中
            for (int i = 0; i < road.channel(); i++) {
                if (tempQueue.get(i).size() != 0) {
                    fpc.add(tempQueue.get(i).get(0));
                }
            }
            Collections.sort(fpc,
                    (Car car1, Car car2) -> car1.position() > car2.position() ? -1 :
                            (car1.position() < car2.position() ? 1 :
                                    (car1.laneIndex() > car2.laneIndex() ? 1 : -1)));
            for (int i = 0; i < fpc.size(); i++) {
                Car car = fpc.get(i);
                if (car.getPriority() == 1) {
                    carQueueInRoad.add(car.id());
                    tempQueue.get(car.laneIndex()).remove(car);
                    tempQueueNum--;
                    continue flag;
                }
            }
            if (fpc.size() == 0) {
                System.out.println("出错道路id：" + road.to());
            }
            carQueueInRoad.add(fpc.get(0).id());
            tempQueue.get(fpc.get(0).laneIndex()).remove(fpc.get(0));
            tempQueueNum--;
        }
        return carQueueInRoad;
    }

    /**
     * 当前车道的第一优先级车辆驶出当前道路达到终止状态时,此车道后面车辆的处理函数
     *
     * @param lane
     */
    public static void laneHandle(HashMap<Integer, Car> carMap, Lane lane) {
        int first = lane.firstCarIndex;
        int last = lane.lastCarIndex;
        //若当前车道还有车时
        if (first != -1 && last != -1) {
            Car firstCar = carMap.get(lane.firstCarId());
            //若在最前面的车辆仍为等待出路口车辆时,不做任何处理,只处理不出路口等待的车辆
            if (firstCar.status() == 1) {
                ArrayList<Car> waitingCarInLane = new ArrayList<>();
                //将此车道中等待前移(状态为1)的车辆装入数组中
                for (int i = first; i >= last; i--) {
                    Car car = carMap.get(lane.lane[i]);
                    if (car != null && car.status() == 1) {
                        waitingCarInLane.add(car);
                    }
                }
                //依次移动等待前移的车辆
                for (Car c : waitingCarInLane) {
                    lane.moveForward(c);
                    c.setStatus(0);//置为终止状态
                }
            }
        }
    }

    /**
     * 判断下一条道路是否可以驶入,可以则返回车道index,不能驶入则返回-1,需要等待则返回-2
     *
     * @param road
     * @param carMap
     * @param m    表示进入道路可以走的距离
     * @return
     */
    public static int isAccessible(HashMap<Integer, Car> carMap, Road road, int m) {
        //m==0说明不能进入条道路
        if (m == 0) return -1;
        //循环当前车道
        for (int i = 0; i < road.channel(); i++) {
            //(当前车道没有车时)或(最后一辆车的位置大于下一辆车要进入的位置)或(最后一辆车不在最后一个位置上且其状态为终止)
            if (road.lane(i).lastCarIndex == -1 || road.lane(i).distanceWithLastCar() > m - 1 ||
                    (road.lane(i).lastCarIndex != 0 && carMap.get(road.lane(i).lastCarId()).status() == 0)) {
                return i;
            }
            //当前车道最后一辆为等待状态且其位置小于等于于下一辆车要进入的位置
            if (road.lane(i).distanceWithLastCar() <= m - 1 && carMap.get(road.lane(i).lastCarId()).status() > 0) {
                return -2;
            }
            //另外一种情况为当前车辆为终止状态且为最后一个位置,则循环到下一车道
        }
        //若所有车道都不能进入,则返回-1
        return -1;
    }

    /**
     * 判断当前车辆出路口时是否与其他车道发生冲突
     *
     * @param node
     * @param car
     * @return
     */
    public static boolean isConflict(HashMap<Integer, Car> carMap, Node node, ArrayList<Road> unhandledRoads, Car car) {
        ArrayList<Car> conflictCars = conflictCars(carMap, node, unhandledRoads, car);
        int d = direction(node, car.currentRoad(), car.nextRoad());
        for (Car car1 : conflictCars) {
            //若car为优先级车辆且car1为普通车辆时,则不可能冲突,跳过
            if (car.getPriority() == 1 && car1.getPriority() == 0) continue;
            //若car为普通车辆却car1为优先级车辆时,则一定发生冲突
            if (car.getPriority() == 0 && car1.getPriority() == 1) return true;

            //如果其他同优先级可能冲突的车辆中有比当前车辆方向优先权大的,则返回true
            //即将到达目的地的车的转向判定为0,比转弯车优先权大
            int d2 = direction(node, car1.currentRoad(), car1.nextRoad());
            if (d == d2) System.out.println("冲突判断出错！");
            if (d > d2) return true;
        }
        return false;
    }

    /**
     * 寻找一条道上与当前车辆有可能发生冲突的车辆
     *
     * @param car
     * @return
     */
    public static ArrayList<Car> conflictCars(HashMap<Integer, Car> carMap, Node node, ArrayList<Road> unhandledRoads, Car car) {
        ArrayList<Car> conflictCars = new ArrayList<>();
        //寻找在同一节点各道路第一优先级车辆的nextRoad为null(即到达终点)或其id与当前车辆的nextRoad的id相同的车辆
        for (Road road : unhandledRoads) {
            Car firstCar = carMap.get(road.carQueue().get(0));
            //若当前车辆到达终点且firstCar的下条道路为当前车辆对面那条路
            if (car.nextRoad() == null && firstCar.nextRoad() != null
                    && Math.abs(node.roadToIndex(car.currentRoad()) - node.roadFromIndex(firstCar.nextRoad())) == 2) {
                conflictCars.add(firstCar);
            }
            //若firstCar到达终点且car的下条道路为firstCar对面那条道路
            if (car.nextRoad() != null && firstCar.nextRoad() == null
                    && Math.abs(node.roadToIndex(firstCar.currentRoad()) - node.roadFromIndex(car.nextRoad())) == 2) {
                conflictCars.add(firstCar);
            }
            //firstCar的下条道路与当前车辆下条道路相同
            if (firstCar.nextRoad() != null && car.nextRoad() != null
                    && firstCar.id() != car.id() && firstCar.nextRoad().id() == car.nextRoad().id()) {
                conflictCars.add(firstCar);
            }
        }
        return conflictCars;
    }

    /**
     * 判断一辆车在路口的转向,1--直行;2--左转;3--右转
     * @param node
     * @param currentRoad
     * @param nextRoad
     * @return
     */
    public static int direction(Node node,Road currentRoad,Road nextRoad) {
        Integer x = node.roadToIndex(currentRoad);
        Integer y = node.roadFromIndex(nextRoad);
        if (x != -1 && y != -1) {
            if (Math.abs(x - y) == 2) {
                return 1;//1--直行
            }
            if (x - y == -1 || x - y == 3) {
                return 2;//2--左转
            }
            if (x - y == 1 || x - y == -3) {
                return 3;//3--右转
            }
        }
        //若到达终点或路径规划第一个节点时,则返回1
        if (x != -1 || y != -1) {
            return 1;
        }
        //其他情况则返回-1
        System.out.println("方向判断出错！");
        return -1;
    }

    /**
     * 出路口等待的车辆移除当前车道的处理
     *
     * @param car
     * @param road
     * @param isAccessible
     */
    public static void carMoveInNextRoad(HashMap<Integer, Car> carMap,
                                         Road road, Car car, int isAccessible, double weightRate) {
        int laneIndex = car.laneIndex();
        road.carQueue().remove(0);//从当前道路的等待出路口的车辆数组中移除当前车辆
        road.lane(laneIndex).moveOut(car);//从当前车道移出当前车辆
        road.carNumInRoad--;
        updateWeight(car, weightRate);
        car.nextRoad().lane(isAccessible).moveIn(car, car.isMoveOut() - 1);
        car.nextRoad().carNumInRoad++;
        car.moveInNextRoad();
        car.setStatus(0);//终止状态
        car.setLaneIndex(isAccessible);
        laneHandle(carMap, road.lane(laneIndex));//处理当前车道后面的车辆
    }

    /**
     * 当前车道的第一优先级车辆不出道路时,此车道后面车辆的处理函数
     *
     * @param lane
     * @param carQueue
     */
    public static void carInLaneMoveForward(HashMap<Integer, Car> carMap, ArrayList carQueue, Lane lane) {
        int first = lane.firstCarIndex;
        int last = lane.lastCarIndex;
        ArrayList<Car> waitingCarInLane = new ArrayList<>();
        //将此车道中所有等待前移（状态大于1）的车辆装入数组中
        for (int i = first; i >= last; i--) {
            Car car = carMap.get(lane.lane[i]);
            if (car != null && car.status() > 0) {
                waitingCarInLane.add(car);
                if (car.status() == 2) carQueue.remove(carQueue.indexOf(car.id()));
            }
        }
        //测试
        if (waitingCarInLane.size() == 0) {
            System.out.println("没有车移动！");
        }
        //依次移动等待前移的车辆
        for (Car c : waitingCarInLane) {
            lane.moveForward(c);
            c.setStatus(0);//置为终止状态
        }
    }

    /**
     * 静态规划路径
     * @param graph
     * @param car
     */
    public static void searchPath(Graph graph, Car car) {
        Dijkstra dijkstra = new Dijkstra(graph, car.from(),car.to(),null);
        Stack<Road> path = dijkstra.pathTo(graph, car.to());
        ArrayList<Road> pathList = new ArrayList<>();
        while (!path.empty()){
            Road road = path.pop();
            pathList.add(road);
        }
        car.setPath(pathList);
    }

    /**
     * 从新规划堵死节点的车辆路径
     *
     * @param graph
     * @return
     */
    public static boolean researchInRoad(HashMap<Integer, Car> carMap, Graph graph, Road deadLockedRoad) {
        //取得当前道路的优先车辆队列
        //取得当前道路第一优先权的车辆
        Car car = carMap.get(deadLockedRoad.carQueue().get(0));
        //若当前车辆为预置车辆或下一条路为到终点的最后一条路或即将到达终点时,不能重新规划路径
        if (car.getPreset() == 1 || car.indexOfCurrentRoad() >= car.path().size() - 2) {
            return false;
        }
        return researchPath(graph, car);
    }

    /**
     * 为车辆从下一条道路到达的节点从新规划路径
     *
     * @param graph
     * @param car
     */
    public static boolean researchPath(Graph graph, Car car) {
        Dijkstra dijkstra = new Dijkstra(graph, car.currentRoad().to(), car.to(), passedNode(car));
        Stack<Road> newPath = dijkstra.pathTo(graph, car.to());
        if (newPath != null) {
            //从原规划路径移除当前道路之后的道路
            for (int i = car.path().size() - 1; i > car.indexOfCurrentRoad(); i--) {
                car.path().remove(car.path().get(i));
            }
            //从当前道路之后添加新的路径
            while (!newPath.empty()) {
                car.path().add(newPath.pop());
            }
            System.out.print("车辆(" + car.id() + ")在节点(" + car.currentRoad().to() + ")  ");
            return true;
        }
        return false;
    }

    /**
     * 返回车辆已经走过的节点和下条道路到达的节点
     * @param car
     */
    public static ArrayList passedNode(Car car) {
        ArrayList passedNode = new ArrayList();
        for (int i = 0; i <= car.indexOfCurrentRoad(); i++) {
            passedNode.add(car.path().get(i).from());
        }
        //添加下条道路到达的节点,即不走下条道路到达的节点了(下条道路不能为到达终点的最后一条路)
        if (car.indexOfCurrentRoad() < car.path().size() - 2) {
            passedNode.add(car.path().get(car.indexOfCurrentRoad() + 1).to());
        }
        return passedNode;
    }

    /**
     * 在车辆上路时增加其经过路径的权值
     * @param car
     * @param weightRate
     */
    public static void increaseWeight(Car car,double weightRate) {
        ArrayList<Road> path = car.path();
        for (int i = 0; i < path.size() && i < 4; i++) {
            Road road = path.get(i);
            road.setWeight(road.weight() * calWeightRate(car.speed(), weightRate));
        }
    }

    /**
     * 更新车辆路径的权值
     * @param car
     */
    public static void updateWeight(Car car, double weightRate) {
        //降低当前的道路的权值
        Road road1 = car.currentRoad();
        road1.setWeight(road1.weight() / calWeightRate(car.speed(), weightRate));
        //增加当前道路前四条道路的权值
        if (car.indexOfCurrentRoad() + 4 < car.path().size()) {
            Road road2 = car.path().get(car.indexOfCurrentRoad() + 4);
            road2.setWeight(road2.weight() * calWeightRate(car.speed(), weightRate));
        }
    }

    /**
     * 计算不同车速下的权值因子
     * @param speed
     * @param weightRate
     * @return
     */
    public static double calWeightRate(double speed, double weightRate) {
        double minSpeed = 4;
        double maxSpeed = 16;
        return 1 + weightRate / 2 + (1 - (speed - minSpeed) / (maxSpeed - minSpeed)) * weightRate / 2;
    }

    /**
     * 判断路上是否还有车
     * @param carInGarage
     * @param carInRoad
     * @return
     */
    public static boolean isRunOver(ArrayList<ArrayList<Car>> carInGarage,ArrayList<Car> carInRoad) {
        boolean flag = false;
        for (ArrayList a : carInGarage) {
            if (a.size() != 0) {
                flag = true;
                break;
            }
        }
        return !flag && carInRoad.size() == 0;
    }

    /**
     * 计算并返回道路上车辆的平均速度
     * @param carInRoad
     * @return
     */
    public static double avgSpeed(ArrayList<Car> carInRoad) {
        if (carInRoad.size() == 0) return 16;
        double avg = 0.0;
        for (Car car : carInRoad) {
            avg += car.currentSpeed();
        }
        return avg / carInRoad.size();
    }

    /**
     * 寻找形成死锁环的道路
     * @param carMap
     * @param unhandledNode
     * @return
     */
    public static ArrayList<Road> getDeadLockedRoads(HashMap<Integer, Car> carMap, ArrayList<Node> unhandledNode) {
        ArrayList<Road> deadLockedRoad = new ArrayList<>();
        //将unhandledNode中所有unhandledRoad的道路加入到deadLockedRoad数组中
        for (int i = unhandledNode.size() - 1; i >= 0; i--) {
            Node node = unhandledNode.get(i);
            ArrayList<Road> unhandledRoads = unhandledRoads(carMap, node);
            for (int j = unhandledRoads.size() - 1; j >= 0; j--) {
                deadLockedRoad.add(unhandledRoads.get(j));
            }
        }
        //从deadLockedRoad数组中去除死锁环外的分支道路
        int deadLockedRoadSize;
        do {
            deadLockedRoadSize = deadLockedRoad.size();
            //如果一条道路没有deadLockedRoad的其他道路到达该道路的from节点,说明不是环的一部分
            for (int i = deadLockedRoadSize - 1; i >= 0; i--) {
                Road road1 = deadLockedRoad.get(i);
                boolean flag = true;
                for (Road road2 : deadLockedRoad) {
                    if (road2.to() == road1.from()) {
                        flag = false;
                        break;
                    }
                }
                if (flag) deadLockedRoad.remove(road1);
            }
        } while (deadLockedRoadSize != deadLockedRoad.size());//循环直到deadLockedRoad的size不再变化,即剩下的道路是组成死锁环的道路
        return deadLockedRoad;
    }

    /**
     * 随机出发节点
     * @param carArrayList
     */
    public static void randomStartNode(ArrayList<Car> carArrayList) {
        Random random = new Random(100);
        int index = 0;
        while (index != carArrayList.size()) {
            int oldIndex = index;
            int speed = carArrayList.get(index).speed();
            int planTime = carArrayList.get(index).planTime();
            for (int i = index; i < carArrayList.size(); i++) {
                if (carArrayList.get(i).speed() != speed || carArrayList.get(i).planTime() != planTime) {
                    index = i;
                    break;
                }
                if (i == carArrayList.size() - 1) index = carArrayList.size();
            }
            ArrayList<Car> temp = new ArrayList<>();
            for (int i = oldIndex; i < index; i++) {
                temp.add(carArrayList.get(i));
            }
            Collections.shuffle(temp,random);
            for (int i = oldIndex; i < index; i++) {
                int j = i - oldIndex;
                carArrayList.set(i, temp.get(j));
            }
        }
    }

    /**
     * 非预置车辆出发时间设置
     * @param graph
     * @param carInRoad
     * @param carPresetStartTime
     * @param carNormal
     * @param carPriority
     * @param time
     */
    public static void setStartTime(Graph graph,ArrayList<Car> carInRoad,HashMap<Integer, Integer> carPresetStartTime,
            ArrayList<Car> carNormal,ArrayList<Car> carPriority,int time,int deadLockedTime) {
        if (time > deadLockedTime) {
            //参数设定
            int maxAllowedCarNum = 120;
            double minAllowCarRate = 0.1;
            double maxAllowCarRate = 0.18;
            //动态设定此时刻的车占比
            double allowCarRate = minAllowCarRate +
                    (maxAllowCarRate - minAllowCarRate) / (1 + Math.exp((avgSpeed(carInRoad) - 8) / 3));
            //动态设定此时刻可以上路的车辆的数目
            int allowCarNum =(int) (maxAllowedCarNum/(1+Math.exp((carInRoad.size() - allowCarRate * graph.roadCapacity())/1000)));
            int allowPresetCarNum = 0;
            if (carPresetStartTime.get(time) != null) allowPresetCarNum = carPresetStartTime.get(time);
            allowCarNum -= allowPresetCarNum;

            int carPrioritySize = carPriority.size();
            for (int i = carPrioritySize - 1; i >= carPrioritySize - 1 - allowCarNum && i >= 0; i--) {
                Car car = carPriority.get(i);
                if (car.planTime() <= time) {
                    car.setActualTime(time);
                    carPriority.remove(car);
                }
            }
            if (carPrioritySize == 0) {
                int carNormalSize = carNormal.size();
                for (int i = carNormalSize - 1; i >= carNormalSize - allowCarNum - 1 && i >= 0; i--) {
                    Car car = carNormal.get(i);
                    if (car.planTime() <= time) {
                        car.setActualTime(time);
                        carNormal.remove(car);
                    }
                }
            }
        }
    }
}


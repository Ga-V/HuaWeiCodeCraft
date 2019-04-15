package com.huawei;
import java.util.ArrayList;

/**
 * 车辆类
 *
 * @author Ga_Vin
 * @create 2019-03-10-19:54
 */
public class Car implements Comparable<Car>, Cloneable {

    private int id;
    private int from;
    private int to;
    private int speed;
    private int planTime;
    private int priority;
    private int preset;

    private int actualTime;
    private int indexOfCurrentRoad;//当前道路在规划路径中的index
    private int status;//当前车辆状态：0--终止； 1--不出路口等待；2--出路口等待前车
    private int position;//当前在车道的位置，-1表示还未上路
    private int laneIndex;//当前车道的index
    private int isMoveOut;//判断当前车辆是否会驶出道路
    private int travelTime;//车辆的行驶时间
    private Road currentRoad;//当前行驶道路
    private Road nextRoad;//计划行驶的下一条道路
    private ArrayList<Road> path;

    /**
     * 构造器
     *
     * @param car
     */
    public Car(int[] car) {

        this.id = car[0];
        this.from = car[1];
        this.to = car[2];
        this.speed = car[3];
        this.planTime = car[4];
        this.priority = car[5];
        this.preset = car[6];

        this.actualTime = Integer.MAX_VALUE;
        this.status = 0;
        this.position = -1;
        this.indexOfCurrentRoad = -1;
        this.laneIndex = -1;
        this.isMoveOut = -1;
        this.currentRoad = null;
        this.nextRoad = null;
        this.path = new ArrayList<>();
    }

    public int id() {
        return id;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int speed() {
        return speed;
    }

    public int planTime() {
        return planTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getPreset() {
        return preset;
    }

    public int actualTime() {
        return actualTime;
    }

    public int indexOfCurrentRoad() {
        return indexOfCurrentRoad;
    }

    public int status() {
        return status;
    }

    public int position() {
        return position;
    }

    public int laneIndex() {
        return laneIndex;
    }

    public int isMoveOut() {
        return isMoveOut;
    }

    public int travelTime() {
        return travelTime;
    }

    public Road currentRoad() {
        return currentRoad;
    }

    public Road nextRoad() {
        return nextRoad;
    }

    public ArrayList<Road> path() {
        return path;
    }

    public void setActualTime(int actualTime) {
        this.actualTime = actualTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setLaneIndex(int laneIndex) {
        this.laneIndex = laneIndex;
    }

    public void setIsMoveOut(int isMoveOut) {
        this.isMoveOut = isMoveOut;
    }

    public void setTravelTime(int reachTime) {
        this.travelTime = reachTime - planTime;
    }

    public void setPreset(int preset) {
        this.preset = preset;
    }

    public void setPath(ArrayList<Road> path) {
        this.path = path;
        if (path.size() != 0) {
            this.nextRoad = path.get(0);
        }
    }

    public void removePath() {
        this.path = new ArrayList<>();
        this.nextRoad = null;
    }

    /**
     * 当前车辆的最大速度
     *
     * @return
     */
    public int currentSpeed() {
        return Math.min(speed, currentRoad.speed());
    }

    /**
     * 车辆进入下一条道路
     */
    public void moveInNextRoad() {
        this.currentRoad = nextRoad;
        if (currentRoad != null) indexOfCurrentRoad++;
        if (indexOfCurrentRoad == path.size() - 1) {
            this.nextRoad = null;
        } else {
            this.nextRoad = path.get(indexOfCurrentRoad + 1);
        }
    }

    /**
     * 排序
     *排序规则：出发时间>优先权>id
     * @param car
     * @return
     */
    @Override
    public int compareTo(Car car) {
        if (this.actualTime > car.actualTime) {
            return -1;
        } else if (this.actualTime < car.actualTime) {
            return 1;
        } else {
            if (this.getPriority() > car.getPriority()) {
                return 1;
            } else if (this.getPriority() < car.getPriority()) {
                return -1;
            } else {
                return car.id - this.id;
            }
        }
    }

    /**
     * 与graph相关联的车辆的复制
     * @param graphCopy
     * @return
     */
    public Object clone(Graph graphCopy) {
        Car car = null;
        try{
            car = (Car) super.clone();
            car.status = 0;
            car.position = -1;
            car.indexOfCurrentRoad = -1;
            car.laneIndex = -1;
            car.isMoveOut = -1;
            car.currentRoad = null;
            car.path = new ArrayList<>();
            for (int i = 0; i < path.size(); i++) {
                Road road = path.get(i);
                Node node = graphCopy.getNodeById(road.to());
                car.path.add( node.roadToCurrentNode(node.roadToIndex(road)));
            }
            if (car.path.size() != 0) {
                car.nextRoad = car.path.get(0);
            }
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return car;
    }
}

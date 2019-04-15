package com.huawei;

/**
 * 车道类
 *
 * @author Ga_Vin
 * @create 2019-03-12-15:36
 */
public class Lane implements Cloneable {

    public int carNum;//车道上车辆的数目
    public int firstCarIndex;//道路中最前位置车辆的索引，-1表示道路中没有车辆
    public int lastCarIndex;//道路中最后位置车辆的索引，-1表示道路中没有车辆
    public int[] lane;//道路中车位置信息的数组

    /**
     * 构造器
     *
     * @param length
     */
    public Lane(int length) {
        this.lane = new int[length];
        this.carNum = 0;
        this.firstCarIndex = -1;
        this.lastCarIndex = -1;
    }

    /**
     * 返回第一辆车
     *
     * @return
     */
    public int firstCarId() {
        return lane[firstCarIndex];
    }

    /**
     * 返回最后一辆车
     *
     * @return
     */
    public int lastCarId() {
        return lane[lastCarIndex];
    }

    /**
     * 道路中某个位置之前是否有前车(当前车道至少有本车)，有则返回与前车的距离，没有则返回-1;
     *
     * @param currentIndex
     * @return
     */
    public int hasFrontCar(int currentIndex) {
        if (firstCarIndex == currentIndex) {
            return -1;
        }
        for (int i = currentIndex + 1; i <= firstCarIndex; i++) {
            if (lane[i] != 0) {
                return i - currentIndex;
            }
        }
        System.out.println("是否有前车判断出错！");
        return -1;
    }

    /**
     * 与最后一辆车的距离
     *
     * @return
     */
    public int distanceWithLastCar() {
        if (lastCarIndex == -1) {
            return lane.length;
        } else {
            return lastCarIndex;
        }
    }

    /**
     * 车辆开始进入道路的处理
     * 在可以进入当前车道时才能进入，即最后一辆车的index不为0，否则不会处理
     *
     * @param car s为在该道路上最大行驶index
     * @param s
     */
    public void moveIn(Car car, int s) {
        if (lastCarIndex != 0) {
            int index = Math.min(s, distanceWithLastCar() - 1);
            lane[index] = car.id();
            car.setPosition(index);
            carNum++;
            if (lastCarIndex == -1 && firstCarIndex == -1) {
                lastCarIndex = index;
                firstCarIndex = index;
            } else {
                lastCarIndex = index;
            }
        } else {
            System.out.println("进入车道失败！");
        }
    }

    /**
     * 车辆在道路内前进
     *
     * @param car
     */
    public void moveForward(Car car) {
        int p = car.position();
        if (p == firstCarIndex) {
            int index = p + car.currentSpeed();
            if (index > lane.length - 1) {
                index = lane.length - 1;
            }
            swap(car, p, index);
            firstCarIndex = index;
        } else {
            int index = p + Math.min(car.currentSpeed(), hasFrontCar(p) - 1);
            swap(car, p, index);
        }
    }

    /**
     * 车辆位置向前移动
     * 若当前位置小于要前进到的位置，则移动车辆
     * 否则保持不变
     *
     * @param car
     * @param p
     * @param index
     */
    public void swap(Car car, int p, int index) {
        if (p < index) {
            lane[index] = car.id();
            lane[p] = 0;
            car.setPosition(index);
            if (p == lastCarIndex) {
                lastCarIndex = index;
            }
        }
    }

    /**
     * 车辆驶出当前车道
     * 只有当前车道的第一辆车能驶出当前车道
     *
     * @param car
     */
    public void moveOut(Car car) {
        int p = car.position();
        if (p == firstCarIndex) {
            lane[p] = 0;
            car.setPosition(-1);
            carNum--;
            if (p == lastCarIndex) {
                firstCarIndex = -1;
                lastCarIndex = -1;
            } else {
                firstCarIndex = findFirstCarIndex(p);
            }
        } else {
            System.out.println("车辆移出错误！");
        }
    }

    /**
     * 找第一辆车的索引
     *
     * @param index 刚出车道的第一辆车的位置
     * @return
     */
    public int findFirstCarIndex(int index) {
        int x = lastCarIndex;
        if (x != -1) {
            for (int i = index - 1; i >= x; i--) {
                if (lane[i] != 0) {
                    return i;
                }
            }
        }
        System.out.println("寻找第一辆车的索引失败！");
        return -1;
    }

    @Override
    public Object clone() {
        Lane lane = null;
        try{
            lane = (Lane) super.clone();
            lane.lane = new int[this.lane.length];
            for (int i = 0; i < this.lane.length; i++) {
                lane.lane[i] = this.lane[i];
            }
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return lane;
    }
}

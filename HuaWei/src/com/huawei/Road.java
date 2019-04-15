package com.huawei;

import java.util.ArrayList;

/**
 * 道路类
 * @author Ga_Vin
 * @create 2019-03-09-17:36
 */
public class Road  implements Comparable<Road>,Cloneable {

    private int id;
    private int length;
    private int speed;
    private int channel;
    private int from;
    private int to;
    private int isDuplex;

    private double weight;
    public int carNumInRoad;
    private Lane[] lanes;
    private ArrayList<Integer> carQueue;

    /**
     * 构造器
     * @param roads
     */
    public Road(int[] roads) {
        this.id = roads[0];
        this.length = roads[1];
        this.speed = roads[2];
        this.channel = roads[3];
        this.from = roads[4];
        this.to = roads[5];
        this.isDuplex = roads[6];
        this.weight = length / (speed * (1 + channel * 0.2)); //道路权值
        this.carNumInRoad = 0;
        this.lanes = new Lane[channel];
        for (int i = 0; i < channel; i++) {
            lanes[i] = new Lane(length);
        }
        this.carQueue = new ArrayList<>();
    }

    public int id() {
        return id;
    }

    public int length() {
        return length;
    }

    public int speed() {
        return speed;
    }

    public int channel() {
        return channel;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int isDuplex() {
        return isDuplex;
    }

    public double weight() {
        return weight;
    }

    public ArrayList<Integer> carQueue() {
        return carQueue;
    }

    public void setCarQueue(ArrayList<Integer> carQueue) {
        this.carQueue = carQueue;
    }

    /**
     * 返回指定index的车道
     * @param i
     * @return
     */
    public Lane lane(int i) {
        return lanes[i];
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    //返回道路id
    public String toString() {
        return String.format("%d",id);
    }

    @Override
    public int compareTo(Road road) {
        return road.id - this.id;
    }

    @Override
    public Object clone() {
        Road road = null;
        try{
            road = (Road) super.clone();
            road.lanes =new Lane[channel];
            for (int i = 0; i < channel; i++) {
                road.lanes[i] =(Lane) lanes[i].clone();
            }
            road.carQueue = new ArrayList<>();
            for (int i = 0; i < carQueue.size(); i++) {
                road.carQueue.add(carQueue.get(i));
            }
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return road;
    }
}

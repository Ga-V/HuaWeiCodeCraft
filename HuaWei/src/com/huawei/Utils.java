package com.huawei;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ga_Vin
 * @create 2019-03-10-19:29
 */
public class Utils {

    /**
     * 读取道路
     * @param pathName
     * @param map1
     * @param map2
     */
    public static void readRoad(String pathName, Map map1,Map map2) {
        try {
            File file = new File(pathName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                bufferedReader.readLine();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line.trim();
                    line = line.substring(1, line.length() - 1);
                    String s[] = line.split(", ");
                    int[] temp = new int[s.length];
                    for (int i = 0; i < s.length; i++) {
                        temp[i] = Integer.parseInt(s[i]);
                    }
                    Road road = new Road(temp);
                    map1.put(temp[0]*temp[5],road);
                    map2.put(temp[0]*temp[4],road);
                    if (temp[6] == 1) {
                        int x = temp[4];
                        temp[4] = temp[5];
                        temp[5] = x;
                        Road oppositeRoad = new Road(temp);
                        map1.put(temp[0]*temp[5],oppositeRoad);
                        map2.put(temp[0]*temp[4],oppositeRoad);
                    }
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件！");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 读取节点
     * @param pathName
     * @param arrayList
     */
    public static void readNode(String pathName, List arrayList) {
        try {
            File file = new File(pathName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                bufferedReader.readLine();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line.trim();
                    line = line.substring(1, line.length() - 1);
                    String s[] = line.split(", ");
                    int[] temp = new int[s.length];
                    for (int i = 0; i < s.length; i++) {
                        temp[i] = Integer.parseInt(s[i]);
                    }
                    arrayList.add(temp);
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件！");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 读取车辆
     * @param pathName
     * @param hashMap
     */
    public static void readCar(String pathName, HashMap hashMap) {
        try {
            File file = new File(pathName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                bufferedReader.readLine();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line.trim();
                    line = line.substring(1, line.length() - 1);
                    String s[] = line.split(",");
                    int[] temp = new int[s.length];
                    for (int i = 0; i < s.length; i++) {
                        temp[i] = Integer.parseInt(s[i].trim());
                    }
                    hashMap.put(temp[0], new Car(temp));
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件！");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 读取答案
     * @param pathName
     * @param arrayList
     */
    public static void readAnswer(String pathName, List arrayList) {
        try {
            File file = new File(pathName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                bufferedReader.readLine();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line.trim();
                    line = line.substring(1, line.length() - 1);
                    String s[] = line.split(",");
                    int[] temp = new int[s.length];
                    for (int i = 0; i < s.length; i++) {
                        temp[i] = Integer.parseInt(s[i].trim());
                    }
                    arrayList.add(temp);
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件！");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 写答案
     * @param pathName
     * @param answer
     */
    public static void writeAnswer(String pathName, String answer) {
        try {
            File file = new File(pathName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(answer);
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println("写入文件内容操作出错");
            e.printStackTrace();
        }
    }
}

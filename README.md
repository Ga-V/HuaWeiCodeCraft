## HuaWeiCodeCraft2019华为软件精英挑战赛
队伍：WhiteGive 成绩：成渝赛区复赛第六名
### 路径规划
  主要的算法还是基于动态权值的dijkstra最短路径算法。在车辆即将上路时进行最短路径规划，车辆上路后的当前道路和接下来要走的三条道路的权值都增加。增加的方法是在当前道路权值的基础上乘以一个与车辆速度相关的系数。发车策略是分速度发车，且保证当前在道路上的车辆不超过阈值。
### 解死锁
  当遇到死锁时，找到形成死锁的环形道路，并重新规划死锁道路的第一优先级车辆的路径，不经过当前规划的下一条道路，以当前节点开始到目标节点重新规划路径。然后保存车辆路径信息，回到0时刻重新调度。
### 感想
  写判题器写了太久，而且没有想到比较好的算法和策略去进一步优化，很遗憾。还是太菜了呀！

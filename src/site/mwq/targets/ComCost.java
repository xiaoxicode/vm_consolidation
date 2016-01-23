package site.mwq.targets;

import site.mwq.gene.Individual;
import site.mwq.main.DataSet;

/**
 * 给出一个解的通信代价，通信量*通信距离
 * @author Email:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:16
 */
public class ComCost implements ObjInterface {

	@Override
	public double objValue(Individual ind) {
		
		int row = DataSet.comMatrix.length;
		int col = DataSet.comMatrix[0].length;
		int dist = 0;
		double cost = 0;
		
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				if(DataSet.comMatrix[i][j]!=0){
					dist = vmDistance(ind, i, j);
					cost += dist*DataSet.comMatrix[i][j];
				}
			}
		}
		
		return cost;
	}
	
	/**
	 * 计算两个虚拟机的通信距离，用经过的交换机个数来衡量
	 * 使用三层树形交换机架构，分为核心层、聚集层、接入层、机架、物理机
	 * 从上到下，交换机和物理机数目为 2 3 6 18
	 * 
	 * 同一个物理机距离为			0
	 * 同一个机架交换机距离为		1
	 * 同一个聚集交换机距离为		3
	 * 否则距离为				5
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private int vmDistance(Individual ind, int i,int j){
		
		if(i==j){		//虚拟机编号相等，返回0
			return 0;
		}
		int idi = 0;
		try{
			idi = ind.vmHostMap.get(i);
		}catch(Exception e){
			System.err.println("error in ComCost");
			System.exit(1);
		}
		int idj = 0;
		
		try{
			idj	= ind.vmHostMap.get(j);
		}catch (Exception e){
			System.err.println("another error in ComCost");
			System.exit(1);
		}
		
		if(idi==idj){	//物理机编号相等，返回0
			return 0;
		}
		
		idi	/= 3;
		idj	/= 3;
		
		if(idi==idj){	//机架编号相等，返回1
			return 1;
		}
		
		idi /= 2;
		idj /= 2;
		
		if(idi==idj){	//聚集层交换机编号相等，返回3，否则返回5
			return 3;
		}
		
		return 5;
	}

}

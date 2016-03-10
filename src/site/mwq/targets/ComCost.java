package site.mwq.targets;

import site.mwq.gene.Individual;
import site.mwq.main.DataSet;
import site.mwq.utils.Utils;

/**
 * 给出一个解的通信代价，通信量*通信距离
 * 本系统假设的集群为：
 * 有2个核心层交换机
 * 有3个聚集层交换机
 * 有6个机架交换机（每个聚集层交换机有2个机架交换机）
 * 有18台物理机（每个机架交换机有3台物理机）
 * 
 * @author Email:qiuweimin@126.com
 * @version 创建时间：2015年12月23日 上午11:53:16
 */
public class ComCost implements ObjInterface {

	@Override
	public double objVal(Individual ind) {
		
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
		int idi = 0;	//虚拟机i所在的物理机编号
		try{
			idi = ind.vmHostMap.get(i);
		}catch(Exception e){
			System.err.println("error in ComCost");
			System.exit(1);
		}
		int idj = 0;	//虚拟机j所在的物理机编号
		
		try{
			idj	= ind.vmHostMap.get(j);
		}catch (Exception e){
			System.err.println("another error in ComCost");
			System.exit(1);
		}
		
		return Utils.getPmDis(idi, idj);
	}

}

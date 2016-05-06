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
		int cost = 0;
		int tempDist,tempCost;
		
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				
				tempCost = DataSet.comMatrix[i][j];
				if(tempCost==0){continue;}
				tempDist = Utils.vmDistance(ind, i, j);

				switch(tempDist){       //用加法代替乘法。
				case 0:break;
				case 1:cost += tempCost;break;
				case 3:cost += (tempCost<<1)+tempCost;break;
				case 5:
					cost += (tempCost<<2)+tempCost;
				}
			}
		}
		
		return cost;
	}
	
}

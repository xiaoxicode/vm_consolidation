package site.mwq.gene;

import java.util.Comparator;

import site.mwq.targets.Balance;
import site.mwq.targets.ComCost;
import site.mwq.targets.MigCnt;
import site.mwq.targets.MigTime;
import site.mwq.targets.ObjInterface;
import site.mwq.targets.PmCnt;

/**
 * 根据目标值将种群排序，优先级：移动次数 > 通信代价 > 所使用的pm数量
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
public class IndComp implements Comparator<Individual>{
	private MigCnt mc;	//迁移次数
	private ComCost cc;	//通信代价
	private PmCnt pc;	//物理机数目
	private Balance ba; //平衡程度
	private MigTime mt;  //迁移时间

	int objNum = 5;
	ObjInterface[] objs = new ObjInterface[objNum];
	
	public IndComp(){
		mc = new MigCnt();
		cc = new ComCost();
		pc = new PmCnt();
		ba = new Balance();
		mt = new MigTime();
		
		//重要程度排序
//		objs[0] = ba;
//		objs[1] = mc;
//		objs[2] = cc;
//		objs[3] = pc;
//		objs[4] = mt;
		
		
		objs[0] = mc;
		objs[1] = pc;
		objs[2] = cc;
		objs[3] = ba;
		objs[4] = mt;
		
	}
	
	@Override
	public int compare(Individual i1, Individual i2) {
		
		for(int i=0;i<objNum;i++){
			if(objs[i].objVal(i1)<objs[i].objVal(i2)){
				return -1;
			}else if(objs[i].objVal(i1)>objs[i].objVal(i2)){
				return 1;
			}
		}
		
		return 0;
	}
}

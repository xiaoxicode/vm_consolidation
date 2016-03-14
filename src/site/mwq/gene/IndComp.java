package site.mwq.gene;

import java.util.Comparator;

import site.mwq.targets.ComCost;
import site.mwq.targets.MigCnt;
import site.mwq.targets.PmCnt;

/**
 * 根据目标值将种群排序，优先级：移动次数 > 通信代价 > 所使用的pm数量
 * @author Email:qiuweimin@126.com
 * @date 2016年1月23日
 */
public class IndComp implements Comparator<Individual>{
	private MigCnt mc;
	private ComCost cc;
	private PmCnt pc;
	
	public IndComp(){
		mc = new MigCnt();
		cc = new ComCost();
		pc = new PmCnt();
	}
	
	@Override
	public int compare(Individual i1, Individual i2) {
		
		double mc1 = mc.objVal(i1);
		double mc2 = mc.objVal(i2);
		
		if(mc1<mc2){
			return -1;
		}else if(mc1>mc2){
			return 1;
		}else{
			
			double pc1 = pc.objVal(i1);
			double pc2 = pc.objVal(i2);
			if(pc1<pc2){
				return -1;
			}else if(pc1>pc2){
				return 1;
			}else{
				double cc1 = cc.objVal(i1);
				double cc2 = cc.objVal(i2);

				if(cc1<cc2){
					return -1;
				}else if(cc1>cc2){
					return 1;
				}
			}
		}
		return 0;
	}
}

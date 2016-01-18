package site.mwq.utils;

import site.mwq.utils.Utils;

/**
 * 
 * 预测迁移性能：迁移时间、停机时间、数据量
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class Migration {
	
	/** 内存剩余阈值
	 * 当剩余这些资源时，热迁移不再进行
	 * */
	private static final double Vthd = 50;
	
	/**迁移的最大轮数*/
	private static final int max_round = 30;
	
	/**
	 * 参数a,b,c是用来计算伽马(r)的参数
	 */
//	private static final double a = -0.0463;
//	private static final double b = -0.0001;
//	private static final double c = 0.3586;
	
	/**在目的物理机恢复VM的时间，暂不考虑*/
	private static final double Tresume = 0;
	
	/**
	 * 
	 * @param Vmem	虚拟机内存
	 * @param D		虚拟机脏页率
	 * @param R		可用带宽
	 * @return double[] 迁移时间、停机时间、迁移的数据量
	 */
	public static double[] performaceModel(double Vmem,double D,double R){
		
		double[] res = new double[3];
		double Tmig=0,Tdown=0,Vmig=0;
		
		double Vi = Vmem;
		double Vi1 = 0;
		double Ti = 0;
		double Ti1 = 0;
//		double r = 0;
//		double Wi1 = 0;		//表示可写工作集 writable working set TODO 暂不考虑可写工作集
		
		for(int i=0;i<max_round;i++){
			Ti = Vi / R;
//			r = a * Ti + b * D + c;
//			Wi1 = r * Ti * D;		//下一轮的可写工作集
//			Vi1 = Ti * D - Wi1;
			Vi1 = Ti * D;
			
			Tmig += Ti;	//累加
			
			Vmig += Vi;
			
			System.out.print((int)Ti+" ");
			
			if(Vi1<= Vthd || Vi1 > Vi || i==(max_round-1)){	//最后一轮
				Vi1 = Ti * D;
				Ti1 = Vi1 / R;
				Tdown = Ti1 + Tresume;
				break;
			}
			Vi = Vi1;
		}
		
		System.out.println();
		res[0] = Tmig;
		res[1] = Tdown;
		res[2] = Vmig;
		return res;
	}
	
	public static void main(String[] args) {
		
		Utils.disArray(Migration.performaceModel(1000,20,21));
	}
	
}

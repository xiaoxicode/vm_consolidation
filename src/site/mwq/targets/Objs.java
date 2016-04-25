package site.mwq.targets;

public class Objs {
	
	/**优化目标，5个*/
	public static int OBJNUM = 5;
	public static ObjInterface[] OBJS = new ObjInterface[OBJNUM];
	
	static {
		OBJS[0] = new PmCnt();
		OBJS[1] = new ComCost();
		OBJS[2] = new MigCnt();
		OBJS[3] = new Balance();
		OBJS[4] = new MigTime();

	}
}

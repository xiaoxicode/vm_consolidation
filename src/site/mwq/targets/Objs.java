package site.mwq.targets;

public class Objs {
	
	//TODO 将目标个数改为3
	public static int OBJNUM = 3;
	public static ObjInterface[] OBJS = new ObjInterface[OBJNUM];
	
	static {
		OBJS[0] = new PmCnt();
		OBJS[1] = new ComCost();
		OBJS[2] = new MigCnt();
	//	OBJS[3] = new Balance();
	//	OBJS[4] = new MigTime();

	}
}

package site.mwq.targets;

public class Objs {
	
	public static int OBJNUM = 5;
	public static ObjInterface[] OBJS = new ObjInterface[OBJNUM];
	
	static {
		OBJS[0] = new PmCnt();
		OBJS[1] = new ComCost();
		OBJS[2] = new MigTime();
		OBJS[3] = new MigCnt();
		OBJS[4] = new Balance();
	}
}

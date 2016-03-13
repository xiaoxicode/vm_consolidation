package site.mwq.rmi;

import java.util.Comparator;

/**
 * 将vm按照vsr降序排列
 * @author Email:qiuweimin@126.com
 * @date 2016年2月24日
 */
public class VMComparator implements Comparator<VM> {

	@Override
	public int compare(VM vm1, VM vm2) {
		
		if(vm1.vsr > vm2.vsr){
			return -1;
		}else if(vm1.vsr < vm2.vsr){
			return 1;
		}
		
		return 0;
	}

}

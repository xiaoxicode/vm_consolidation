package site.mwq.rmi;

import java.util.Comparator;

/**
 * 将pm安装volume负载值降序排列
 * @author Email:qiuweimin@126.com
 * @date 2016年2月24日
 */
public class PMComparator implements Comparator<PM> {

	@Override
	public int compare(PM pm1, PM pm2) {
		
		if(pm1.volume > pm2.volume){
			return -1;
		}else if(pm1.volume < pm2.volume){
			return 1;
		}
		
		return 0;
	}

}

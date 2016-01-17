package site.mwq.model;

import java.util.ArrayList;

/**
 * 网络拓扑，由核心层交换机列表来表示
 *
 * @author Email:qiuweimin@126.com
 * @version 2016年1月14日
 */
public class Topology {
	public ArrayList<CoreSwitch> coreSwitch;
	
	public Topology(){
		this.coreSwitch = new ArrayList<CoreSwitch>();
	}
}

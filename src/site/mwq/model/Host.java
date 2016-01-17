package site.mwq.model;

import java.util.HashSet;

/**
 *表示一台物理机 
 *
 */
public class Host {
	
	/**标号*/
	public int id;
	
	public int mem;
	public int cpu;
	
	/**虚拟机列表*/
	HashSet<VM> vms;
	
	/**
	 * 物理机构造函数，虚拟机列表初始化为空列表
	 * @param mem	内存
	 * @param cpu	cpu核数
	 */
	public Host(int mem,int cpu){
		this.mem = mem;
		this.cpu = cpu;
		this.vms = new HashSet<VM>();
	}

	@Override
	public String toString() {
		
		String str = "";
		
		str += "Host(id=" + id + ", mem=" + mem + ", cpu=" + cpu + ")"+":{";
		
		for(VM vm :vms){
			str += vm.id+" ";
		}
		
		return str+"}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cpu;
		result = prime * result + id;
		result = prime * result + mem;
		result = prime * result + ((vms == null) ? 0 : vms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Host other = (Host) obj;
		if (cpu != other.cpu)
			return false;
		if (id != other.id)
			return false;
		if (mem != other.mem)
			return false;
		if (vms == null) {
			if (other.vms != null)
				return false;
		} else if (!vms.equals(other.vms))
			return false;
		return true;
	}

	
}

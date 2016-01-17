package site.mwq.model;

/**
 *表示一台虚拟机 ，识别一个VM主要通过id
 *equals方法只关注id
 *mem和cpu是次要因素
 */
public class VM {
	
	public int id;
	
	public int mem;
	public int cpu;
	
	/**VM所在的host*/
	public Host host;
	
	/**
	 * 虚拟机构造函数，所在的host初始化为null
	 * @param mem	内存
	 * @param cpu	cpu核数
	 */
	public VM(int mem,int cpu){
		this.mem = mem;
		this.cpu = cpu;
		this.host = null;
	}

	@Override
	public String toString() {
		
		String str = "";
		str += "VM(id=" + id + ", mem=" + mem + ", cpu=" + cpu + ")";
		if(host != null){
			str += "[host: "+host.id+"]";
		}else{
			str += "[host: null]";
		}
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cpu;
		result = prime * result + id;
		result = prime * result + mem;
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
		VM other = (VM) obj;
		if (cpu != other.cpu)
			return false;
		if (id != other.id)
			return false;
		if (mem != other.mem)
			return false;
		return true;
	}

}

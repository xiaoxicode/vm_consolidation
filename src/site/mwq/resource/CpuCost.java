package site.mwq.resource;

public class CpuCost {

	//用于数据计算的常量
	public static int i = 1000000, j = 3, l = 16452, k = 31455, m = 9823, jj = 2342;
	
	/**
	 * 动态增加cpu比例，从40%到80%，差不多每一秒增加一次
	 */
	public static void setCpuPercent(){
		int busy = 400;
		int idle = 1000 - busy;
		long count = 0;
		long print = 0;
		while (true) {
			long start = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			if (busy <= 800) {
				busy += 5;
				idle -= 5;
			}
			while (end - start < busy) { // 运行busy毫秒

				mathCal();				//执行数学计算
				
				count++;
				if (count % 10000000 == 0) {
					count = 0;
					print++;
					System.out.println(print);
				}
				end = System.currentTimeMillis();
			}
			try {
				Thread.sleep(idle); 	// 睡眠400毫秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 让CPU使用固定的百分比
	 * @param percent
	 */
	public static void setCpuPercent(double percent) {
		int busy = (int) (1000 * percent);
		int idle = 1000 - busy;
		long count = 0;
		long print = 0;
		while (true) {
			long start = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while (end - start < busy) { // 运行busy毫秒

				mathCal();				//执行数学计算
				
				count++;
				if (count % 10000000 == 0) {
					count = 0;
					print++;
					System.out.println(print);		//打印计数
				}
				end = System.currentTimeMillis();
			}
			try {
				Thread.sleep(idle); 	// 睡眠400毫秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**数学计算*/
	public static void mathCal() {
		m = m ^ l;
		k = (k / m * jj) % i;
		l = j * m * k;
		i = (j * k) ^ m;
		k = (k / m * jj) % i;
		m = m ^ l;
		m = m ^ l;
		i = (j * k) ^ m;
		k = (k / m * jj) % i;
		m = i * i * i * i * i * i * i; // m=k*l*jj*l;
		m = m ^ l;
		k = (k / m * jj) % i;
		l = j * m * k;
		i = (j * k) ^ m;
		l = (k / m * jj) % i;
		m = m ^ l;
		m = m ^ l;
		i = (j * k) ^ m;
		k = (k / m * jj) % i;
		m = k * k * k * k * k - m / i;
	}
	
	public static void main(String[] args) {
		setCpuPercent(900);
	}
	

}

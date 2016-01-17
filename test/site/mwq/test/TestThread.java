package site.mwq.test;

public class TestThread implements Runnable{

	@Override
	public void run() {
		for(int i=0;i<10;i++){
			System.out.println("thread2 "+i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
}


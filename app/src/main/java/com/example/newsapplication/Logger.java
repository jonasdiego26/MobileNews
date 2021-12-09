package es.upm.hcid.pui.assignment;

import java.util.Date;

public class Logger {
	public static final int ERROR = -1;
	public static final int INFO = 1;
	
	public static int level = INFO;
	
	public static final void log(int level, String message){
		if (level<=Logger.level){
			switch(level){
			case ERROR:
				System.err.println("["+printCurrentDate()+"] ERROR "+message);
				break;
			case INFO:
				System.out.println("["+printCurrentDate()+"] INFO "+message);
				break;
			}
			
		}
	}

	private static String printCurrentDate() {
		 return Utils.dateToString(new Date());
	}
}

package ir.iranet.vms.util;

import java.io.IOException;
import java.util.logging.*;

public class VMSLogger 
{
		
	private static Logger logger;
	static {
		try {
			
			LogManager lm = LogManager.getLogManager();
			logger = Logger.getLogger("VMSLogger");			
			lm.addLogger(logger);
			
			FileHandler fh = new FileHandler ("vms.log");						
			fh.setFormatter(new SimpleFormatter());
			
			FileHandler fh2 = new FileHandler ("vms.xml");
			fh2.setFormatter(new XMLFormatter());
			
			logger.setLevel(Level.OFF);
			logger.addHandler(fh);
			logger.addHandler(fh2);
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log (Level level, String str )
	{	
		logger.log(level, str);
	}
	
}

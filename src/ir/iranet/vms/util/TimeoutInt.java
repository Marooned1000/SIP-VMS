package ir.iranet.vms.util;

import ir.iranet.vms.SessionHandler;

import java.io.IOException;
import java.util.logging.Level;


enum SessionState {
	start, s0, s1, s2, s3, s4, s5, s6,s7  
}
public class TimeoutInt extends Thread
{
	
		SessionState state;
		/** Length of timeout */
		private int m_length;		
		SessionHandler session;
		boolean stop;

		/**
		  * Creates a timer of a specified length
		  * @param	length	Length of time before timeout occurs
		  */
		public TimeoutInt ( int length, SessionHandler s)
		{		
			m_length = length;
			session=new SessionHandler();
			session = s;
			stop=false;
		}	
		
		public TimeoutInt ( int length)
		{
			m_length = length;
			stop=false;
			
		}
		
		
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		public boolean getStop() {
			return this.stop;
		}
		
		
		
		/** Performs timer specific code */
		public void run()
		{			
				// Put the timer to sleep
				try
				{ 		
					while((m_length-->0) && (stop==false))
					{
						Thread.sleep(1);
					}
					if(stop==false)
					{
						System.out.println("Time out.");
						timeout();
					}
					else
					{
						//Thread thread=Thread.currentThread();						
						//thread=null;
						System.out.println("Timer is stoped.");
					}
				}
				catch (InterruptedException ioe) 
				{
					//continue;
				}
		}

		// Override this to provide custom functionality
		
		public void timeout()
		{			
			try{
				try {
					session.TimeOut();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch(IOException e)
			{
				
			}
		}
		
		public void Wait()
		{
			try
			{ 
									
				Thread.sleep(m_length);
				
				
			}
			catch (InterruptedException ioe) 
			{
				//continue;
			}
		}

		
	}
	

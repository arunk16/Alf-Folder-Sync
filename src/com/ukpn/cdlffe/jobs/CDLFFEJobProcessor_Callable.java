package com.ukpn.cdlffe.jobs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.alfresco.webservice.util.AuthenticationUtils;
import org.apache.log4j.Logger;

import com.ukpn.cdlffe.core.DirectoryProcessor;
import com.ukpn.cdlffe.core.DirectoryProcessorFactory;
import com.ukpn.cdlffe.util.Cons;

public class CDLFFEJobProcessor_Callable implements Callable<Integer> {
	
	private static final Logger logger = Logger.getLogger(CDLFFEJobProcessor_Callable.class);
	
	private BlockingQueue<CDLFFEJobVO> cdlJobVOQueue;
	
	private String threadName;
	
	private boolean running = true;
	
	private int processedCount;

	public CDLFFEJobProcessor_Callable(
			BlockingQueue<CDLFFEJobVO> cdlJobVOQueue, String threadName) {
		super();
		this.cdlJobVOQueue = cdlJobVOQueue;
		this.threadName = threadName;
	}

	@Override
	public Integer call() throws Exception {
		while (running) {
		      // check if current Thread is interrupted
		      checkInterruptStatus();

		      try {
		        // get message from message queue with timeout of 10ms
		    	  CDLFFEJobVO job = cdlJobVOQueue.poll(10, TimeUnit.MILLISECONDS);

		        if (job != null) {
		          // do message processing here
		        	DirectoryProcessor alfrescoDirProcessor = new DirectoryProcessorFactory().createDirectoryProcessor();
		        	AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
		    		//process the content between source and targets
		    		alfrescoDirProcessor.captureSourceTargetAndProcessContentSync(job.getSourceFolder(), job.getDestinationFolder());  	
		          	
		    		AuthenticationUtils.endSession();
		          logger.debug(threadName + ": processed Job "
		              + job.toString());

		          // increment processed message count
		          processedCount++;
		        } else {
		        	logger.debug(threadName
		              + ": waiting for message in the blocking queue");
		        }

		        // for demo purpose sleep current thread for 500ms otherwise
		        // program will complete very quickly
		        Thread.sleep(500);
		      } catch (InterruptedException e) {
		        throw new CDLFFEProcessiongException(threadName
		            + " thread interrupted while waiting for message", e);
		      }
		    }

		    // return result
		    return processedCount;
	}
	
	private void checkInterruptStatus() throws CDLFFEProcessiongException {
	    if (Thread.interrupted()) {
	      throw new CDLFFEProcessiongException("Thread was interrupted");
	    }
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @return the processedCount
	 */
	public int getProcessedCount() {
		return processedCount;
	}

	
}

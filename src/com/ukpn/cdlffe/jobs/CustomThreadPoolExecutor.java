package com.ukpn.cdlffe.jobs;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
	
	private static final Logger logger = Logger.getLogger(CustomThreadPoolExecutor.class);

	  /**
	   * CustomThreadPoolExecutor Constructor.
	   */
	  public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize,
	      long keepAliveTime, TimeUnit unit,
	      BlockingQueue<Runnable> workQueue) {
	    super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
	  }

	  @Override
	  public void beforeExecute(Thread t, Runnable r) {
	    super.beforeExecute(t, r);
	    logger.debug("After calling beforeExecute() method for a thread "
	        + r);
	  }

	  @Override
	  public void afterExecute(Runnable r, Throwable t) {
	    super.afterExecute(r, t);
	    logger.debug("After calling afterExecute() method for a thread "
	        + r);
	  }

	  @Override
	  public void terminated() {
	    super.terminated();
	    logger.debug("Threadpool terminated");
	  }}

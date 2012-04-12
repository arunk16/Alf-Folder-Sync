package com.ukpn.cdlffe.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.ukpn.cdlffe.core.DirectoryProcessor;
import com.ukpn.cdlffe.core.DirectoryProcessorFactory;
import com.ukpn.cdlffe.jxl.ExcelDealer;
import com.ukpn.cdlffe.util.XPathUtils;

/**
 * Job runner main class allocates the CDL folder sync jobs to worker threads
 * and controls them via ThreadExecutor.
 * 
 * @author arun
 * 
 */
public class CDLFFE_JobRunnerMain {
	
	private static final Logger logger = Logger.getLogger(CDLFFE_JobRunnerMain.class);

	private static final int MSG_QUEUE_SIZE = 100;

	private static final int THREAD_POOL_SIZE = 2;

	// create BlockingQueue to put cdl sync job objects
	private BlockingQueue<CDLFFEJobVO> cdlJobVOQueue = new ArrayBlockingQueue<CDLFFEJobVO>(
			MSG_QUEUE_SIZE);

	private ThreadPoolExecutor executor;

	private HashMap<String, CDLFFEJobProcessor_Callable> callableMap;

	private ArrayList<Future<Integer>> futurList;

	public CDLFFE_JobRunnerMain() {
		// create a thread pool with fixed no of threads
		executor = new CustomThreadPoolExecutor(THREAD_POOL_SIZE,
				THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		callableMap = new HashMap<String, CDLFFEJobProcessor_Callable>();

		// create list to store reference to Future objects
		futurList = new ArrayList<Future<Integer>>();
	}

	private void createAndSubmitTasks() {
		// create Callables
		CDLFFEJobProcessor_Callable callable1 = new CDLFFEJobProcessor_Callable(
				cdlJobVOQueue, "CDL_JOBProcessor_1");
		callableMap.put(callable1.getThreadName(), callable1);

		CDLFFEJobProcessor_Callable callable2 = new CDLFFEJobProcessor_Callable(
				cdlJobVOQueue, "CDL_JOBProcessor_2");
		callableMap.put(callable2.getThreadName(), callable2);

		// submit callable tasks
		Future<Integer> future;
		future = executor.submit(callable1);
		futurList.add(future);

		future = executor.submit(callable2);
		futurList.add(future);
	}

	private void printWorkersResult() {
		for (Future<Integer> f : futurList) {
			try {
				Integer result = f.get(1000, TimeUnit.MILLISECONDS);
				logger.debug(f + " result. Processed jobs " + result);
			} catch (InterruptedException e) {
				logger.debug(e.getMessage());
			} catch (ExecutionException e) {
				logger.debug(e.getCause().getMessage());
			} catch (TimeoutException e) {
				logger.debug(e.getMessage());
			} catch (CancellationException e) {
				logger.debug(e.getMessage());
			}
		}
	}

	/**
	 * Populate the work queue with cdl jobs.
	 * 
	 * @throws InterruptedException
	 */
	private void populateJobVOQueue() throws Exception {
		// put CDl Job VO objects in BlockingQueue
		List<String> sourceList = ExcelDealer.getInstance().getSourceDir();
		HashMap<String,List<String>> sourceTargetMap = ExcelDealer.getInstance().getCdlFfeDirectoryMap();
		for(String sourceDir : sourceList){
			String sourcePath = XPathUtils.generateSourceFolderXPath(sourceDir);
			List<String> destList = sourceTargetMap.get(sourceDir);
			if(destList != null){
				for(String destDir : destList){
					String destinationPath = XPathUtils.generateDestinationFolderXPath(destDir);
					// this method will put cdl JobVO object in the queue
					cdlJobVOQueue.put(new CDLFFEJobVO(sourcePath, destinationPath));
				}
			}
		}	
	}

	private void printProcessorStatus() throws InterruptedException {
		// print processor status until all orders are processed
		while (!cdlJobVOQueue.isEmpty()) {
			for (Map.Entry<String, CDLFFEJobProcessor_Callable> e : callableMap
					.entrySet()) {
				logger.debug(e.getKey() + " processed job count: "
						+ e.getValue().getProcessedCount());
			}
			Thread.sleep(1000);
		}
	}

	/**
	 * Stop the threads gracefully or harshly using the forceShutdown flag
	 * 
	 * @param forceShutdown
	 */
	private void shutDown(boolean forceShutdown) {
		if (!forceShutdown) {
			// shutdown() method will mark the thread pool shutdown to true
			executor.shutdown();
			logger.debug("Executor shutdown status "
					+ executor.isShutdown());
			logger.debug("Executor terninated status "
					+ executor.isTerminated());

			// Mark threads to return threads gracefully.
			for (Map.Entry<String, CDLFFEJobProcessor_Callable> jobProcessor : callableMap
					.entrySet()) {
				jobProcessor.getValue().setRunning(false);
			}
		} else {

			for (Future<Integer> f : futurList) {
				f.cancel(true);
			}

			// shutdown() method will mark the thread pool shutdown to true
			executor.shutdownNow();
		}
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) throws Exception {         
                
          while (true) {
        	  	//check CDL-FFE folder structure is available or create it
        	  	DirectoryProcessor dirProcessorObj = new DirectoryProcessorFactory().createDirectoryProcessor();
        	  	dirProcessorObj.createCDLStructure();
        	  
                logger.debug("CDL_FFE porcessor starting jobs ");
                // flag for if thread pool should be shutdown forcefully or gracefully
                final boolean FORCE_SHUTDOWN = false;
                
                CDLFFE_JobRunnerMain mainProcessor = new CDLFFE_JobRunnerMain();
                // create the callable tasks and submit to executor
                mainProcessor.createAndSubmitTasks();

                // sleeping for 1 min before starting new set of worker threads
                Thread.sleep(60000);
                logger.debug("Main thread awaken after 2 mins. "
                        + "Putting JobVO objects in blocking queue");

                // populate orderVO in blocking queue
                mainProcessor.populateJobVOQueue();

                // print processor status
                mainProcessor.printProcessorStatus();

                // shutdown thread pool
                mainProcessor.shutDown(FORCE_SHUTDOWN);

                // print final statistics
                mainProcessor.printWorkersResult();
                
                logger.debug("Executor terminated status "
                        + mainProcessor.executor.isTerminated());
                
                logger.debug("CDL_FFE porcessor finished jobs");
            }
	}

}

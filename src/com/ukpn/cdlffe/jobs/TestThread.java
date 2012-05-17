package com.ukpn.cdlffe.jobs;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.alfresco.webservice.util.AuthenticationUtils;

import com.ukpn.cdlffe.core.DirectoryProcessorImpl;
import com.ukpn.cdlffe.jxl.ExcelDealer;
import com.ukpn.cdlffe.util.Cons;
import com.ukpn.cdlffe.util.XPathUtils;

/**
 * A Thread tester class to test the CDL & FFE content copy
 * 
 * @author arun
 *
 */
public class TestThread implements Runnable{

	@Override
	public void run() {
		while(true){
			//first put thread to sleep for 30 sec
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1){
				e1.printStackTrace();
			}
			
			DirectoryProcessorImpl processor = new DirectoryProcessorImpl();
			try {
				AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
				List<String> sourceList = ExcelDealer.getInstance().getSourceDir();
				HashMap<String,List<String>> sourceTargetMap = ExcelDealer.getInstance().getCdlFfeDirectoryMap();
				for(String sourceDir : sourceList){
					String sourcePath = XPathUtils.generateSourceFolderXPath(sourceDir);
					List<String> destList = sourceTargetMap.get(sourceDir);
					if(destList != null){
						for(String destDir : destList){
							String destinationPath = XPathUtils.generateDestinationFolderXPath(destDir);
							// process the source directory and copy the content to target
							processor.captureSourceTargetAndProcessContentSync(sourcePath, destinationPath);
						}
					}
					break; //TODO remove this to do full source - target sync
				}
				AuthenticationUtils.endSession();
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String []args) throws Exception{
		Thread t = new Thread(new TestThread());
		t.start();
	}
	
	

}

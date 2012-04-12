package com.ukpn.cdlffe.core;

/**
 * Factory gives DirectoryProcessor object to threads.
 * 
 * @author arun
 *
 */
public class DirectoryProcessorFactory {
	
	public DirectoryProcessor createDirectoryProcessor(){
		return new DirectoryProcessorImpl();
	}

}

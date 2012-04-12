package com.ukpn.cdlffe.core;


/**
 * Interface specifies the operations to achieve CDL structure equivalent to Documentum  
 * target structure known as CDL-FFE and serves published content via mapped network drive to external 
 * systems.
 * 
 * @author arun
 *
 */
public interface DirectoryProcessor {
	/**
	 * Creates CDL structure in Alfresco
	 * @throws Exception
	 */
	public void createCDLStructure() throws Exception;
	
	/**
	 * Sync content from source folder to destination folder
	 * @throws Exception
	 */
	public void captureSourceTargetAndProcessContentSync(String sourceFolderPath, String destinationFolderPath) throws Exception;

}

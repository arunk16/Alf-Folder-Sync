package com.ukpn.cdlffe.core;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.content.Content;
import org.alfresco.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.webservice.repository.QueryResult;
import org.alfresco.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Node;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSet;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.ContentUtils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.log4j.Logger;

import com.ukpn.cdlffe.jxl.ExcelDealer;
import com.ukpn.cdlffe.util.Cons;
import com.ukpn.cdlffe.util.XPathUtils;

public class DirectoryProcessorImpl implements DirectoryProcessor{
	
	private static Logger logger = Logger.getLogger(DirectoryProcessorImpl.class);
	
	public DirectoryProcessorImpl(){
		
	}

	@Override
	public void createCDLStructure() throws Exception {
		// get the target structure from the ExcelDealer
		List<String> targetStruture = ExcelDealer.getInstance().getCdlDirectories();
		
		// use PublishedContentReaderWriter to create the target structure
		PublishedContentReaderWriter pcrw = new PublishedContentReaderWriter(); 
		//1. create CDL folder
		// Start the session
        AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
        Reference companyHome = new Reference(Cons.STORE, null,
				Cons.COMPANY_HOME_PATH);       
        
    	if (!isFolderPresent(WebServiceFactory.getRepositoryService(), companyHome, Cons.CDL_DESTINATION_ROOT_FOLDER_NAME)) {
    		logger.debug("CDL-FFE target folder structure created before any content sync happens.");
    		
			//create folder	
			Reference folder = new Reference(Cons.STORE, null,
					Cons.CDL_DESTINATION_FOLDER_ALF_PATH);
			Reference cdlFolder = pcrw.createFolder(null, folder, Cons.CDL_DESTINATION_ROOT_FOLDER_NAME);
			//2. Loop through target structure folders and create them under CDL folder
			for (String targetFolder : targetStruture) {
				logger.debug(targetFolder);
				String[] folders = targetFolder.split("\\\\");
				//don't consider first element
				Reference subFolder = null;
				Reference createdSubFolder = null;
				for (int i = 1; i < folders.length; i++) {
					if (!folders[i].isEmpty()) {
						if (i == 1) {
							subFolder = new Reference(Cons.STORE, null,
									cdlFolder.getPath() + "/cm:" + folders[i]);
							createdSubFolder = pcrw.createFolder(
									cdlFolder.getPath(), subFolder, folders[i]);
						} else {
							subFolder = new Reference(Cons.STORE, null,
									createdSubFolder.getPath() + "/cm:"
											+ folders[i]);
							createdSubFolder = pcrw.createFolder(
									createdSubFolder.getPath(), subFolder,
									folders[i]);
						}
					}
				}
			}
		} else {
			logger.debug("CDL-FFE target folder structure in place already.");
		}
		AuthenticationUtils.endSession();
	}
	
	@Override
	public void captureSourceTargetAndProcessContentSync(String sourceFolderPath, String destinationFolderPath) throws Exception{
		
		//AuthenticationResult result =  WebServiceFactory.getAuthenticationService().startSession(Cons.USERNAME, Cons.PASSWORD);
		//String ticket = result.getTicket();
		// Start the session
                //AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
                //make folder references
                Reference sourceFolder = new Reference(Cons.STORE, null, sourceFolderPath);
		Reference destinationFolder = new Reference(Cons.STORE, null, destinationFolderPath);
		//delegate the content sync work
		copyContentFromSourceToTarget(sourceFolder,destinationFolder);
		//AuthenticationUtils.endSession()Session()();
		//WebServiceFactory.getAuthenticationService().endSession(ticket);
	}

	
	private void copyContentFromSourceToTarget(Reference sourceFolder, Reference destinationFolder) throws Exception {

		//find the files under source folder
		RepositoryServiceSoapBindingStub repositoryService = WebServiceFactory.getRepositoryService();  
		// Get the content service
                ContentServiceSoapBindingStub contentService = WebServiceFactory.getContentService();        

                // Get children of the source folder
                List<Reference> sourceChildren = getChildren(repositoryService, sourceFolder);

                PublishedContentReaderWriter pcrw = new PublishedContentReaderWriter();

                        //copy the files to the destination folder
                for(Reference ref : sourceChildren){

                        Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{ref}, Cons.STORE, null));
                        logger.debug(nodes[0].getType());
                        String nodeName = getNameOfReference(nodes[0]);
                        logger.debug(nodeName);  
                        // Read the node content from the source folder
                    Content[] readResult = contentService.read(
                                                        new Predicate(new Reference[]{ref}, Cons.STORE, null), 
                                                        Constants.PROP_CONTENT);
                    Content content = readResult[0];                   
                        if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
                        	Reference destDocRef = null;
                        	boolean pdfNeeded = isPdfForPublishedContentNeeded(ref);
							String fileName = pdfNeeded ? XPathUtils
									.changeFileNameExtensionToPDF(nodeName)
									: nodeName;
							boolean nodePresent = isNodePresent(repositoryService,
									destinationFolder, fileName);
                                if (isPublished(ref)) { //published case
									//check this document available in destination
									
									if (nodePresent) { //update content
										 destDocRef = getNodeReferenceByNodeName(
												repositoryService,
												destinationFolder, fileName);
										pcrw.updateContent(
												contentService,
												destDocRef,
												ContentUtils
														.getContentAsString(content));
									} else { //create new document and copy content
										Reference destRef = pcrw
												.createNewContent(
														destinationFolder
																.getPath(),
														fileName);
										if (pdfNeeded)
											pcrw.createNewContentAsPDF(ref,
													destRef, fileName);
										else
											pcrw.writeContentToRepo(
													contentService,
													destRef,
													ContentUtils.getContentAsString(content),
													fileName);
										//pcrw.copyNativeDocToDestination(pcrw.getParentReference(destinationFolder.getPath(), fileName), ref);
									}
								} else if(isRetired(ref))  { //retired case
									
									if(nodePresent){ //delete the document or rendition in CDL -FFE - target folder 
										logger.debug("Document "+ fileName + " under "+ sourceFolder + "is detected as retired. Deletion will starting now.");
										destDocRef = getNodeReferenceByNodeName(
												repositoryService,
												destinationFolder, fileName);
										pcrw.deleteDocument(destDocRef,fileName);
										logger.debug("Document "+ fileName + " under "+ sourceFolder + "is deleted now.");
									}
								}
                        } else if(nodes[0].getType().equals(Constants.TYPE_FOLDER)) { //folder
                                //check if folder exists in destination and create folder to get reference       		
                                Reference folder = new Reference(Cons.STORE, null, destinationFolder.getPath()+"/cm:"+nodeName);
                                Reference createdFolder = pcrw.createFolder(destinationFolder.getPath(),folder, nodeName);
                                //copy nodes under source folder sub folder to destination
                                if(hasChildren(repositoryService, ref))
                                        copyContentFromSourceToTarget(ref, createdFolder);
                        }
                }
		
	}
	
	private String getNameOfReference(Node node) {
		String name = null;
		for(NamedValue value : node.getProperties()){
    		if(value.getName().endsWith(Constants.PROP_NAME)){
    			name = value.getValue();    			
    		}
    	}
		return name;
	}
	
	private boolean hasChildren(RepositoryServiceSoapBindingStub repositoryService,Reference reference) {
		try {
			QueryResult queryResult = repositoryService.queryChildren(
					reference);
			ResultSet resultSet = queryResult.getResultSet();
			ResultSetRow[] rows = resultSet.getRows();
			return (rows != null) && (rows.length > 0);
		} catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
		
	private List<Reference> getChildren(RepositoryServiceSoapBindingStub repositoryService,Reference ref) throws Exception{
		List<Reference> children = new ArrayList<Reference>();
        QueryResult result = repositoryService.queryChildren(ref);
        ResultSetRow[] rows = result.getResultSet() != null ? result.getResultSet().getRows() : null;
        if(rows != null){
        	for(ResultSetRow row : rows){
        		Reference childRef = new Reference(Cons.STORE,row.getNode().getId(),null);
        		children.add(childRef);
        	}       		
        }
        return children;
	}
	
	private boolean isNodePresent(RepositoryServiceSoapBindingStub repositoryService,Reference ref,String nodeName) throws Exception{
		boolean exists = false;
		List<Reference> children = getChildren(repositoryService, ref);
		for(Reference child : children){
			Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{child}, Cons.STORE, null));
       	    	
        	if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
        		String name = getNameOfReference(nodes[0]);
        		if(name.equals(nodeName)){
        			exists = true;
        			break;
        		}       		
        	}
		}
		return exists;
	}
	
	private boolean isPdfForPublishedContentNeeded(Reference ref) throws Exception{
		boolean yes = true;		
		Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{ref}, Cons.STORE, null));    	    	
       	if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
       		for(NamedValue prop : nodes[0].getProperties()){
        		if(prop.getName().equals(Cons.PROP_PUBLISH_NATIVE_ONLY)){
        			String value = prop.getValue();
        			if(value.equals("true"))
        				yes = false;
        		}
        	}      		
       	}
		return yes;
	}
	
	
	private boolean isPublished(Reference ref) throws Exception{
		boolean yes = false;		
		Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{ref}, Cons.STORE, null));    	    	
       	if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
       		for(NamedValue prop : nodes[0].getProperties()){
        		if(prop.getName().equals(Cons.PROP_LIFECYCLE_STATE)){
        			String value = prop.getValue();
        			if(value.trim().equalsIgnoreCase("Published"))
        				yes = true;
        		}
        	}      		
       	}
		return yes;
	}
	
	
	private boolean isRetired(Reference ref) throws Exception{
		boolean yes = false;		
		Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{ref}, Cons.STORE, null));    	    	
       	if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
       		for(NamedValue prop : nodes[0].getProperties()){
        		if(prop.getName().equals(Cons.PROP_LIFECYCLE_STATE)){
        			String value = prop.getValue();
        			if(value.trim().equalsIgnoreCase("Retired"))
        				yes = true;
        		}
        	}      		
       	}
		return yes;
	}
	
	private boolean isFolderPresent(RepositoryServiceSoapBindingStub repositoryService,Reference ref,String folderName) throws Exception{
		boolean exists = false;
		List<Reference> children = getChildren(repositoryService, ref);
		for(Reference child : children){
			Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{child}, Cons.STORE, null));
       	    	
        	if(nodes[0].getType().equals(Constants.TYPE_FOLDER)) { //content
        		String name = getNameOfReference(nodes[0]);
        		if(name.equals(folderName)){
        			exists = true;
        			break;
        		}       		
        	}
		}
		return exists;
	}
	
	private Reference getNodeReferenceByNodeName(RepositoryServiceSoapBindingStub repositoryService,Reference ref,String nodeName) throws Exception{
		Reference docRef = null;
		List<Reference> children = getChildren(repositoryService, ref);
		for(Reference child : children){
			Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{child}, Cons.STORE, null));
       	    	
        	if(nodes[0].getType().equals(Constants.PROP_CONTENT)) { //content
        		String name = getNameOfReference(nodes[0]);
        		if(nodeName.equals(name)){
        			docRef = child;
        			break;
        		}       		
        	}
		}
		return docRef;
	}

	/**
	 * private method used to create source structure for testing purposes.
	 * 
	 * @throws Exception
	 */
	private void createSourceStructure() throws Exception {
		// get the target structure from the ExcelDealer
		List<String> targetStruture = ExcelDealer.getInstance().getSourceDir();
		
		// use PublishedContentReaderWriter to create the target structure
		PublishedContentReaderWriter pcrw = new PublishedContentReaderWriter(); 
		//1. create CDL folder
		// Start the session
        AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
        
    	//create folder	
    	Reference folder = new Reference(Cons.STORE, null, Cons.COMPANY_HOME_PATH+"/cm:UKPN_Docs1");
    	Reference cdlFolder = pcrw.createFolder(null,folder, "UKPN_Docs1");
    	
    	//2. Loop through target structure folders and create them under CDL folder
    	for(String targetFolder : targetStruture){
    		logger.debug(targetFolder);
    		String[] folders = targetFolder.split("/");
    		//don't consider first element
    		Reference subFolder = null;
			Reference createdSubFolder = null;			
    		for(int i = 0; i < folders.length; i++){
    			if(!folders[i].isEmpty()){
	    			if(i == 1){
	    				subFolder = new Reference(Cons.STORE, null, cdlFolder.getPath()+"/cm:" + folders[i]);
	    				createdSubFolder = pcrw.createFolder(cdlFolder.getPath(), subFolder, folders[i]);
	    			} else {
	    				subFolder = new Reference(Cons.STORE, null, createdSubFolder.getPath()+"/cm:" + folders[i]);
	    				createdSubFolder = pcrw.createFolder(createdSubFolder.getPath(), subFolder, folders[i]);
	    			}
    			}
    		}
    	}
	}

	
	public static void main(String []args) throws Exception{
		DirectoryProcessorImpl obj = new DirectoryProcessorImpl();
		
		AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
        String source = "/FFE Cabinet/LIVE - FFE Device Documents/EDF1manuals";
        //String source = "/FFE Cabinet/LIVE - FFE Device Documents/EDF1SafetyDocs";
        List<String> list =  ExcelDealer.getInstance().getCdlFfeDirectoryMap().get(source);
        
        String sourcePath = XPathUtils.generateSourceFolderXPath(source);
        logger.debug(sourcePath);
        
        String destinationPath = null;
        for(String value : list){
        	logger.debug(value);
        	destinationPath = XPathUtils.generateDestinationFolderXPath(value);        	
            logger.debug(destinationPath);
            obj.captureSourceTargetAndProcessContentSync(sourcePath, destinationPath); 
        }
        
        AuthenticationUtils.endSession();
        
		
		//obj.createCDLStructure();
	}

}

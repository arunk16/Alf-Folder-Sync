package com.ukpn.cdlffe.core;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.webservice.content.Content;
import org.alfresco.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.webservice.repository.UpdateResult;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLWriteContent;
import org.alfresco.webservice.types.ContentFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.log4j.Logger;

import com.ukpn.cdlffe.util.Cons;

/**
 * Utility class mainly deals with alfresco to read published content from Alfresco spaces and create them in 
 * CDL FFE spaces.
 * 
 * @author Arun
 *
 */
public class PublishedContentReaderWriter {
	
	private static Logger logger = Logger.getLogger(PublishedContentReaderWriter.class);
	
	/**
     * Method to create new node and its content under a given folder space.
     *  
     * @param parentSpace   
     * @param destFileName
     * @return                  a reference to the created content node
     * @throws Exception        
     */
    protected Reference createNewContent(String parentSpace, String destFileName) 
        throws Exception
    {
        // Update name
        //name = System.currentTimeMillis() + "_" + name;
    	//space = "/app:company_home/cm:sample_folder";
    	
    	if(parentSpace == null ||  parentSpace.isEmpty())
    		throw new IllegalArgumentException("space cannot be null");
    	
    	if(destFileName == null || destFileName.isEmpty())
    		throw new IllegalArgumentException("Name cannot be null");
    	
    	// Get the content service
        //ContentServiceSoapBindingStub contentService = WebServiceFactory.getContentService();        
        
        
        // Create a parent reference, this contains information about the association we are createing to the new content and the
        // parent of the new content (the space retrived from the search)
        ParentReference parentReference = new ParentReference(Cons.STORE, null, parentSpace, Cons.ASSOC_CONTAINS, 
                "{" + Constants.NAMESPACE_CONTENT_MODEL + "}" + destFileName);
              
//        NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, destFileName),
//        		Utils.createNamedValue(Constants.PROP_CONTENT,"" )};
        
        NamedValue[] properties = new NamedValue[] {
        		new NamedValue(Constants.PROP_NAME.toString(), false, destFileName, null),
                new NamedValue(Constants.PROP_CONTENT.toString(), false, new ContentData(null, getMimeTypeByFileName(destFileName), 0L, "UTF-8").toString(), null)
        };
        CMLCreate create = new CMLCreate("1", parentReference, null, null, null, Constants.TYPE_CONTENT, properties);
        CML cml = new CML();
        cml.setCreate(new CMLCreate[]{create});
        UpdateResult[] result = WebServiceFactory.getRepositoryService().update(cml);     
        
        Reference newContentNode = result[0].getDestination();
        return newContentNode;
    }
    
    protected Reference writeContentToRepo(ContentServiceSoapBindingStub contentService,Reference ref,String contentString,String fileName) throws Exception{    	
        Content content = contentService.write(ref, Constants.PROP_CONTENT, contentString.getBytes("UTF-8"), getContentFormatByFileName(fileName));
        return content.getNode();
//        CMLWriteContent write = new CMLWriteContent();
//        write.setWhere(new Predicate(new Reference[]{ref}, Cons.STORE, null));
//        write.setProperty(Constants.PROP_CONTENT.toString());
////        ContentFormat format = new ContentFormat(MimetypeMap.MIMETYPE_TEXT_PLAIN, "UTF-8");
//        write.setFormat(getContentFormatByFileName(fileName));
//        write.setContent(contentString.getBytes());
//        
//        CML cml = new CML();
//        cml.setWriteContent(new CMLWriteContent[]{write});
//
//        UpdateResult[] result = WebServiceFactory.getRepositoryService().update(cml); 
//        Reference reference = result[0].getDestination();
        // Get a reference to the newly created content
//        return reference;	
    }
    
    protected ParentReference getParentReference(String parentSpace, String destFileName) throws Exception{
    	ParentReference parentReference = new ParentReference(Cons.STORE, null, parentSpace, Cons.ASSOC_CONTAINS, 
                "{" + Constants.NAMESPACE_CONTENT_MODEL + "}" + destFileName);
    	return parentReference;
    }
    
    protected void copyNativeDocToDestination(ParentReference parent,Reference ref) throws Exception{
    	CMLCopy copy = new CMLCopy();
        copy.setTo(parent);
        copy.setWhere(new Predicate(new Reference[]{ref}, Cons.STORE, null));
        
        CML cml = new CML();
        cml.setCopy(new CMLCopy[]{copy});
        
        WebServiceFactory.getRepositoryService().update(cml); 
    }
    /**
     * Function to delete and clear document content from an alfresco folder.
     * 
     * @param ref
     * @throws Exception
     */
    protected void deleteDocument(Reference ref, String fileName) throws Exception{
    	logger.debug("Deleting Document : " + fileName);
    	
    	CMLDelete delete = new CMLDelete();
        delete.setWhere(new Predicate(new Reference[]{ref}, Cons.STORE, null));
        
        CML cml = new CML();
        cml.setDelete(new CMLDelete[]{delete});
        
        WebServiceFactory.getRepositoryService().update(cml); 
    }
    
    /**
     * Method to update content of the node under given folder space.
     *  
     * @param contentReference    the content node reference
     * @param updatedContentString    the updated content itself
     * @return                  a reference to the updated content node
     * @throws Exception        
     */
    protected Reference updateContent(ContentServiceSoapBindingStub contentService,Reference contentReference, String updatedContentString) 
        throws Exception
    {
        // Update content
        
    	// Get the content service
        //ContentServiceSoapBindingStub contentService = WebServiceFactory.getContentService();        
        
        
        // Update the content with something new
        contentService.write(contentReference, Constants.PROP_CONTENT, updatedContentString.getBytes(), null);
        
        // Now output the updated content
        Content[] readResult = contentService.read(
                                            new Predicate(new Reference[]{contentReference}, Cons.STORE, null), 
                                            Constants.PROP_CONTENT);
        
        // Get a reference to the updated content
        return readResult[0].getNode();
    }
    
    /**
     * Helper method creates folder in given space or find the reference of the folder if it exists.
     * 
     * @param folder
     * @param folderName
     * @return
     * @throws Exception
     */
    
    protected Reference createFolder(String parenFolderPath,Reference folder,String folderName) throws Exception{
    	
    	Reference ref = null;
    	try
        {
            // Check to see if the folder has already been created or not
            org.alfresco.webservice.types.Node[] nodes = WebServiceFactory.getRepositoryService().get(new Predicate(new Reference[]{folder}, Cons.STORE, null));
            if(nodes != null && nodes.length > 0){
            	org.alfresco.webservice.types.Node node = nodes[0];
            	ref = node.getReference();
            }
        }
        catch (Exception exception)
        {
            // Create parent reference to company home
            ParentReference parentReference = new ParentReference(
            		Cons.STORE,
                    null, 
                    parenFolderPath != null ? parenFolderPath : Cons.COMPANY_HOME_PATH,
                    Constants.ASSOC_CONTAINS, 
                    Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, folderName));

            // Create folder
            NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, folderName)};
            CMLCreate create = new CMLCreate("1", parentReference, null, null, null, Constants.TYPE_FOLDER, properties);
            CML cml = new CML();
            cml.setCreate(new CMLCreate[]{create});
            UpdateResult[] results = WebServiceFactory.getRepositoryService().update(cml);                
            
            // Create parent reference to sample folder
            Reference sampleFolder = results[0].getDestination();
            ref = sampleFolder;
            
        }
    	return ref;
    }
    
    private ContentFormat getContentFormatByFileName(String name){
    	// Define the content format for the content we are adding
        ContentFormat contentFormat = null;
        if(name.endsWith(Cons.EXTN_DOC) || name.endsWith(Cons.EXTN_DOCX))
        	contentFormat =	new ContentFormat(MimetypeMap.MIMETYPE_WORD, "UTF-8");
        else if(name.endsWith(Cons.EXTN_XLS) || name.endsWith(Cons.EXTN_XLSX))
        	contentFormat =	new ContentFormat(MimetypeMap.MIMETYPE_EXCEL, "UTF-8");
        else if(name.endsWith(Cons.EXTN_PDF))
        	contentFormat =	new ContentFormat(MimetypeMap.MIMETYPE_PDF, "UTF-8");
        
        return contentFormat;
    }
    
    private String getMimeTypeByFileName(String name) {
    	// Define the content format for the content we are adding
        String contentFormat = null;
        if(name.endsWith(Cons.EXTN_DOC) || name.endsWith(Cons.EXTN_DOCX))
        	contentFormat =	MimetypeMap.MIMETYPE_WORD;
        else if(name.endsWith(Cons.EXTN_XLS) || name.endsWith(Cons.EXTN_XLSX))
        	contentFormat =	MimetypeMap.MIMETYPE_EXCEL;
        else if(name.endsWith(Cons.EXTN_PDF))
        	contentFormat =	MimetypeMap.MIMETYPE_PDF;
        
        return contentFormat;
    }
    
    protected void createNewContentAsPDF(Reference source, Reference dest, String destFileName) throws Exception{
    	WebServiceFactory.getContentService().transform(source, Constants.PROP_CONTENT, dest, 
    			Constants.PROP_CONTENT, getContentFormatByFileName(destFileName));
    }
    
    public static void main(String []args) throws Exception{
    	PublishedContentReaderWriter obj = new PublishedContentReaderWriter();
    	
    	// Start the session
        AuthenticationUtils.startSession(Cons.USERNAME, Cons.PASSWORD);
        
    	
    	
    	//create folder
    	Reference folder = new Reference(Cons.STORE, null, Cons.COMPANY_HOME_PATH+"/cm:arun_folder");
    	Reference createdFolder = obj.createFolder(null,folder, "arun_test");
    	
    	Reference folder1 = new Reference(Cons.STORE, null, createdFolder.getPath()+"/cm:arun_sub1");
    	Reference createdFolder1 = obj.createFolder(createdFolder.getPath(),folder1, "arun_sub1");
    	
    	
    	logger.debug(createdFolder.getUuid());
    	logger.debug(createdFolder.getPath());
    	//create content
    	//obj.createNewContent(createdFolder1.getPath(), "test-Document.txt", "My first content");
    	
    	
    }


}

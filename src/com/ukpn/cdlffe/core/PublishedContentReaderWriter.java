package com.ukpn.cdlffe.core;

import org.alfresco.webservice.content.Content;
import org.alfresco.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.webservice.repository.UpdateResult;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLCreate;
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
     * @param contentService    the content web service
     * @param content           the content itself
     * @return                  a reference to the created content node
     * @throws Exception        
     */
    protected Reference createNewContent(ContentServiceSoapBindingStub contentService, String space, String name, String contentString) 
        throws Exception
    {
        // Update name
        //name = System.currentTimeMillis() + "_" + name;
    	//space = "/app:company_home/cm:sample_folder";
    	
    	if(space == null ||  space.isEmpty())
    		throw new IllegalArgumentException("space cannot be null");
    	
    	if(name == null || name.isEmpty())
    		throw new IllegalArgumentException("Name cannot be null");
    	
    	// Get the content service
        //ContentServiceSoapBindingStub contentService = WebServiceFactory.getContentService();        
        
        
        // Create a parent reference, this contains information about the association we are createing to the new content and the
        // parent of the new content (the space retrived from the search)
        ParentReference parentReference = new ParentReference(Cons.STORE, null, space, Cons.ASSOC_CONTAINS, 
                "{" + Constants.NAMESPACE_CONTENT_MODEL + "}" + name);
        
        // Define the content format for the content we are adding
        ContentFormat contentFormat = null;
        if(name.endsWith(Cons.EXTN_DOC) || name.endsWith(Cons.EXTN_DOCX))
        	contentFormat =	new ContentFormat(Cons.CONTENT_FORMAT_DOC, "UTF-8");
        else if(name.endsWith(Cons.EXTN_XLS) || name.endsWith(Cons.EXTN_XLSX))
        	contentFormat =	new ContentFormat(Cons.CONTENT_FORMAT_XLS, "UTF-8");
        else if(name.endsWith(Cons.EXTN_PDF))
        	contentFormat =	new ContentFormat(Cons.CONTENT_FORMAT_PDF, "UTF-8");
        
        NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, name)};
        CMLCreate create = new CMLCreate("1", parentReference, null, null, null, Constants.TYPE_CONTENT, properties);
        CML cml = new CML();
        cml.setCreate(new CMLCreate[]{create});
        UpdateResult[] result = WebServiceFactory.getRepositoryService().update(cml);     
        
        Reference newContentNode = result[0].getDestination();
        Content content = contentService.write(newContentNode, Constants.PROP_CONTENT, contentString.getBytes(), contentFormat);
        
        // Get a reference to the newly created content
        return content.getNode();
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
    
    protected void createNewContentAsPDF() throws Exception{
    	//WebServiceFactory.getContentService().transform(source, property, destinationReference, destinationProperty, destinationFormat)
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

package com.ukpn.cdlffe.util;

import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;

public class Cons {
	
	/** Admin user name and password used to connect to the repository */
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    
    /** The store used throughout */
    public static final Store STORE = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
    
	
	/** The type of the association we are creating to the new content */
    public static final String ASSOC_CONTAINS = "{http://www.alfresco.org/model/content/1.0}contains";
    
    public static final String COMPANY_HOME_PATH = "/app:company_home";
    
    public static final String CDL_DESTINATION_ROOT_FOLDER_NAME = "CDL";
    
    public static final String CDL_SOURCE_ROOT_FOLDER_NAME = "UKPN_Docs";
    
    public static final String XPATH_SPACE_REPLACEMENT_LITERAL = "_x0020_";
    
    public static final String CDL_SOURCE_FOLDER_ALF_PATH = COMPANY_HOME_PATH+"/cm:"+CDL_SOURCE_ROOT_FOLDER_NAME;
    
    public static final String CDL_DESTINATION_FOLDER_ALF_PATH = COMPANY_HOME_PATH+"/cm:"+CDL_DESTINATION_ROOT_FOLDER_NAME;
    
    public static final String PROP_PUBLISH_NATIVE_ONLY ="{ukpn.ukpnDoc.model}publishNativeFormatOnly";
    
    
}

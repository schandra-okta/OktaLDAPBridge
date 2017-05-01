/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.*;


/**
 *
 * @author sundarganesan
 */
public final class LDAPUtil {
    //List of attributes to searched and returned in the JSON object
    static String[] attributeFilter = { "uid", "mail", "sn", "givenName", "JDMember", "jdUserClass", "jdAccountState", "jdConsentTime", };
    
    private static final LDAPUtil onlyInstance = new LDAPUtil();
    
    public static LDAPUtil getInstance () {
        return onlyInstance;
        
    }
    
    private LDAPUtil() {
        Properties prop = new Properties();
	InputStream input = null;

	try {
                input = LDAPUtil.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");
		//input = new FileInputStream("OktaLDAPBridgeConfig.properties");

		// load a properties file
		prop.load(input);

		// get the property value and print it out
		System.out.println(prop.getProperty("database"));
		System.out.println(prop.getProperty("dbuser"));
		System.out.println(prop.getProperty("dbpassword"));

	} catch (IOException ex) {
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

        
    }
    
    

  public static String queryLDAP(String filter) throws Exception {
    Properties env = new Properties();

    String sp = "com.sun.jndi.ldap.LdapCtxFactory";
    env.put(Context.INITIAL_CONTEXT_FACTORY, sp);

    String ldapUrl = "ldap://localhost:1389/dc=example,dc=com";
    env.put(Context.PROVIDER_URL, ldapUrl);

    DirContext dctx = new InitialDirContext(env);


    String base = "ou=People";

    SearchControls sc = new SearchControls();
    
    sc.setReturningAttributes(attributeFilter);
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

    //filter = "(&(sn=Atp)(l=New Haven))";

    NamingEnumeration results = dctx.search(base, filter, sc);
    int i = 0;
    String retStr = "";
    SearchResult sr = (SearchResult) results.next();
    i++;
    while(results.hasMore()){
         i++;
    }
    dctx.close();
    if(i==1) {
        retStr = convertToJson(sr);
    } else {
        throw new javax.naming.NamingException("Check username");
    }
    return retStr;
  }
  
  public static String convertToJson(SearchResult sr) {
      JSONObject retObj = new JSONObject();
      Attributes attrs = sr.getAttributes();
      int i = 0;
      while (i<attributeFilter.length) {
            String name = attributeFilter[i];
            String value = "";
            Attribute attr = attrs.get(name);
            try {
                value = "";
                if (name!="JDMember") {
                    // No special handling for single valued attributes
                    value = attr.get().toString();
                } else {
                    // Special handling is required for multi-valued attributes like JDMember
                    Enumeration valueEnum = attr.getAll();
                    while(valueEnum.hasMoreElements()) {
                        if (value != "") {
                            // Comma is used to separate multiple values
                            value = value + ", " + valueEnum.nextElement();
                        } else {
                            // First element gets added directly
                            value = (String)valueEnum.nextElement();
                        }
                    }
                    
                }
            } catch (NamingException ex) {
                Logger.getLogger(LDAPUtil.class.getName()).log(Level.SEVERE, null, ex + " Unable to retrieve attribute to build JSON");
                
            }
            System.out.println("debug 444 " + value);
            //System.out.println("debug 555 " + value.substring(value.in));
            retObj.put(name, value);
            i++;
      }
      
      System.out.println(retObj.toString());
      return retObj.toString();
  }
}

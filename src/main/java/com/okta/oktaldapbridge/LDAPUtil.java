/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author sundarganesan
 */
public final class LDAPUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPUtil.class);

    //List of attributes to searched and returned in the JSON object
    static String[] attributeFilter = null;
    static String dbuser = "";
    static String dbpassword = "";
    static String connectionString = "";
    static String searchbase = "";
    static String attributes = "";
    
    private static final LDAPUtil onlyInstance = new LDAPUtil();
    
    public static LDAPUtil getInstance () {
        return onlyInstance;
        
    }
    
    private LDAPUtil() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = LDAPUtil.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");
            EncryptionUtil encUtilInstance = EncryptionUtil.getInstance();

            // load properties file
            prop.load(input);

            dbuser = encUtilInstance.decryptAES(prop.getProperty("dbuser"));
            dbpassword = encUtilInstance.decryptAES(prop.getProperty("dbpassword"));
            connectionString = prop.getProperty("connectionString");
            searchbase = prop.getProperty("searchbase");
            attributes = prop.getProperty("attributes");

            attributeFilter = attributes.split(",");

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
    env.put(Context.PROVIDER_URL, connectionString);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, dbuser);
    env.put(Context.SECURITY_CREDENTIALS, dbpassword);

    DirContext dctx = new InitialDirContext(env);


    String base = searchbase;

    SearchControls sc = new SearchControls();
    
    sc.setReturningAttributes(attributeFilter);
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

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

  public static boolean updateLDAP(String filter, Map<String, Object> attrValmap) throws Exception {
    Properties env = new Properties();

    String sp = "com.sun.jndi.ldap.LdapCtxFactory";
    env.put(Context.INITIAL_CONTEXT_FACTORY, sp);
    env.put(Context.PROVIDER_URL, connectionString);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, dbuser);
    env.put(Context.SECURITY_CREDENTIALS, dbpassword);

    DirContext dctx = new InitialDirContext(env);

    String base = searchbase;

    SearchControls sc = new SearchControls();
    
    sc.setReturningAttributes(new String[]{"dn"});//Don't need the rest
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

    NamingEnumeration results = dctx.search(base, filter, sc);
    int i = 0;
    SearchResult sr = (SearchResult) results.next();
    i++;
    while(results.hasMore()){
         i++;
    }
    dctx.close();
    if(i==1) {
        
        String dn = sr.getAttributes().get("dn").get().toString();
        ModificationItem[] mods = new ModificationItem[attrValmap.keySet().size()];
        int count = 0;
        Iterator it = attrValmap.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next().toString();
            String val = attrValmap.get(key).toString();
            Attribute mod = new BasicAttribute(key, val);
            mods[count++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod);
        }

        dctx.modifyAttributes(dn, mods);

    } else {
        throw new javax.naming.NamingException("Check username");
    }
    return true;
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
                LOGGER.error(ex + " Unable to retrieve attribute to build JSON");
                
            }
            LOGGER.debug("debug 444 " + value);
            //LOGGER.debug("debug 555 " + value.substring(value.in));
            retObj.put(name, value);
            i++;
      }
      
      LOGGER.debug(retObj.toString());
      return retObj.toString();
  }
}

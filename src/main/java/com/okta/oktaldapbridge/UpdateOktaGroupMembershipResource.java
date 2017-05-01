/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.json.JSONArray;

/**
 * REST Web Service
 *
 * @author sundarganesan
 */
@Path("UpdateOktaGroupMembership")
public class UpdateOktaGroupMembershipResource {
    
    String oktaSearchUserUrl = new String();
    String oktaGetGroupMembershipUrl = new String();
    String oktaSearchGroupUrl = new String();
    String oktaCreateGroupUrl = new String();
    String oktaAddDeleteUserToGroupUrl = new String();
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of UpdateOktaGroupMembershipResource
     */
    public UpdateOktaGroupMembershipResource() {
         Properties prop = new Properties();
	InputStream input = null;

	try {
                input = LDAPUtil.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");
		//input = new FileInputStream("OktaLDAPBridgeConfig.properties");

		// load a properties file
		prop.load(input);
                
                //getOktaUserAPI = prop.getProperty("oktaUserApi");
                //oktaApiKey = prop.getProperty("oktaApiKey");
		// get the property value and print it out
                oktaSearchUserUrl = prop.getProperty("oktaSearchUserUrl");
		System.out.println(prop.getProperty("oktaSearchUserUrl"));
                oktaSearchGroupUrl = prop.getProperty("oktaSearchGroupUrl");
		System.out.println(prop.getProperty("oktaSearchGroupUrl"));
                 oktaCreateGroupUrl = prop.getProperty("oktaCreateGroupUrl");
		System.out.println(prop.getProperty("oktaCreateGroupUrl"));
                 oktaAddDeleteUserToGroupUrl = prop.getProperty("oktaAddDeleteUserToGroupUrl");
		System.out.println(prop.getProperty("oktaAddDeleteUserToGroupUrl"));
                oktaGetGroupMembershipUrl = prop.getProperty("oktaGetGroupMembershipUrl");
		System.out.println(prop.getProperty("oktaGetGroupMembershipUrl"));
		//System.out.println(prop.getProperty("dbpassword"));

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


    /**
     * PUT method for updating or creating an instance of UpdateOktaGroupMembershipResource
     * @param content representation for the resource
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJson(String content) {
        
        //Read username from content 
        String username = new JSONObject(content).getString("userName");
        
        String ldapObjStr = new String();
        String oktaObjStr = new String();
        String oktaGroupMemStr = new String();
        RetObj ret = new RetObj();

        try {
            //Query LDAP for username and get JDMember attributes of the entry
            ldapObjStr = LDAPUtil.queryLDAP("(uid="+username + ")");
            System.out.println("debug 0000000 ldapObjStr " + ldapObjStr);
        } catch (Exception ex) {
            Logger.getLogger(UpdateOktaGroupMembershipResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.NOT_FOUND).entity("User not found in LDAP for userName: " + username).build();
        }  
            
        //Query Okta for User with username in content
        String uUrl = oktaSearchUserUrl;
        
        
        try {
            uUrl = uUrl + URLEncoder.encode(username, "UTF-8");
            System.out.println("debug 1111111 oktaObjStr Url " + uUrl);
            ret = HTTPUtil.get(uUrl);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UpdateOktaGroupMembershipResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("debug 1111111 retArray " + ret.output);
        
        oktaObjStr = RetObj.stripSquareBracs(ret.output);
        
        String uUid = new String();
        //Extract UID from Okta's response
        if(oktaObjStr!= null) {
            JSONObject jObj = new JSONObject(oktaObjStr);
            uUid = jObj.getString("id");
            System.out.println("debug 3313131 uUid " + uUid);
        }
        //Query Okta for user's group membership
        String gUrl = MessageFormat.format(oktaGetGroupMembershipUrl, uUid);
        
        System.out.println("debug 2121221 oktaGroupMemStr URL " + gUrl);
      
            //oktaGroupMemStr = HTTPUtil.httpGet(gUrl + URLEncoder.encode(username, "UTF-8"));
            ret = HTTPUtil.get(gUrl);
       
        System.out.println("debug 2121221 retArray " + ret.output);
        
        //oktaGroupMemStr = RetObj.stripSquareBracs(ret.output);
        oktaGroupMemStr = ret.output;
        
        //Compare user's current group membership with JDMember groups
        JSONObject lObj = new JSONObject(ldapObjStr);
        System.out.println("6767676" + lObj.get("JDMember"));
        String jdMemberListStr = (String)lObj.get("JDMember");
        //ArrayList jdMemberList = new ArrayList();
        List li = Arrays.asList(jdMemberListStr.split(","));
        int ctr = 0;
        while (ctr < li.size()) {
            li.set(ctr, ((String)li.get(ctr)).trim());
            ctr++;
            
        }
                 
        Set<String> jdMemberListSet = new HashSet<>(li);
        // remove whitespaces in the Group names
        
        
        JSONObject uObj = new JSONObject(oktaObjStr);
        
        JSONArray jArr = new JSONArray(oktaGroupMemStr);
        
        System.out.println("7878787    " + jArr.length());
        int i = 0;
        //ArrayList userInGroups = new ArrayList();
        Set<String> userInGroupsSet = new HashSet<>();
        
        Iterator<Object> itr = jArr.iterator();
        while(itr.hasNext()) {
            JSONObject temp = (JSONObject)itr.next();
             System.out.println("7878787 string " + i + " " + temp.toString());
            System.out.println("7878787 id " + temp.get("id"));
            System.out.println("7878787 profile " + temp.get("profile"));
            JSONObject pro = temp.getJSONObject("profile");
            String gname = (String)pro.get("name");
            System.out.println("7878787 gname " + gname);
            String type = (String)temp.get("type");
            System.out.println("7878787 name " + temp.get("type"));
            if (gname.equals("Everyone") || gname.startsWith("okta_")) {
                 System.out.println("898989 Skipping group!!!!!");  

            } else {
                userInGroupsSet.add(gname.trim());
            }
        }
          
        System.out.println("54554545 jdMemberListSet " + jdMemberListSet.toString());
        
        System.out.println("54554545 userInGroupsSet " + userInGroupsSet.toString());
        
        Set<String> s1 = new HashSet<>(jdMemberListSet);
        
        Set<String> s2 = new HashSet<>(userInGroupsSet);
        
        s1.removeAll(userInGroupsSet);
         
        
        s2.removeAll(jdMemberListSet);
        
        System.out.println("81818181 - Groups to be added to user " + s1.toString());
        
        System.out.println("81818181 - Groups to be removed from user " + s2.toString());
        
        Iterator<String> s1itr = s1.iterator();
        
            
        while(s1itr.hasNext()) {
            String g = s1itr.next();
            g=g.trim();
            String gid = getGroupId(g);
            if(gid.contains("NOT FOUND")) {
                System.out.println("Need to create Group"); 
                gUrl = oktaCreateGroupUrl;
                JSONObject jobj = new JSONObject();
                JSONObject jpro = new JSONObject();
                jpro.put("name", g);
                jpro.put("description", g);
                jobj.put("profile", jpro);
                RetObj retAns = HTTPUtil.post(gUrl, jobj.toString());
                if(retAns.responseCode != 200) {
                    //To Do Error Handling
                    System.out.println("Error creating group");
                    continue;
                } else {
                    System.out.println(retAns.output);
                    //String temp = RetObj.stripSquareBracs(retAns.output);
                    int start = retAns.output.indexOf("id");
                    int end = retAns.output.indexOf("created");
                    String temp = retAns.output.substring(start+5, end-3);
                    //JSONArray jArr2 = new JSONArray(temp);
                   
                    //JSONObject j = jArr2.getJSONObject(1);
                    //gid = j.getString("id");
                    
                    gid = temp;
                    
                    String tUrl = MessageFormat.format(oktaAddDeleteUserToGroupUrl, gid, uUid);
                    RetObj reto2 = HTTPUtil.put(tUrl, "");
                    if (reto2.responseCode == 204) {
                        System.out.println("Adding user " + uUid + " to group " + gid + " COMPLETE");
                    } else {
                        System.out.println("Adding user " + uUid + " to group " + gid + " FAILED!!!!!!");
                        return Response.status(Response.Status.BAD_REQUEST).entity("Runtime Exception: Unable to add user to Group").build();    
                    }
                    
                }
                    
            } else if(gid.contains("MULTIPLE")) {
                System.out.println("THROW Error"); 
                // To Do: ERROR HANDLING
                //return "Runtime Exception: MULTIPLE GROUPS FOUND";
                return Response.status(Response.Status.BAD_REQUEST).entity("Runtime Exception: Multiple values returned rom Okta for a JDMember group name").build();
               
               
            } else {
            
                System.out.println("Adding user " + uUid + " to group " + gid);
                String tUrl = MessageFormat.format(oktaAddDeleteUserToGroupUrl, gid, uUid);
                RetObj retAns2 = HTTPUtil.put(tUrl, "");
                if (retAns2.responseCode == 204) {
                    System.out.println("Adding user " + uUid + " to group " + gid + " COMPLETE");
                } else {
                    System.out.println("Adding user " + uUid + " to group " + gid + " FAILED!!!!!!");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Runtime Exception: Unable to add user to Group").build();    
                }
            }
            
        }
        
        
        Iterator<String> s2itr = s2.iterator();
        
            
        while(s2itr.hasNext()) {
            String g = s2itr.next();
            g=g.trim();
            String gid = getGroupId(g);
            if(gid.contains("MULTIPLE")) {
                System.out.println("THROW Error"); 
                // To Do: ERROR HANDLING
                return Response.status(Response.Status.BAD_REQUEST).entity("Runtime Exception: Multiple values returned rom Okta for a JDMember group name").build();
               
               
            } else {
                System.out.println("Removing user " + uUid + " from group " + gid);
                String tUrl = MessageFormat.format(oktaAddDeleteUserToGroupUrl, gid, uUid);
                RetObj reto = HTTPUtil.delete(tUrl, "");
                if (reto.responseCode == 204) {
                    System.out.println("Removing user " + uUid + " to group " + gid + " COMPLETE");
                } else {
                    System.out.println("Removing user " + uUid + " to group " + gid + " FAILED!!!!!!");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Runtime Exception: Unable to delete user from Group").build();
               
                }
                
            }
            
            
        }
        
        
        //JSONObject gObj = new JSONObject(oktaGroupMemStr);
        
        
        //System.out.println("7878787" + gObj.get("name"));
        
        
        //Create group is required

        //Update group membership if required
        
        // Build response object
        JSONObject resp = new JSONObject();
        resp.put("status", "SUCCESS");
        
        return Response.status(Response.Status.OK).entity(resp.toString()).build();
        
        //return jdMemberListSet + "\n$$$$$$$$ " + userInGroupsSet + "\n$$$$$$$$ " + s1 + "\n$$$$$$$" + s2;
    }
    
    private String getGroupId(String g) {
        String gid = new String();
        String gUrl = oktaSearchGroupUrl + g.trim();
        RetObj ret = HTTPUtil.get(gUrl);
        if(ret.responseCode == 200) {
            JSONArray jArr = new JSONArray(ret.output);
            Iterator<Object> itr = jArr.iterator();
            if(jArr.length()==0) {
                System.out.println("Need to create Group"); 
                gid = "NOT FOUND";
                return gid;
            } 
            if(jArr.length() > 1) {
                System.out.println("Multiple Groups returned, check name"); 
                gid = "MULTIPLE GROUPS FOUND WITH SAME NAME";
                return gid;
            } 
            while (itr.hasNext()){
                JSONObject temp = (JSONObject)itr.next();
                gid = (String) temp.get("id");
            }
        } else {
            gid = "ERROR";
        }
        return gid;
        
        
    }
}

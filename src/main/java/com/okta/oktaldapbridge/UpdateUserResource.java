/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author sundarganesan
 */
@Path("UpdateUser")
public class UpdateUserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserResource.class);
    String oktaAPIUrlPrefix = new String();

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of UpdateUserResource
     */
    public UpdateUserResource() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = LDAPUtil.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");

            // load a properties file
            prop.load(input);

            oktaAPIUrlPrefix = prop.getProperty("oktaAPIUrlPrefix");

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
     * Retrieves representation of an instance of com.okta.oktaldapbridge.UpdateUserResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * POST method for updating or creating an instance of UpdateUserResource
     * @param content representation for the resource
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postJson(String content) {
        //Read username from content 
        JSONObject input = new JSONObject(content);
        String userName = input.getString("userName");
        Map<String, Object> attrValmap = input.toMap();
        attrValmap.remove("userName");
        
        //Update Okta
        try
        {
            String userResourceURI = oktaAPIUrlPrefix+"/Users/"+userName;
            JSONObject oktaPartialProfileToUpdate = new JSONObject();
            oktaPartialProfileToUpdate.put("profile", attrValmap);
            HTTPUtil.post(userResourceURI, oktaPartialProfileToUpdate.toString());
        } catch (Exception ex) {
            LOGGER.error(null, ex);
            return Response.status(Response.Status.NOT_FOUND).entity("Entry not updated in Okta for userName: " + userName).build();
        } 
        
        //Update LDAP
        try {
            //Update LDAP entry for user
            boolean updated = LDAPUtil.updateLDAP("(uid="+userName + ")", attrValmap);
            LOGGER.debug("LDAP Update result : " + updated);
        } catch (Exception ex) {
            LOGGER.error(null, ex);
            return Response.status(Response.Status.NOT_FOUND).entity("Entry not updated in LDAP for userName: " + userName).build();
        }  
        
        return Response.status(Response.Status.OK).entity(""/*resp.toString()*/).build();
    }
}

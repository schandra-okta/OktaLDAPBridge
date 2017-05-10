/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.okta.oktaldapbridge;

/**
 *
 * @author schandra
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationRequestFilter.class);
    private static String secretPassphrase = new String();
    private static final EncryptionUtil encUtilInstance = EncryptionUtil.getInstance();

    /**
     * Creates a new instance of UpdateUserResource
     */
    public AuthorizationRequestFilter() {
        super();
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = AuthorizationRequestFilter.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");
            prop.load(input);
            secretPassphrase = prop.getProperty("secretPassphrase");
        } catch (IOException ex) {
            LOGGER.error("Error initializing AuthorizationRequestFilter", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error("Error initializing AuthorizationRequestFilter while closing properties file stream", e);
                }
            }
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext)
                    throws IOException {
        String authzHeader = requestContext.getHeaderString("Authorization");
        
        if(null==authzHeader||!(encUtilInstance.decryptAES(authzHeader).equals(secretPassphrase)))
                requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User cannot access the resource.")
                    .build());
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.lang.*;
import java.net.Proxy;
import java.util.*;

/**
 *
 * @author sundarganesan
 */
public final class HTTPUtil {
        static String getOktaUserAPI = "";
        static String oktaApiKey = "";
        private static final HTTPUtil onlyInstance = new HTTPUtil();
        
        public static HTTPUtil getInstance () {
        return onlyInstance;
        
        }
        public HTTPUtil() {
             Properties prop = new Properties();
	InputStream input = null;

	try {
                input = LDAPUtil.class.getResourceAsStream("/OktaLDAPBridgeConfig.properties");
		//input = new FileInputStream("OktaLDAPBridgeConfig.properties");

		// load a properties file
		prop.load(input);
                
                getOktaUserAPI = prop.getProperty("oktaUserApi");
                oktaApiKey = prop.getProperty("oktaApiKey");
		// get the property value and print it out
		

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
	
        

        public static RetObj get(String resource) {
        RetObj ret = new RetObj();
        try {
            URL url = new URL(resource);
            HttpURLConnection conn;
           
                conn = (HttpURLConnection) url.openConnection();
  
            conn.setConnectTimeout(1000000);
            conn.setReadTimeout(1000000);
            conn.setRequestProperty("Authorization", "SSWS " + oktaApiKey);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");
            String line;
            ret.responseCode = conn.getResponseCode();
            if (ret.responseCode == 200) {
                InputStream s = conn.getInputStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.output = "";
                        while ((line = rd.readLine()) != null) {
                            ret.output += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }
                }
            } else {
                InputStream s = conn.getErrorStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.errorOutput = "";
                        while ((line = rd.readLine()) != null) {
                            ret.errorOutput += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }                   
                }
            }
            String rll = conn.getHeaderField("X-Rate-Limit-Limit");
            if (rll != null && rll.length() > 0) {
                try {
                    ret.rateLimitLimit = Integer.parseInt(rll);
                } catch (Exception e) {
                    ret.rateLimitLimit = 1200;
                }
            }
        } catch (IOException e) {
            ret.exception = e.getMessage();
        }
        return ret;
    }
	
        
        public static RetObj post(String resource, String data) {
        RetObj ret = new RetObj();
        try {
            URL url = new URL(resource);
            HttpURLConnection conn;
     
                conn = (HttpURLConnection) url.openConnection();
       
            conn.setConnectTimeout(1000000);
            conn.setReadTimeout(1000000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "SSWS " + oktaApiKey);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(data.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            String line;
            ret.responseCode = conn.getResponseCode();
            if (ret.responseCode == 201 || ret.responseCode == 200) {
                InputStream s = conn.getInputStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.output = "";
                        while ((line = rd.readLine()) != null) {
                            ret.output += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }
                }
            } else {
                InputStream s = conn.getErrorStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.errorOutput = "";
                        while ((line = rd.readLine()) != null) {
                            ret.errorOutput += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }                   
                }
            }
            String rll = conn.getHeaderField("X-Rate-Limit-Limit");
            if (rll != null && rll.length() > 0) {
                try {
                    ret.rateLimitLimit = Integer.parseInt(rll);
                } catch (Exception e) {
                    ret.rateLimitLimit = 1200;
                }
            }
        } catch (IOException e) {
            ret.exception = e.getMessage();
        }
        return ret;
    }
        
        
        public static RetObj put(String resource, String data) {
        RetObj ret = new RetObj();
        try {
            URL url = new URL(resource);
            HttpURLConnection conn;
     
                conn = (HttpURLConnection) url.openConnection();
       
            conn.setConnectTimeout(1000000);
            conn.setReadTimeout(1000000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "SSWS " + oktaApiKey);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("PUT");
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(data.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            String line;
            ret.responseCode = conn.getResponseCode();
            if (ret.responseCode == 201 || ret.responseCode == 200 || ret.responseCode == 204) {
                InputStream s = conn.getInputStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.output = "";
                        while ((line = rd.readLine()) != null) {
                            ret.output += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }
                }
            } else {
                InputStream s = conn.getErrorStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.errorOutput = "";
                        while ((line = rd.readLine()) != null) {
                            ret.errorOutput += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }                   
                }
            }
            String rll = conn.getHeaderField("X-Rate-Limit-Limit");
            if (rll != null && rll.length() > 0) {
                try {
                    ret.rateLimitLimit = Integer.parseInt(rll);
                } catch (Exception e) {
                    ret.rateLimitLimit = 1200;
                }
            }
        } catch (IOException e) {
            ret.exception = e.getMessage();
        }
        return ret;
    }
        
        public static RetObj delete(String resource, String data) {
        RetObj ret = new RetObj();
        try {
            URL url = new URL(resource);
            HttpURLConnection conn;
     
                conn = (HttpURLConnection) url.openConnection();
       
            conn.setConnectTimeout(1000000);
            conn.setReadTimeout(1000000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "SSWS " + oktaApiKey);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("DELETE");
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(data.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            String line;
            ret.responseCode = conn.getResponseCode();
            if (ret.responseCode == 201 || ret.responseCode == 200 || ret.responseCode == 204) {
                InputStream s = conn.getInputStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.output = "";
                        while ((line = rd.readLine()) != null) {
                            ret.output += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }
                }
            } else {
                InputStream s = conn.getErrorStream();
                if (s != null) {
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(s));
                        ret.errorOutput = "";
                        while ((line = rd.readLine()) != null) {
                            ret.errorOutput += line;
                        }
                    }
                    catch(IOException ioe)
                    {
                        throw ioe;
                    }                   
                }
            }
            String rll = conn.getHeaderField("X-Rate-Limit-Limit");
            if (rll != null && rll.length() > 0) {
                try {
                    ret.rateLimitLimit = Integer.parseInt(rll);
                } catch (Exception e) {
                    ret.rateLimitLimit = 1200;
                }
            }
        } catch (IOException e) {
            ret.exception = e.getMessage();
        }
        return ret;
    }
    
}


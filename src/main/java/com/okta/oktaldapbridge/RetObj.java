/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.okta.oktaldapbridge;

/**
 *
 * @author sundarganesan
 */
public class RetObj {

        public String exception;
        public int responseCode;
        public String errorOutput;
        public String output;
        public int rateLimitLimit;
        public int rateLimitRemaining;
        public int rateLimitReset;
        
        public static String stripSquareBracs(String retoutput) {
            System.out.println("5555 " + retoutput);
            retoutput = retoutput.replaceAll("\\[", "").replaceAll("\\]","");
            System.out.println("6666 " + retoutput);
            return retoutput;
        }
    }
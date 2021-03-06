/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.jandex.tests;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.fat.util.LoggingTest;
import com.ibm.ws.fat.util.SharedServer;
import com.ibm.ws.fat.util.browser.WebResponse;
import com.ibm.ws.jandex.JandexApplicationHelper;

/**
 * Test server.xml configuration where
 * <application ... > // useJandex not specified. Defaults to false
 * <applicationManager ...> // useJandex not specified. Defaults to false.
 */
public class JandexAppDefaultAppMgrDefaultTest extends LoggingTest {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(JandexAppDefaultAppMgrDefaultTest.class.getName());

    protected static final Map<String, String> testUrlMap = new HashMap<String, String>();

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("jandexAppDefaultAppMgrDefault_server");

    /*
     * (non-Javadoc)
     *
     * @see com.ibm.ws.fat.util.LoggingTest#getSharedServer()
     */
    @Override
    protected SharedServer getSharedServer() {
        // TODO Auto-generated method stub
        return SHARED_SERVER;
    }

    @BeforeClass
    public static void setUp() throws Exception {

        LOG.info("Setup : add TestServlet40 to the server if not already present.");

        // WCApplicationHelper.addEarToServerDropins(SHARED_SERVER.getLibertyServer(), "TestServlet40.ear", true,
        //                                           "TestServlet40.war", true, "TestServlet40.jar", true, "testservlet40.war.servlets",
        //                                           "testservlet40.war.listeners", "testservlet40.jar.servlets");

        JandexApplicationHelper.addEarToServerApps(SHARED_SERVER.getLibertyServer(),
                                               "TestServlet40.ear", // earName
                                               true, // addEarResources
                                               "TestServlet40.war", // warName
                                               true, // addWarResources
                                               "TestServlet40.jar", // jarName
                                               true, // addJarResources
                                               "testservlet40.war.servlets", // packageNames
                                               "testservlet40.jar.servlets");

        SHARED_SERVER.startIfNotStarted();

        LOG.info("Setup : wait for message to indicate app has started");

        SHARED_SERVER.getLibertyServer().waitForStringInLog("CWWKZ0001I.* TestServlet40", 10000);

        LOG.info("Setup : app has started, or so we believe");

    }

    @AfterClass
    public static void testCleanup() throws Exception {

        LOG.info("testCleanUp : stop server");

        SHARED_SERVER.getLibertyServer().stopServer("CWWKZ0014W");

    }

    protected String parseResponse(WebResponse wr, String beginText, String endText) {
        String s;
        String body = wr.getResponseBody();
        int beginTextIndex = body.indexOf(beginText);
        if (beginTextIndex < 0)
            return "begin text, " + beginText + ", not found";
        int endTextIndex = body.indexOf(endText, beginTextIndex);
        if (endTextIndex < 0)
            return "end text, " + endText + ", not found";
        s = body.substring(beginTextIndex + beginText.length(), endTextIndex);
        return s;
    }

    /**
     * Verify we are NOT using jandex
     *
     * @throws Exception
     */
    @Test
    public void testNotUsingJandex() throws Exception {
        // Search for message indicating Jandex is being used.
        // CWWKC0092I: Read Jandex indexes for {0} out of {1} archives ({2} out of {3} classes) in {4}.
        List l = SHARED_SERVER.getLibertyServer().findStringsInLogs("CWWKC0092I");
        assertTrue("   ", l.isEmpty()); // The list should be empty
    }

    /**
     * Request a simple servlet.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleServlet() throws Exception {

        this.verifyResponse("/TestServlet40/SimpleTestServlet", "Hello World");
    }

    /**
     * Simple test to a servlet then read the header to ensure we are using
     * Servlet 4.0
     *
     * @throws Exception
     *             if something goes horribly wrong
     */
    @Test
    public void testServletHeader() throws Exception {
        WebResponse response = this.verifyResponse("/TestServlet40/MyServlet", "Hello World");

        // verify the X-Powered-By Response header
        response.verifyResponseHeaderEquals("X-Powered-By", false, "Servlet/4.0", true, false);
    }

    /**
     * Verifies that the ServletContext.getMajorVersion() returns 4 and
     * ServletContext.getMinorVersion() returns 0 for Servlet 4.0.
     *
     * @throws Exception
     */

    @Test
    public void testServletContextMajorMinorVersion() throws Exception {
        this.verifyResponse("/TestServlet40/MyServlet?TestMajorMinorVersion=true", "majorVersion: 4");

        this.verifyResponse("/TestServlet40/MyServlet?TestMajorMinorVersion=true", "minorVersion: 0");
    }

}
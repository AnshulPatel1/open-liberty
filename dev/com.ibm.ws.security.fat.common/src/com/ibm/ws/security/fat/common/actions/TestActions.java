/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.security.fat.common.actions;

import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.ibm.ws.security.fat.common.exceptions.TestActionException;
import com.ibm.ws.security.fat.common.logging.CommonFatLoggingUtils;
import com.ibm.ws.security.fat.common.web.WebFormUtils;

public class TestActions {

    public static final String ACTION_INVOKE_PROTECTED_RESOURCE = "invokeProtectedResource";
    public static final String ACTION_SUBMIT_LOGIN_CREDENTIALS = "submitLoginCredentials";

    CommonFatLoggingUtils loggingUtils = new CommonFatLoggingUtils();
    WebFormUtils webFormUtils = new WebFormUtils();

    /**
     * Invokes the specified URL and returns the Page object that represents the response.
     */
    public Page invokeUrl(String currentTest, String url) throws Exception {
        return invokeUrl(currentTest, createWebClient(), url);
    }

    /**
     * Invokes the specified URL using the provided WebClient object and returns the Page object that represents the response.
     */
    public Page invokeUrl(String currentTest, WebClient wc, String url) throws Exception {
        String thisMethod = "invokeUrl";
        loggingUtils.printMethodName(thisMethod);
        try {
            WebRequest request = createGetRequest(url);
            return submitRequest(currentTest, wc, request);
        } catch (Exception e) {
            throw new Exception("An error occurred invoking the URL [" + url + "]: " + e);
        }
    }

    /**
     * Submits the specified WebRequest and returns the Page object that represents the response.
     */
    public Page submitRequest(String currentTest, WebRequest request) throws Exception {
        return submitRequest(currentTest, createWebClient(), request);
    }

    /**
     * Submits the specified WebRequest using the provided WebClient object and returns the Page object that represents the
     * response.
     */
    public Page submitRequest(String currentTest, WebClient wc, WebRequest request) throws Exception {
        String thisMethod = "submitRequest";
        loggingUtils.printMethodName(thisMethod);

        if (request == null) {
            throw new Exception("Cannot invoke the URL because the provided WebRequest object is null.");
        }
        if (wc == null) {
            wc = createWebClient();
        }
        return submitRequestWithNonNullObjects(currentTest, wc, request);
    }

    private Page submitRequestWithNonNullObjects(String currentTest, WebClient wc, WebRequest request) throws Exception {
        String thisMethod = "submitRequestWithNonNullObjects";
        loggingUtils.printRequestParts(wc, request, currentTest);
        try {
            Page response = wc.getPage(request);
            loggingUtils.printResponseParts(response, currentTest, "Response from URL: ");
            return response;
        } catch (Exception e) {
            throw new TestActionException(thisMethod, "An error occurred while submitting a request to [" + request.getUrl() + "].", e);
        }
    }

    /**
     * Finds, fills out, and submits the standard login form in the provided page using the specified credentials. This method
     * expects the page to be an instance of HtmlPage that contains at least one form. The first form the page contains is
     * expected to have an action value of {@code j_security_check} and inputs for {@code j_username} and {@code j_password}.
     */
    public Page doFormLogin(Page loginPage, String username, String password) throws Exception {
        String thisMethod = "doFormLogin";
        loggingUtils.printMethodName(thisMethod);

        if (loginPage == null) {
            throw new Exception("Cannot perform login because the provided page object is null.");
        }
        if (!(loginPage instanceof HtmlPage)) {
            throw new Exception("Cannot perform login because the provided page object is not a " + HtmlPage.class.getName() + " instance. Page class is: "
                    + loginPage.getClass().getName());
        }
        return doFormLogin((HtmlPage) loginPage, username, password);
    }

    /**
     * Finds, fills out, and submits the standard login form in the provided page using the specified credentials. This method
     * expects the page to contain at least one form. The first form the page contains is expected to have an action value of
     * {@code j_security_check} and inputs for {@code j_username} and {@code j_password}.
     */
    public Page doFormLogin(HtmlPage loginPage, String username, String password) throws Exception {
        String thisMethod = "doFormLogin";
        loggingUtils.printMethodName(thisMethod);
        if (loginPage == null) {
            throw new Exception("Cannot perform login because the provided page object is null.");
        }
        try {
            Page postSubmissionPage = webFormUtils.getAndSubmitLoginForm(loginPage, username, password);
            loggingUtils.printResponseParts(postSubmissionPage, thisMethod, "Response from login form submission:");
            return postSubmissionPage;
        } catch (Exception e) {
            throw new TestActionException(thisMethod, "An error occurred while performing form login.", e);
        }
    }

    WebClient createWebClient() {
        return new WebClient();
    }

    WebRequest createGetRequest(String url) throws MalformedURLException {
        return new WebRequest(new URL(url), HttpMethod.GET);
    }

}

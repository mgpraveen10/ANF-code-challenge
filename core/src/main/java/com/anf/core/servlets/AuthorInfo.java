package com.anf.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import javax.jcr.Value;
import com.anf.core.constants.GlobalConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONArray;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

@Component(service = Servlet.class, property = { "sling.servlet.methods=get", "sling.servlet.paths=/bin/author" })
public class AuthorInfo extends SlingSafeMethodsServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorInfo.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        ResourceResolver requestResource=request.getResourceResolver(); 
        String extension = request.getRequestPathInfo().getExtension();
        Resource pageResource = requestResource.getResource(GlobalConstants.ANF_PAGE_PATH);
        // Get the Page object from the resource
        PageManager pageManager = requestResource.adaptTo(PageManager.class);
        if (pageManager != null) {
            Page currentPage = pageManager.getContainingPage(pageResource);
            LOGGER.info("page1{}",currentPage);
            String parentPagelastModifiedBy = currentPage.getLastModifiedBy();
              LOGGER.info("parentPagelastModified{}",parentPagelastModifiedBy);
            // Getting the First Name and last name using user manager
            List<String> userDetail = userDetails(requestResource, parentPagelastModifiedBy);
            List<String> pageTitles = getAllPageTitle(pageResource, pageManager, parentPagelastModifiedBy);
            if (GlobalConstants.JSON.equals(extension)) {
                printJsonResponse(response, userDetail, pageTitles);
            } else if (GlobalConstants.XML.equals(extension)) {
                printXmlResponse(response, userDetail, pageTitles);
            } else {
                // Handle unsupported extension
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GlobalConstants.UNSUPPORTED_ERROR + extension);
            }
        } // Set the response content type to JSON
        response.setContentType(GlobalConstants.APPLICATION_JSON);

    }

    // Get the User First Name AND Last Name in this method
    public List<String> userDetails(ResourceResolver requestResolver,
            String parentPagelastModifiedBy)
            throws IllegalStateException {
        UserManager userManager = requestResolver.adaptTo(UserManager.class);
        Value[] firstName = null;
        Value[] lastName = null;
        List<String> username = new LinkedList<>();
        if (userManager != null) {
            Authorizable authorizable;

            try {
                authorizable = userManager.getAuthorizable(parentPagelastModifiedBy);
                if (authorizable != null && authorizable.hasProperty(GlobalConstants.PROFILE_GIVEN_NAME)
                        && authorizable.hasProperty(GlobalConstants.FAMILY_GIVEN_NAME)) {
                    User user = (User) authorizable;
                    firstName = user.getProperty(GlobalConstants.PROFILE_GIVEN_NAME);
                    lastName = user.getProperty(GlobalConstants.FAMILY_GIVEN_NAME);
                    username.add(firstName[0].getString());
                    username.add(lastName[0].getString());
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }

        }
        return username;
    }

    // Get the parent page and child page of the updated user
    public List<String> getAllPageTitle(Resource pageResource,
            PageManager pageManager,
            String parentPagelastModifiedBy)

    {
        List<String> pageTitleList = new LinkedList<>();
        Iterator<Resource> resourceIterator = pageResource.listChildren();
        if (resourceIterator != null) {
            resourceIterator.forEachRemaining(resource -> {
                Page page = pageManager.getContainingPage(resource);
                if (page != null) {
                    final Resource contentResource = page.getContentResource();
                    final ValueMap pageValueMap = contentResource.getValueMap();
                    final String pageTitle = pageValueMap.get(JcrConstants.JCR_TITLE).toString();
                    String lastModifiedBy = page.getLastModifiedBy();
                    if (parentPagelastModifiedBy.equals(lastModifiedBy)) {
                        pageTitleList.add(pageTitle);
                    }
                }
            });
        }
        return pageTitleList;
    }

    //Get the Output through Json Response 
    public void printJsonResponse(SlingHttpServletResponse response, List<String> userDetail, List<String> pageTitles)
            throws IOException {
        JSONObject jsonResponse = new JSONObject();
        response.setContentType(GlobalConstants.APPLICATION_JSON);
        try {

            jsonResponse.put(GlobalConstants.FIRST_NAME, userDetail.get(0));
            jsonResponse.put(GlobalConstants.LAST_NAME, userDetail.get(1));
            JSONArray pageTitleArray = new JSONArray();
            for (String pageTitle : pageTitles) {
                pageTitleArray.put(pageTitle);
            }
            jsonResponse.put(GlobalConstants.PAGE_TITLES, pageTitleArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.getWriter().write(jsonResponse.toString());
    }


      //Get the Output through XML Response 
    public void printXmlResponse(SlingHttpServletResponse response, List<String> userDetail, List<String> pageTitles)
            throws IOException,TransformerFactoryConfigurationError {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document xmlDoc = dBuilder.newDocument();

            Element rootElement = xmlDoc.createElement(GlobalConstants.RESPONSE);
            xmlDoc.appendChild(rootElement);
            Element messageElement = xmlDoc.createElement(GlobalConstants.FIRST_NAME);
            messageElement.appendChild(xmlDoc.createTextNode(userDetail.get(0)));
            rootElement.appendChild(messageElement);
            messageElement = xmlDoc.createElement(GlobalConstants.LAST_NAME);
            messageElement.appendChild(xmlDoc.createTextNode(userDetail.get(1)));
            rootElement.appendChild(messageElement);

            Element subRootElement = xmlDoc.createElement(GlobalConstants.PAGE_TITLES);
            rootElement.appendChild(subRootElement);
            for (String title : pageTitles) {
                messageElement = xmlDoc.createElement(GlobalConstants.PAGE);
                messageElement.appendChild(xmlDoc.createTextNode(title));
                subRootElement.appendChild(messageElement);

            }
            // Transform XML document to string
            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, GlobalConstants.STRING_VALUE_TRUE);
            transformer.setOutputProperty(GlobalConstants.XML_INDENT_PATH, GlobalConstants.INTEGER_VALUE_STRING);
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
            String xmlString = writer.toString();
            response.setContentType(GlobalConstants.APPLICATION_XML);
            response.getWriter().write(xmlString);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GlobalConstants.XML_RESPONSE_ERROR);

        }

    }
}

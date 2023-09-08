package com.anf.core.servlets;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.anf.core.constants.GlobalConstants;
import com.anf.core.exceptions.InvalidParameterException;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

//** Begin Code **//
//**MG praveen *//

@Component(service = Servlet.class, property = { "sling.servlet.methods=get", "sling.servlet.paths=/bin/author" })
public class AuthorInfoServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;
	@Reference
	transient ResourceResolverFactory resolverFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorInfoServlet.class);

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {

		Map<String, Object> serviceUserMap = new HashMap<>();
		serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, GlobalConstants.SUB_SERVICE);

		try (ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(serviceUserMap)) {

			String extension = request.getRequestPathInfo().getExtension();
			if (StringUtils.isBlank(extension))
				throw new InvalidParameterException("Only JSON or XML extensions are valid");

			Resource pageResource = resourceResolver.getResource(GlobalConstants.ANF_PAGE_PATH);
			// Get the Page object from the resource
			PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
			if (pageManager != null) {
				Page currentPage = pageManager.getContainingPage(pageResource);
				String parentPagelastModifiedBy = currentPage.getLastModifiedBy();

				// Getting the First Name and last name using user manager
				List<String> userDetail = userDetails(resourceResolver, parentPagelastModifiedBy);
				List<String> pageTitles = getAllPageTitle(pageResource, parentPagelastModifiedBy);
				if (GlobalConstants.JSON.equals(extension)) {
					response.getWriter().write(printJsonResponse(response, userDetail, pageTitles));
				} else if (GlobalConstants.XML.equals(extension)) {
					printXmlResponse(response, userDetail, pageTitles);
				}
			}

		} catch (InvalidParameterException e) {
			LOGGER.error("Exception in doGet Method {}", e.getMessage());
			response.sendError(SC_BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Exception in doGet Method {}", e.getMessage());
		}

	}

	// Get the User First Name AND Last Name in this method
	private List<String> userDetails(ResourceResolver requestResolver, String parentPagelastModifiedBy)
			throws IllegalStateException {
		UserManager userManager = requestResolver.adaptTo(UserManager.class);
		Value[] firstName = null;
		Value[] lastName = null;
		List<String> userDetails = new LinkedList<>();
		if (userManager != null) {
			try {
				User user = (User) userManager.getAuthorizable(parentPagelastModifiedBy);
				if (user != null && user.hasProperty(GlobalConstants.PROFILE_GIVEN_NAME)
						&& user.hasProperty(GlobalConstants.FAMILY_GIVEN_NAME)) {

					firstName = user.getProperty(GlobalConstants.PROFILE_GIVEN_NAME);
					lastName = user.getProperty(GlobalConstants.FAMILY_GIVEN_NAME);
					userDetails.add(firstName[0].getString());
					userDetails.add(lastName[0].getString());
				}
			} catch (RepositoryException e) {
				LOGGER.error("Exception in getting userDetails Method {}", e.getMessage());
			}
		}
		return userDetails;
	}

	// Get the parent page and child page of the updated user
	private List<String> getAllPageTitle(Resource pageResource, String parentPagelastModifiedBy)

	{
		List<String> pageTitleList = new LinkedList<>();
		Iterator<Resource> resourceIterator = pageResource.listChildren();
		while (resourceIterator.hasNext()) {
			Resource childPageResource = resourceIterator.next();
			if (null != childPageResource) {
				Page page = childPageResource.adaptTo(Page.class);
				if (null != page) {
					final Resource contentResource = page.getContentResource();
					final ValueMap pageValueMap = contentResource.getValueMap();
					final String pageTitle = pageValueMap.get(JcrConstants.JCR_TITLE).toString();
					String lastModifiedBy = page.getLastModifiedBy();
					if (parentPagelastModifiedBy.equals(lastModifiedBy)) {
						pageTitleList.add(pageTitle);
					}
				}
			}
		}
		return pageTitleList;
	}

	// Get the Output through Json Response
	private String printJsonResponse(SlingHttpServletResponse response, List<String> userDetail,
			List<String> pageTitles) {
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
		return jsonResponse.toString();
	}

	// Get the Output through XML Response
	private void printXmlResponse(SlingHttpServletResponse response, List<String> userDetail, List<String> pageTitles)
			throws IOException, TransformerFactoryConfigurationError {
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

  //**END */

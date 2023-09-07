package com.anf.core.servlets;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import com.anf.core.constants.GlobalConstants;
import com.anf.core.exceptions.InvalidParameterException;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component(service = { Servlet.class })
@SlingServletPaths(value = "/bin/searchbox1")
public class ComponentSearchServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(ComponentSearchServlet.class);

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

		String textInput = request.getParameter("textInput");
		ResourceResolver resourceResolver = request.getResourceResolver();

		try {
			if(textInput == null) 
				throw new InvalidParameterException("textInput parameter is required");

			// Get the Page object from the resource
			Resource rootPageResource = resourceResolver.getResource(GlobalConstants.ANF_PAGE_PATH);
			if (rootPageResource != null) {
				
				Page rootPage = rootPageResource.adaptTo(Page.class);
				List<JSONObject> jsonResponse = new ArrayList<>();
				
				Iterator<Page> rootPageIterator = rootPage.listChildren(null, true);
				while (rootPageIterator.hasNext()) {
					getDetails(textInput, rootPageIterator, jsonResponse);
				}
				response.getWriter().print(jsonResponse);
			} else {
				response.getWriter().write(GlobalConstants.PAGE_MANAGER_ERROR);
			}
		} catch(InvalidParameterException e) {
			LOGGER.error("Exception in doGet Method {}", e.getMessage());
			response.sendError(SC_BAD_REQUEST, e.getMessage());
		}

	}

  public List<JSONObject> getDetails(String data, Iterator<Page> rootPageIterator, List<JSONObject> responses) {
    Page childPage = rootPageIterator.next();
    ValueMap pageProperties = childPage.getProperties(); 
		String pageTitle = pageProperties.get(JcrConstants.JCR_TITLE, StringUtils.EMPTY);
		String pageDesc = pageProperties.get(JcrConstants.JCR_DESCRIPTION, StringUtils.EMPTY);
    if (pageTitle.contains(data) || pageDesc.contains(data)) {
      JSONObject resp = new JSONObject();
      try {
        resp.put(GlobalConstants.TITLE, pageProperties.get(JcrConstants.JCR_TITLE, String.class));
        resp.put(GlobalConstants.DESCRIPTION, pageProperties.get(JcrConstants.JCR_DESCRIPTION, String.class));
        resp.put(GlobalConstants.LASTMODIFIED, pageProperties.get(JcrConstants.JCR_LASTMODIFIED, String.class));
        getImage(resp, childPage);
        responses.add(resp);
      } catch (Exception e) {
        LOGGER.info("Null{}", e.getMessage());
      }
    }

    return responses;

  }

  public JSONObject getImage(JSONObject resp, Page childPage) throws JSONException {
    if (childPage.getContentResource(GlobalConstants.IMAGE) != null) {
      Resource featuredImageResource = childPage.getContentResource(GlobalConstants.IMAGE);
      ValueMap imageProperty = featuredImageResource.adaptTo(ValueMap.class);
      String fileReference = imageProperty.get(GlobalConstants.IMAGE_REFERENCE, String.class);
      resp.put(GlobalConstants.IMAGE, fileReference);
    }
    return resp;
  }

}

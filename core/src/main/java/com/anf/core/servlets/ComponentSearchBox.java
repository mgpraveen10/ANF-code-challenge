package com.anf.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import com.anf.core.constants.GlobalConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Component(service = { Servlet.class })
@SlingServletPaths(value = "/bin/searchbox1")
public class ComponentSearchBox extends SlingAllMethodsServlet {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(ComponentSearchBox.class);

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

    BufferedReader bufferedReader = request.getReader();
    String data = bufferedReader.readLine();
    LOGGER.info("data from js::{}",data);

    // Get the Page object from the resource
    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    if (pageManager != null) {

      List<JSONObject> responses = new LinkedList<>();
      Page rootPage = pageManager.getPage(GlobalConstants.ANF_PAGE_PATH);
      Iterator<Page> rootPageIterator = rootPage.listChildren(null, true);
      while (rootPageIterator.hasNext()) {
        getDetails(data,rootPageIterator,responses);
      }
      response.getWriter().print(responses);
    } else {
      response.getWriter().write(GlobalConstants.PAGE_MANAGER_ERROR);
    }

  }

  public List<JSONObject> getDetails(String data, Iterator<Page> rootPageIterator, List<JSONObject> responses) {
    Page childPage = rootPageIterator.next();
    ValueMap properties = childPage.getProperties(); 
    // Get the properties of the child page
    String pageTitle = StringUtils.isNotBlank(properties.get(JcrConstants.JCR_TITLE, String.class))
        ? properties.get(JcrConstants.JCR_TITLE, String.class)
        : StringUtils.EMPTY;
    String description = StringUtils.isNotBlank(properties.get(JcrConstants.JCR_DESCRIPTION,String.class))
        ? properties.get(JcrConstants.JCR_DESCRIPTION, String.class)
        : StringUtils.EMPTY;
    if (pageTitle.contains(data) || description.contains(data)) {
      JSONObject resp = new JSONObject();
      try {
        resp.put(GlobalConstants.TITLE, properties.get(JcrConstants.JCR_TITLE, String.class));
        resp.put(GlobalConstants.DESCRIPTION, properties.get(JcrConstants.JCR_DESCRIPTION, String.class));
        resp.put(GlobalConstants.LASTMODIFIED, properties.get(JcrConstants.JCR_LASTMODIFIED, String.class));
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

package com.anf.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.ServletException;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.anf.core.constants.GlobalConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

//** Begin Code **//
//**MG praveen *//

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class AuthorInfoServletTest {

	@InjectMocks
	AuthorInfoServlet authorInfoServlet;

	@Mock
	private ResourceResolverFactory resourceResolverFactory;

	@Mock
	private ResourceResolver resourceResolver;

	@Mock
	private UserManager userManager;

	@Mock
	private Authorizable authorizable;

	@Mock
	private User user;

	AemContext context = new AemContext();

	MockSlingHttpServletRequest request;
	MockSlingHttpServletResponse response;

	@Mock
	private PageManager pageManager;

	@Mock
	private Value value;

	private static final String TEST_CHILD_PAGE = "testpage1";
	private static final String TEST_USER = "admin";

	@BeforeEach
	public void setUp() throws LoginException, RepositoryException {
		Resource pageResource = context.load().json("/AuthorInfoTest.json", "/content/anf-code-challenge/us/en");
		Page page = pageResource.adaptTo(Page.class);
		Page childPage = pageResource.getChild(TEST_CHILD_PAGE).adaptTo(Page.class);

		Map<String, Object> serviceUserMap = new HashMap<>();
		serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, GlobalConstants.SUB_SERVICE);
		lenient().when(resourceResolverFactory.getServiceResourceResolver(serviceUserMap)).thenReturn(resourceResolver);
		lenient().when(resourceResolver.getResource(GlobalConstants.ANF_PAGE_PATH)).thenReturn(pageResource);
		lenient().when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
		lenient().when(pageManager.getContainingPage(pageResource)).thenReturn(page);
		lenient().when(pageManager.getContainingPage(pageResource.getChild(JcrConstants.JCR_CONTENT))).thenReturn(null);
		lenient().when(pageManager.getContainingPage(pageResource.getChild(TEST_CHILD_PAGE))).thenReturn(childPage);

		lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);

		lenient().when(user.hasProperty(GlobalConstants.PROFILE_GIVEN_NAME)).thenReturn(Boolean.TRUE);
		lenient().when(user.hasProperty(GlobalConstants.FAMILY_GIVEN_NAME)).thenReturn(Boolean.TRUE);

		Value[] valueArr = new Value[1];
		valueArr[0] = value;

		lenient().when(user.getProperty(GlobalConstants.PROFILE_GIVEN_NAME)).thenReturn(valueArr);
		lenient().when(user.getProperty(GlobalConstants.FAMILY_GIVEN_NAME)).thenReturn(valueArr);
		lenient().when(valueArr[0].getString()).thenReturn(TEST_USER);

		lenient().when(userManager.getAuthorizable(TEST_USER)).thenReturn(user);

		request = context.request();
		response = context.response();

	}

	@Test
	void testDoGetJSON() throws ServletException, IOException {
		context.requestPathInfo().setExtension("json");
		authorInfoServlet.doGet(request, response);
		String outputString = response.getOutputAsString();
		JsonObject outputJSON = new Gson().fromJson(outputString, JsonObject.class);
		assertEquals("[\"Test Title 1\",\"Test Title 2\"]", outputJSON.get("PageTitles").toString());
	}

	@Test
	void testDoGetXML() throws ServletException, IOException {
		context.requestPathInfo().setExtension("xml");
		authorInfoServlet.doGet(request, response);
		String outputString = response.getOutputAsString();
		assertTrue(outputString.contains("Test Title 1"));
		assertFalse(outputString.contains("Test Title 3"));

	}

}

// **END */

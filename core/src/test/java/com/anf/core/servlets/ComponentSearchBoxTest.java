package com.anf.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import com.anf.core.constants.GlobalConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ComponentSearchBoxTest {
	
	@InjectMocks
	private ComponentSearchServlet componentSearchBoxServlet;

	private MockSlingHttpServletRequest mockRequest;
	private MockSlingHttpServletResponse mockResponse;
	private Map<String, Object> servletParamMap = new HashMap<>();
	
	@BeforeEach
	void setUp(AemContext context) throws LoginException {
		context.load().json("/ComponentSearchBoxServletTest.json", GlobalConstants.ANF_PAGE_PATH);
		mockRequest = context.request();
		mockResponse = context.response();
	}

	@Test
	void testDoGetWithoutParams() throws IOException {
		componentSearchBoxServlet.doGet(mockRequest, mockResponse);
		assertEquals(400, mockResponse.getStatus());
	}
	
	@Test
	void testDoGetWithParams() throws IOException {
		servletParamMap.put("textInput", "Test");
		mockRequest.setParameterMap(servletParamMap);
		componentSearchBoxServlet.doGet(mockRequest, mockResponse);
		assertNotNull(mockResponse.getOutputAsString());

	}

}

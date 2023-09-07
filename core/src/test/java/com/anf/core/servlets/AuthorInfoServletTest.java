package com.anf.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;

import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.anf.core.constants.GlobalConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;


//** Begin Code **//
//**MG praveen *//

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AuthorInfoServletTest {
	
	@InjectMocks
	private AuthorInfoServlet authorInfo;

    Resource pagResource;
	private MockSlingHttpServletRequest mockRequest;
	private MockSlingHttpServletResponse mockResponse;
	
    
	AemContext aemContext=new AemContext();
    AuthorInfoServlet author = Mockito.mock(AuthorInfoServlet.class);

    @Mock
    UserManager userManager;
   
	@BeforeEach
	void setUp(AemContext context) throws LoginException {
      pagResource=context.load().json("/AuthorInfoTest.json", GlobalConstants.ANF_PAGE_PATH);
         context.load().json("/UserInfo.json", "/home/users/testuser");
		mockRequest = context.request();
		mockResponse = context.response();
	}


	@Test
	void testDoGetWithJson() throws IOException, ServletException,IllegalStateException {     
        aemContext.requestPathInfo().setExtension("json");
        List <String> User=new LinkedList<>();
        User.add("praveen");
        User.add("mg");
      
        lenient().when(author.userDetails(aemContext.resourceResolver(), "praveen")).thenReturn(User);
		author.doGet(aemContext.request(), aemContext.response());
         assertEquals("json", aemContext.request().getRequestPathInfo().getExtension());
         assertNotNull(aemContext.response().getOutputAsString());
         System.out.println(aemContext.response().getOutputAsString());

      
       
	}
    @Test
	void testDoGetWithXml() throws IOException, ServletException,IllegalStateException {     
        aemContext.requestPathInfo().setExtension("xml");
        List <String> User=new LinkedList<>();
        User.add("praveen");
        User.add("mg");
        lenient().when(author.userDetails(aemContext.resourceResolver(), "praveen")).thenReturn(User);
		author.doGet(aemContext.request(), aemContext.response());
         assertEquals("xml", aemContext.request().getRequestPathInfo().getExtension());
         assertNotNull(aemContext.response().getOutputAsString());
       
	}
    	@Test
	void testDoGetWithoutExtension() throws IOException, ServletException {
        aemContext.requestPathInfo().setExtension(null);
         List <String> User=new LinkedList<>();
        User.add("praveen");
        User.add("mg");
        lenient().when(author.userDetails(aemContext.resourceResolver(), "praveen")).thenReturn(User);
		author.doGet(aemContext.request(), aemContext.response());
		assertEquals(200, mockResponse.getStatus());
	}
	
    

    @Test
	void printJsonResponseTest() throws IOException, ServletException,IllegalStateException {
        List<String>userDetail=new LinkedList<>();
        List<String>pageTitles=new LinkedList<>();
        String expectedValue="{\"FirstName\":\"praveen\",\"LastName\":\"mg\",\"PageTitles\":[\"English\",\"Test page 2\"]}";
        userDetail.add("praveen");
        userDetail.add("mg");
        pageTitles.add("English");
        pageTitles.add("Test page 2");
        authorInfo.printJsonResponse(mockResponse, userDetail, pageTitles);
        assertEquals( expectedValue,mockResponse.getOutputAsString());
    }

     @Test
	void printXmlResponseTest() throws IOException, ServletException,IllegalStateException {
        List<String>userDetail=new LinkedList<>();
        List<String>pageTitles=new LinkedList<>();
        String expectedValue="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + //
                "<response>\r\n" + //
                "  <FirstName>praveen</FirstName>\r\n" + //
                "  <LastName>mg</LastName>\r\n" + //
                "  <PageTitles>\r\n" + //
                "    <Page>English</Page>\r\n" + //
                "    <Page>Test page 2</Page>\r\n" + //
                "  </PageTitles>\r\n" + //
                "</response>\r\n";
        userDetail.add("praveen");
        userDetail.add("mg");
        pageTitles.add("English");
        pageTitles.add("Test page 2");
        authorInfo.printXmlResponse(mockResponse, userDetail, pageTitles);
        assertEquals( expectedValue,mockResponse.getOutputAsString());
    }

    @Test
	void getAllPageTitleTest() throws IOException, ServletException,IllegalStateException {
        List <String> pages=new LinkedList<>();
        pages.add("English");
        pages.add("Test Title 2");
        assertEquals(pages,authorInfo.getAllPageTitle(pagResource,aemContext.pageManager(),"praveen"));
        
    }
 

}
//**END */
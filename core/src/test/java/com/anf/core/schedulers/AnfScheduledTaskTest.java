
 package com.anf.core.schedulers;

import com.anf.core.constants.GlobalConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

//** Begin Code **//
//**MG praveen *//

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AnfScheduledTaskTest {
 private AnfScheduledTask fixture = new AnfScheduledTask();

    AemContext context = new AemContext();
    
    AnfSchedulerConfiguration config=Mockito.mock(AnfSchedulerConfiguration.class);
    @Mock
    ResourceResolverFactory resolverFactory;
    @Mock
    Session session;
    @Mock
    ScheduleOptions options;

    Scheduler scheduler = mock(Scheduler.class);

    private TestLogger logger = TestLoggerFactory.getTestLogger(fixture.getClass());
    
 
    @BeforeEach
    void setup() {
        TestLoggerFactory.clear();
        context.load().json("/AnfScheduledTest.json",GlobalConstants.ANF_PAGE_PATH);

    }

    @Test
    void run() throws Exception {
         Map<String, Object> serviceUserMap = new HashMap<>();
            serviceUserMap.put(ResourceResolverFactory.SUBSERVICE,GlobalConstants.SUB_SERVICE);
            lenient().when(resolverFactory.getServiceResourceResolver(serviceUserMap)).thenReturn(context.resourceResolver());
        fixture.resolverFactory=resolverFactory;
        fixture.activate(config);
        fixture.run();

        List<LoggingEvent> events = logger.getLoggingEvents();
        assertEquals(6, events.size());
        LoggingEvent event = events.get(3);
        assertEquals(Level.ERROR, event.getLevel());
    }
}
 //**END */
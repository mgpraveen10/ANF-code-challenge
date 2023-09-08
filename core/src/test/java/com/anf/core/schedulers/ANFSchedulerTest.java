package com.anf.core.schedulers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.lenient;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.anf.core.schedulers.config.AnfSchedulerConfiguration;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * author Praveen MG
 **/
@ExtendWith({MockitoExtension.class, AemContextExtension.class})
class ANFSchedulerTest {

	@Mock
	private AnfSchedulerConfiguration anfSchedulerConfiguration;

	@Mock
	ResourceResolverFactory resolverFactory;

	@Mock
	Session session;

	@Mock
	ScheduleOptions options;

	@InjectMocks
	ANFScheduler anfScheduler;

	@Mock
	Scheduler scheduler;

	AemContext context = new AemContext();

	@BeforeEach
	void setup() throws LoginException {
		context.load().json("/AnfSchedulerTest.json", "/content/anf-code-challenge/us/en");

		Map<String, Object> serviceUserMap = new HashMap<>();
		serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, "anfUser");
		lenient().when(resolverFactory.getServiceResourceResolver(serviceUserMap)).thenReturn(context.resourceResolver());

		lenient().when(anfSchedulerConfiguration.isSchedulerEnabled()).thenReturn(true);
		lenient().when(anfSchedulerConfiguration.isSchedulerConcurrent()).thenReturn(true);
		lenient().when(anfSchedulerConfiguration.schedulerExpression()).thenReturn("0 0/2 * 1/1 * ? *");
		lenient().when(anfSchedulerConfiguration.schedulerName()).thenReturn("Anf Replication Check");
		lenient().when(scheduler.EXPR((anfSchedulerConfiguration.schedulerExpression()))).thenReturn(options);

	}

	@Test
	void testRunOnActivate() {	
		anfScheduler.activate(anfSchedulerConfiguration);
		assertDoesNotThrow(() -> anfScheduler.run());
		anfScheduler.deactivate(anfSchedulerConfiguration);

	}

	@Test
	void testRunOnModify() {		
		anfScheduler.modified(anfSchedulerConfiguration);
		assertDoesNotThrow(() -> anfScheduler.run());
	}

}

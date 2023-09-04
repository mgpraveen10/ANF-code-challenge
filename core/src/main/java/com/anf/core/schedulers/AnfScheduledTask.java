package com.anf.core.schedulers;

import com.anf.core.constants.AppConstants;
import com.anf.core.constants.GlobalConstants;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.*;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import javax.jcr.Session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



@Slf4j
@Designate(ocd = AnfSchedulerConfiguration.class)
@Component(service = Runnable.class, property = {"sling.run.modes=author"})
public class AnfScheduledTask implements Runnable {

    @Reference
    Scheduler scheduler;

    @Reference
    ResourceResolverFactory resolverFactory;


    @Activate
    protected void activate(final AnfSchedulerConfiguration config) {

        // Execute this method to add scheduler.
        addScheduler(config);

    }

    @Deactivate
    protected void deactivate(AnfSchedulerConfiguration config) {
        removeScheduler(config);
    }

    // On component modification change status will remove and add scheduler
    @Modified
    protected void modified(AnfSchedulerConfiguration config) {
        removeScheduler(config);
        addScheduler(config);
    }

    //     Add all configurations to Schedule a scheduler depending on name and expression.
    public void addScheduler(AnfSchedulerConfiguration config) {
        if (config.isSchedulerEnabled()) {
            ScheduleOptions options = scheduler.EXPR(config.schedulerExpression());
            options.name(config.schedulerName());
            options.canRunConcurrently(config.isSchedulerConcurrent());

            // Add scheduler to call depending on option passed.
            scheduler.schedule(this, options);
            log.info("Scheduler added successfully name='{}'", config.schedulerName());
        } else {
            log.info("SimpleScheduledTask disabled");
        }
    }

    // Custom method to deactivate or unschedule scheduler
    public void removeScheduler(AnfSchedulerConfiguration config) {
        scheduler.unschedule(config.schedulerName());
    }

    @Override
    public void run() {
        log.info("runs every two minutes >>>>>>>>>>>");
        findPublishedPages();
    }

    private void findPublishedPages() {
         Map<String, Object> serviceUserMap = new HashMap<>();
            serviceUserMap.put(ResourceResolverFactory.SUBSERVICE,GlobalConstants.SUB_SERVICE);
          

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(serviceUserMap);
        ) {
           
// Get the PageManager to work with pages
            PageManager pageManager = resolver.adaptTo(PageManager.class);

            // Define the path to the root of your website
            String siteRootPath =GlobalConstants.ANF_PAGE_PATH; // Replace with your actual path

            // Get the root page
            Page rootPage = pageManager.getPage(siteRootPath);
            if (rootPage != null) {
                // Recursively check if all child pages are published
                checkPublishedStatus(rootPage,resolver);
            } else {
                log.error("Root page not found.");
            }
        } catch (LoginException e) {
            log.error("Error getting page : {} ", e.getMessage());
        }
    }

    // Recursively check the published status of child pages
    private void checkPublishedStatus(Page page,ResourceResolver resolver) {
        if (page != null) {
            ReplicationStatus replicationStatus = page.adaptTo(ReplicationStatus.class);

            Session session = resolver.adaptTo(Session.class);
            try {
                if (replicationStatus.isActivated()) {
                    DateTime dateTime = new DateTime();
                    Resource resource=resolver.getResource(page.getPath()+"/jcr:content");
                    ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
                    valueMap.put(GlobalConstants.PROCESSED_DATA, dateTime.toLocalDateTime().toString());
                    // Handle the case where the page is not published
                    session.save();
                }
            } catch (Exception e) {
                log.error("Exception saving processedDate {} ", e);
            }

            // Recursively check child pages
            Iterator<Page> childPages = page.listChildren();
            while (childPages.hasNext()) {
                Page childPage = childPages.next();
                checkPublishedStatus(childPage,resolver);
            }
        }
    }
}

package com.anf.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

//** Begin Code **//
//**MG praveen *//

@ObjectClassDefinition(name = "ANF Scheduled Task",
        description = "Corn job to find published page once every two minutes" +
                " and set property processedDate to current time ")
public @interface AnfSchedulerConfiguration {

    @AttributeDefinition(
            name = "Scheduler name",
            description = "Scheduler name",
            type = AttributeType.STRING)
    String schedulerName() default "Anf Replication Check";
    @AttributeDefinition(name = "Corn job expression")
    String schedulerExpression() default "0 0/2 * 1/1 * ? *";

    @AttributeDefinition(name = "Concurrent task",
            description = "Whether or not to schedule this task concurrently",
            type = AttributeType.BOOLEAN)
    boolean isSchedulerConcurrent() default false;

    @AttributeDefinition(
            name = "Enable Scheduler",
            description = "Enable Scheduler",
            type = AttributeType.BOOLEAN)
    boolean isSchedulerEnabled() default true;

    @AttributeDefinition(
            name = "Custom Property",
            description = "Custom Property",
            type = AttributeType.STRING)
    String customProperty() default "Test";
}

    
//**END */
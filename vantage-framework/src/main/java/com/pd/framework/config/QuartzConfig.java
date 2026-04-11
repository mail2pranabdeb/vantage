package com.pd.framework.config;

import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Quartz configuration
 */
@Configuration
public class QuartzConfig {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    /**
     * Job factory that supports Spring bean autowiring
     */
    public static final class AutowireCapableJobFactory extends SpringBeanJobFactory {

        private final AutowireCapableBeanFactory beanFactory;

        public AutowireCapableJobFactory(AutowireCapableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object jobInstance = super.createJobInstance(bundle);
            beanFactory.autowireBean(jobInstance);
            return jobInstance;
        }
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setJobFactory(new AutowireCapableJobFactory(beanFactory));
        factoryBean.setOverwriteExistingJobs(true);
        factoryBean.setWaitForJobsToCompleteOnShutdown(true);
        return factoryBean;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factoryBean) {
        return factoryBean.getScheduler();
    }
}

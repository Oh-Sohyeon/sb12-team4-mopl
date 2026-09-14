package com.mopl.batch.content.job;

import com.mopl.batch.content.tasklet.SportsDbContentCollectionTasklet;
import com.mopl.batch.content.tasklet.TmdbContentCollectionTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ContentCollectionJobConfig {

    private static final String JOB_NAME = "contentCollectionJob";
    private static final String TMDB_STEP_NAME = "tmdbContentCollectionStep";
    private static final String SPORTS_DB_STEP_NAME = "sportsDbContentCollectionStep";

    @Bean
    public Job contentCollectionJob(
            JobRepository jobRepository,
            @Qualifier(TMDB_STEP_NAME) Step tmdbContentCollectionStep,
            @Qualifier(SPORTS_DB_STEP_NAME) Step sportsDbContentCollectionStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tmdbContentCollectionStep)
                .next(sportsDbContentCollectionStep)
                .build();

    }

    @Bean
    public Step tmdbContentCollectionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TmdbContentCollectionTasklet tasklet
    ) {
        return new StepBuilder(TMDB_STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step sportsDbContentCollectionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SportsDbContentCollectionTasklet tasklet
    ) {
        return new StepBuilder(SPORTS_DB_STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}

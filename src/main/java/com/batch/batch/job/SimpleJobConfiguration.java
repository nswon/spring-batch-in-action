package com.batch.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    //Job은 하나의 배치 실행 단위댜.
    @Bean
    public Job simpleJob() {
        return new JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1(null))
            .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}}") String requestData) {
        return new StepBuilder("simpleStep1", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step1");
                log.info(">>>>> requestDate = {}", requestData);
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }
}

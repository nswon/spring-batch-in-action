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
public class SimpleJobParameterJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job simpleJobParameterJob() {
        return new JobBuilder("simpleJobParameterJob", jobRepository)
            .start(simpleJobParameterStep1(null))
            .next(simpleJobParameterStep2(null))
            .build();
    }

    @Bean
    @JobScope
    public Step simpleJobParameterStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new StepBuilder("simpleJobParameterStep1", jobRepository)
            .tasklet(((contribution, chunkContext) -> {

                /*
                throw new IllegalArgumentException("step1에서 에러발생");
                requestDate=20180807 으로 돌리면 에러가 난다.

                그다음 정상로직으로 바꾸고, requestDate는 그대로 20180807로 돌려도 오류없이 재수행이 된다.
                 */
                log.info(">>>>> This is Step1");
                log.info(">>>>> requestDate = {}", requestDate);
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    @JobScope
    public Step simpleJobParameterStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new StepBuilder("simpleJobParameterStep2", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step2");
                log.info(">>>>> requestDate = {}", requestDate);
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }
}

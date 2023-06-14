package com.batch.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    //Job은 하나의 배치 작업 단위
    //ex) "모든 사용자의 이메일로 회원가입 축하 메세지를 보내자!"가 통째로 하나의 Job이다.
    @Bean
    public Job simpleJob() {
        return new JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1())
            .build();
    }

    //하나의 Job은 여러 Step을 가질 수 있다.
    @Bean
    public Step simpleStep1() {
        return new StepBuilder("simpleStep1", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step1");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }
}

package com.batch.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeciderJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job deciderJob() {
        return new JobBuilder("deciderJob", jobRepository)
            .start(startStep()) //시작 로그 남김
            .next(decider()) //홀수인지 짝수인지 판단
            .from(decider()) //decider 상태가
            .on("ODD") //홀수라면
            .to(oddStep()) //oddStep으로 간다
            .from(decider()) //decider 상태가
            .on("EVEN") //짝수라면
            .to(evenStep()) //evenStep으로 간다
            .end() //builder 종료
            .build();
    }

    @Bean
    public Step startStep() {
        return new StepBuilder("startStep", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> Start!");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    public Step oddStep() {
        return new StepBuilder("oddStep", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> 홀수입니다.");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    public Step evenStep() {
        return new StepBuilder("evenStep", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> 짝수입니다.");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new OddDecider();
    }

    /*
    JobExecutiondecider은 Step들의 Flow속에서 분기만 담당하는 타입이다.
    원래라면 Step이 직접 ExitStatus를 조작했는데 (Step이 기존 역할과 분기로직의 역할 둘 다 가지고 있었음),
    JobExecutiondecider를 사용하면 Step과 역할과 책임이 분리된 상태에서 진행할 수 있다.
     */
    public static class OddDecider implements JobExecutionDecider {

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            Random random = new Random();

            int randomNumber = random.nextInt(50) + 1;
            log.info("랜덤숫자: {}", randomNumber);

            if(randomNumber % 2 == 0) {
                return new FlowExecutionStatus("EVEN");
            }
            return new FlowExecutionStatus("ODD");
        }
    }
}

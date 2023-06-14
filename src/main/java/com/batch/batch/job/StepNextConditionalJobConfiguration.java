package com.batch.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
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
public class StepNextConditionalJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * .on()
     * 캐치할 ExitStatus 지정, *일 경우 모든 ExitStatus가 지정된다.
     *
     * .to()
     * 다음으로 이동할 Step 지정
     *
     * from()
     * 상태값을 보고 일치하는 상태라면 to()에 포함된 step을 호출한다.
     * step1의 이벤트 캐치가 FAILED로 되있는 상태에서 추가로 이벤트 캐치하려면 from을 써야만 한다
     * @return
     */
    @Bean
    public Job stepNextConditionalJob() {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
            .start(conditionalJobStep1())
                .on("FAILED") // FAILED일 경우
                .to(conditionalJobStep3()) // step3으로 이동한다.
                .on("*") // step3의 결과와 상관없이
                .end() // step3로 이동하면 Flow가 종료한다.
            .from(conditionalJobStep1()) // step1으로부터
                .on("*") //FAILED 외에 모든 경우
                .to(conditionalJobStep2()) // step2으로 이동한다.
                .next(conditionalJobStep3()) // step가 정상 종료되면 step3으로 이동한다.
                .on("*") // step3의 결과 관계 없이
                .end() // step3으로 이동하면 Flow가 종료한다.
            .end() // Job 종료
            .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return new StepBuilder("step1", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step1");

                /**
                 * ExitStatus를 FAILED로 지정한다.
                 * 해당 status를 보고 flow가 진행된다.
                 */
                contribution.setExitStatus(ExitStatus.FAILED);

                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return new StepBuilder("step2", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step2");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return new StepBuilder("step3", jobRepository)
            .tasklet(((contribution, chunkContext) -> {
                log.info(">>>>> This is Step3");
                return RepeatStatus.FINISHED;
            }), transactionManager)
            .build();
    }
}

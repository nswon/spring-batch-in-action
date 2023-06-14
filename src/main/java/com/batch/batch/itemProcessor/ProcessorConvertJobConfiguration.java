package com.batch.batch.itemProcessor;

import com.batch.batch.entity.Teacher;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ItemProcessor을 사용하는 2가지 이유 (Why? processor은 필수가 아니다. Chunk 지향처리에서 필수인 것은 reader와 writer뿐이다.)
 * 1. 변환
 * 2. 필터
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProcessorConvertJobConfiguration {
    public static final String JOB_NAME = "ProcessorConvertBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job processorConvertJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .preventRestart()
            .start(processorConvertStep())
            .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step processorConvertStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
            .<Teacher, String>chunk(chunkSize, transactionManager)
            .reader(processorConvertReader())
            .processor(processorConvertProcessor())
            .writer(processorConvertWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> processorConvertReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
            .name(BEAN_PREFIX + "reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString("SELECT t FROM Teacher t")
            .build();
    }

    //첫번째 타입은 Reader에서 읽어올 타입, 두번째 타입은 Writer에 넘겨줄 타입
    @Bean
    public ItemProcessor<Teacher, String> processorConvertProcessor() {
        return Teacher::getName;
    }

    private ItemWriter<String> processorConvertWriter() {
        return items -> items.forEach(item -> log.info("Teacher Name={}", item));
    }
}

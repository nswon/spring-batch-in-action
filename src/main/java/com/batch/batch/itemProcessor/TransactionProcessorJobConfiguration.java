package com.batch.batch.itemProcessor;

import com.batch.batch.entity.ClassInformation;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionProcessorJobConfiguration {

    public static final String JOB_NAME = "transactionProcessorBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job transactionProcessorJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .preventRestart()
            .start(transactionProcessorStep())
            .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step transactionProcessorStep() {
        return new StepBuilder(BEAN_PREFIX + "step", jobRepository)
            .<Teacher, ClassInformation>chunk(chunkSize, transactionManager)
            .reader(transactionProcessorReader())
            .processor(transactionProcessor())
            .writer(transactionProcessorWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> transactionProcessorReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
            .name(BEAN_PREFIX + "reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString("SELECT t FROM Teacher t")
            .build();
    }

    public ItemProcessor<Teacher, ClassInformation> transactionProcessor() {
        return teacher -> new ClassInformation(teacher.getName(), teacher.getStudents().size());
    }

    private ItemWriter<ClassInformation> transactionProcessorWriter() {
        return items -> {
            log.info(">>>>> Item Writer");
            for (ClassInformation item: items) {
                log.info("반 정보={}", item);
            }
        };
    }
}

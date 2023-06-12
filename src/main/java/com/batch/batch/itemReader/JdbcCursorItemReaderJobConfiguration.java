package com.batch.batch.itemReader;

import com.batch.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
/*
CursorItemReader를 사용하려면 Database와 SocketTimeout을 큰 값으로 설정해야만 한다.
하나의 connection으로 batch가 끝날 때까지 사용되기 때문에 batch가 끝나기 전에 애플리케이션의 connection이 먼저 끊어질 수 있다.
그래서 Batch 수행 시간이 오래 걸리는 경우에는 PagingItemReader를 사용하는게 낫다
 */
public class JdbcCursorItemReaderJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return new JobBuilder("jdbcCursorItemReaderJob", jobRepository)
            .start(jdbcCursorItemReaderStep())
            .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() {
        return new StepBuilder("jdbcCursorItemReaderStep", jobRepository)
            //첫번째 Pay는 Reader 반환값이고, 두번째 Pay는 Writer의 파라미터다.
            .<Pay, Pay>chunk(chunkSize, transactionManager)
            .reader(jdbcCursorItemReader())
            .writer(jdbcCursorItemWriter())
            .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
            .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
            .name("jdbcCursorItemReader")
            .build();
    }

    @Bean
    public ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            for(Pay pay : list) {
                log.info("Current Pay = {}", pay);
            }
        };
    }
}

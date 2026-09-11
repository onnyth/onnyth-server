package com.onnyth.onnythserver.bookmark.adapter.out.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookmarkCreatedTopic() {
        return TopicBuilder
                .name(BookmarkKafkaTopics.BOOKMARK_CREATED_V1)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

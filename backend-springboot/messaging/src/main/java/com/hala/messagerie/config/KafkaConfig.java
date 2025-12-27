package com.hala.messagerie.config;

import com.hala.messagerie.dto.NotificationDTO;
import com.hala.messagerie.dto.PrivateMessageDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // === Notification Producer ===
    @Bean
    public ProducerFactory<String, NotificationDTO> notificationProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "notificationKafkaTemplate")
    public KafkaTemplate<String, NotificationDTO> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    // === Notification Consumer ===
    @Bean
    public ConsumerFactory<String, NotificationDTO> notificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(NotificationDTO.class, false)
        );
    }

    @Bean(name = "notificationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, NotificationDTO> notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        return factory;
    }


    // === PrivateMessage Producer ===
    @Bean
    public ProducerFactory<String, PrivateMessageDTO> privateMessageProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "privateMessageKafkaTemplate")
    public KafkaTemplate<String, PrivateMessageDTO> privateMessageKafkaTemplate() {
        return new KafkaTemplate<>(privateMessageProducerFactory());
    }
    @Bean
    public ConsumerFactory<String, PrivateMessageDTO> privateMessageConsumerFactory() {
        JsonDeserializer<PrivateMessageDTO> deserializer = new JsonDeserializer<>(PrivateMessageDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.hala.messagerie.dto"); // 🔥 INDISPENSABLE
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "private-message-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
    @Bean(name = "deleteMessageKafkaTemplate")
    public KafkaTemplate<String, Long> deleteMessageKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Long> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }


    @Bean(name = "privateMessageKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PrivateMessageDTO> privateMessageKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PrivateMessageDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(privateMessageConsumerFactory());
        return factory;
    }

}

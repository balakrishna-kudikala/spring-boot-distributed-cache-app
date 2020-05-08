package com.springboot.xoriant.distributed.cache.config;

import oracle.jms.AQjmsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.sql.DataSource;

@Configuration
public class OracleAQConfig {

    @Value("${aq.datasource.url}")
    private String databaseUrl;

    @Value("${aq.datasource.username}")
    private String username;

    @Value("${aq.datasource.password}")
    private String password;

    @Value("${aq.datasource.driverClassName}")
    private String driverClassName;

    @Value("${aq.topic.name}")
    private String topicName;

    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(aqDataSource());
        return manager;
    }

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        return AQjmsFactory.getTopicConnectionFactory(aqDataSource());
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTemplate;
    }

    @Bean
    public JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory,
                                                               DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    @ConfigurationProperties(prefix = "aq.datasource")
    public DataSource aqDataSource(){
        return DataSourceBuilder.create()
                .type(oracle.jdbc.pool.OracleConnectionPoolDataSource.class)
                .driverClassName(driverClassName).url(databaseUrl)
                .username(username)
                .password(password).build();
    }
}

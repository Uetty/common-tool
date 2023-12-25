package com.uetty.common.tool.core.cloud.aliyun.redis.configdemo;

import com.uetty.common.tool.core.cloud.aliyun.kms.configdemo.KmsConfiguration;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Configuration
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
public class RedisConfiguration {


    @AutoConfigureAfter(KmsConfiguration.class)
    @Configuration
    @ConditionalOnProperty(value = "spring.redis.enable", havingValue = "true")
    public class ConditionalRedisConfiguration {

        @Autowired
        RedisProperties redisProperties;

        @Value("${spring.redis.trustCaPath:/opt/runtime/security/Redis-CA-Chain.jks}")
        String redisTrustCaPath;

        @Bean
        public LettuceConnectionFactory redisConnectionFactory(
                ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                ClientResources clientResources) {

            LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(builderCustomizers, clientResources,
                    redisProperties.getLettuce().getPool());
            return createLettuceConnectionFactory(clientConfig);
        }

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean(ClientResources.class)
        DefaultClientResources lettuceClientResources() {
            return DefaultClientResources.create();
        }

        /**
         * 替换掉默认的JdkSerializationRedisSerializer
         * <p>可供选择的除了默认的JDKSerializer外，还有JacksonJsonRedisSerializer和GenericJackson2JsonRedisSerializer</p>
         * <p>JacksonJsonRedisSerializer和GenericJackson2JsonRedisSerializer的区别：</p>
         * <p>GenericJackson2JsonRedisSerializer在json中加入@class属性，类的全路径包名，方便反系列化。</p>
         * <p>JacksonJsonRedisSerializer如果存放了List则在反系列化的时候，如果没指定TypeReference则会报错java.util.LinkedHashMap cannot be cast</p>
         */
        @Bean(name = "redisTemplate")
        @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(connectionFactory);
            // 使用Jackson2JsonRedisSerialize 替换默认序列化
            GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                    new GenericJackson2JsonRedisSerializer();

            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

            // 设置value的序列化规则和 key的序列化规则
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.setKeySerializer(stringRedisSerializer);

            redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer);
            redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

            redisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer);
            redisTemplate.setEnableDefaultSerializer(true);
            redisTemplate.afterPropertiesSet();

            return redisTemplate;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }

        private LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration) {
            return new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
        }

        protected final RedisStandaloneConfiguration getStandaloneConfig() {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            if (StringUtils.hasText(this.redisProperties.getUrl())) {
                RedisConnectionInfo connectionInfo = parseUrl(this.redisProperties.getUrl());
                config.setHostName(connectionInfo.getHostName());
                config.setPort(connectionInfo.getPort());
                config.setUsername(connectionInfo.getUsername());
                config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
            } else {
                config.setHostName(this.redisProperties.getHost());
                config.setPort(this.redisProperties.getPort());
                config.setUsername(this.redisProperties.getUsername());
                config.setPassword(RedisPassword.of(this.redisProperties.getPassword()));
            }
            config.setDatabase(this.redisProperties.getDatabase());
            return config;
        }

        private LettuceClientConfiguration getLettuceClientConfiguration(
                ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                ClientResources clientResources, RedisProperties.Pool pool) {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = createBuilder(pool);
            applyProperties(builder);
            builder.clientOptions(createClientOptions());
            builder.clientResources(clientResources);
            builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
            return builder.build();
        }

        private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool pool) {
            return LettuceClientConfiguration.builder();
        }

        private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyProperties(
                LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
            if (redisProperties.isSsl()) {
                builder.useSsl();
            }
            if (redisProperties.getTimeout() != null) {
                builder.commandTimeout(redisProperties.getTimeout());
            }
            if (redisProperties.getLettuce() != null) {
                RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
                if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                    builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
                }
            }
            if (StringUtils.hasText(redisProperties.getClientName())) {
                builder.clientName(redisProperties.getClientName());
            }
            return builder;
        }

        private ClientOptions createClientOptions() {
            ClientOptions.Builder builder = initializeClientOptionsBuilder();
            if (redisProperties.isSsl()) {
                SslOptions sslOptions = SslOptions.builder().jdkSslProvider().truststore(new File(redisTrustCaPath)).build();
                builder.sslOptions(sslOptions);
            }
            Duration connectTimeout = redisProperties.getConnectTimeout();
            if (connectTimeout != null) {
                builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
            }
            return builder.timeoutOptions(TimeoutOptions.enabled()).build();
        }

        private ClientOptions.Builder initializeClientOptionsBuilder() {
            if (redisProperties.getCluster() != null) {
                ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
                RedisProperties.Lettuce.Cluster.Refresh refreshProperties = redisProperties.getLettuce().getCluster().getRefresh();
                ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                        .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
                if (refreshProperties.getPeriod() != null) {
                    refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
                }
                if (refreshProperties.isAdaptive()) {
                    refreshBuilder.enableAllAdaptiveRefreshTriggers();
                }
                return builder.topologyRefreshOptions(refreshBuilder.build());
            }
            return ClientOptions.builder();
        }

        protected RedisConnectionInfo parseUrl(String url) {
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme();
                if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                    throw new RuntimeException("invalid url : " + url);
                }
                boolean useSsl = ("rediss".equals(scheme));
                String username = null;
                String password = null;
                if (uri.getUserInfo() != null) {
                    String candidate = uri.getUserInfo();
                    int index = candidate.indexOf(':');
                    if (index >= 0) {
                        username = candidate.substring(0, index);
                        password = candidate.substring(index + 1);
                    } else {
                        password = candidate;
                    }
                }
                return new RedisConnectionInfo(uri, useSsl, username, password);
            } catch (URISyntaxException ex) {
                throw new RuntimeException("invalid url : " + url, ex);
            }
        }
    }

    static class RedisConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        RedisConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }
}

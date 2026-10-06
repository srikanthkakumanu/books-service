package com.books.infrastructure.platform;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PlatformDirectoryProperties.class)
class PlatformDirectoryConfiguration {

	static final String BUILDER = "platformRestClientBuilder";

	/** Resolves {@code http://user-service} and {@code http://auth-service} through the registry. */
	@Bean(BUILDER)
	@LoadBalanced
	@ConditionalOnProperty(name = "platform.directory.load-balanced", havingValue = "true", matchIfMissing = true)
	RestClient.Builder registryRestClientBuilder(PlatformDirectoryProperties properties) {
		return builder(properties);
	}

	/** Calls the configured addresses as they are. */
	@Bean(BUILDER)
	@ConditionalOnProperty(name = "platform.directory.load-balanced", havingValue = "false")
	RestClient.Builder directRestClientBuilder(PlatformDirectoryProperties properties) {
		return builder(properties);
	}

	@Bean
	ServiceTokenProvider serviceTokenProvider(@Qualifier(BUILDER) RestClient.Builder builder,
			PlatformDirectoryProperties properties) {
		return new ServiceTokenProvider(builder.clone().baseUrl(properties.authServiceUrl()).build(), properties);
	}

	@Bean
	UserServiceDirectoryAdapter userServiceDirectoryAdapter(@Qualifier(BUILDER) RestClient.Builder builder,
			PlatformDirectoryProperties properties, ServiceTokenProvider tokens) {
		return new UserServiceDirectoryAdapter(builder.clone().baseUrl(properties.userServiceUrl()).build(), tokens);
	}

	private static RestClient.Builder builder(PlatformDirectoryProperties properties) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(properties.connectTimeout());
		requestFactory.setReadTimeout(properties.readTimeout());
		return RestClient.builder().requestFactory(requestFactory);
	}
}

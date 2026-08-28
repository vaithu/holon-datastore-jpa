/*
 * Copyright 2016-2025 Axioma srl.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa.spring.boot;

import com.holonplatform.datastore.jpa.internal.cache.CachingInterceptor;
import com.holonplatform.datastore.jpa.internal.cache.QueryCacheBuilder;
import com.holonplatform.datastore.jpa.internal.cache.QueryResultCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for query result caching.
 * 
 * Provides automatic setup of {@link QueryResultCache} and {@link CachingInterceptor}
 * with configurable properties:
 * <ul>
 *   <li>{@code holon.datastore.cache.enabled} - Enable/disable caching (default: true)</li>
 *   <li>{@code holon.datastore.cache.ttl-minutes} - TTL in minutes (default: 5)</li>
 *   <li>{@code holon.datastore.cache.max-size} - Max cache entries (default: 1000)</li>
 * </ul>
 * 
 * Usage in application.yml:
 * <pre>
 * holon:
 *   datastore:
 *     cache:
 *       enabled: true
 *       ttl-minutes: 10
 *       max-size: 5000
 * </pre>
 *
 * @since 12.0.0
 */
@Configuration
@EnableConfigurationProperties(QueryCacheProperties.class)
public class QueryCacheAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
        name = "holon.datastore.cache.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public QueryResultCache queryResultCache(QueryCacheProperties properties) {
        return QueryCacheBuilder.builder()
            .ttlMinutes(properties.getTtlMinutes())
            .maxSize(properties.getMaxSize())
            .build();
    }

    @Bean
    @ConditionalOnProperty(
        name = "holon.datastore.cache.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public CachingInterceptor cachingInterceptor(QueryResultCache queryResultCache) {
        return new CachingInterceptor(queryResultCache);
    }
}

/**
 * Configuration properties for query result cache.
 */
@ConfigurationProperties(prefix = "holon.datastore.cache")
class QueryCacheProperties {

    /**
     * Enable query result caching. Default: true
     */
    private boolean enabled = true;

    /**
     * TTL in minutes. Default: 5 minutes
     */
    private int ttlMinutes = 5;

    /**
     * Maximum cache size (number of entries). Default: 1000
     */
    private int maxSize = 1000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTtlMinutes() {
        return ttlMinutes;
    }

    public void setTtlMinutes(int ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}

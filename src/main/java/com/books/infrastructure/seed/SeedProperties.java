package com.books.infrastructure.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

/**
 * Whether the starter catalog is loaded at startup, and from where. On in development, off
 * elsewhere (see service-configs).
 */
@ConfigurationProperties("books.seed")
record SeedProperties(@DefaultValue("false") boolean enabled,
		@DefaultValue("classpath:data/authors.json") Resource authors,
		@DefaultValue("classpath:data/books.json") Resource books) {
}

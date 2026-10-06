package com.books.infrastructure.platform;

import java.time.Duration;
import java.util.UUID;

import com.books.domain.exception.UserDirectoryUnavailableException;
import com.books.domain.model.OwnerId;
import com.books.domain.model.UserStanding;
import com.books.support.StubPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** The user lookup against a stand-in for auth-service and user-service, over real HTTP. */
class UserServiceDirectoryAdapterTest {

	private final StubPlatform platform = new StubPlatform();
	private final OwnerId user = new OwnerId(UUID.randomUUID());

	@AfterEach
	void stop() {
		platform.stop();
	}

	@Test
	void anActiveUserIsActive() {
		platform.user(user.toString(), "ACTIVE");

		assertThat(adapter(StubPlatform.CLIENT_SECRET).standingOf(user)).isEqualTo(UserStanding.ACTIVE);
		assertThat(platform.authorizationHeaders()).containsExactly("Bearer service-token-1");
	}

	@Test
	void aDisabledOrLockedUserIsInactiveAndAMissingOneUnknown() {
		OwnerId locked = new OwnerId(UUID.randomUUID());
		platform.user(user.toString(), "DISABLED");
		platform.user(locked.toString(), "LOCKED");
		var adapter = adapter(StubPlatform.CLIENT_SECRET);

		assertThat(adapter.standingOf(user)).isEqualTo(UserStanding.INACTIVE);
		assertThat(adapter.standingOf(locked)).isEqualTo(UserStanding.INACTIVE);
		assertThat(adapter.standingOf(new OwnerId(UUID.randomUUID()))).isEqualTo(UserStanding.UNKNOWN);
	}

	@Test
	void theServiceTokenIsReusedUntilItIsAboutToExpire() {
		platform.user(user.toString(), "ACTIVE");
		var adapter = adapter(StubPlatform.CLIENT_SECRET);

		adapter.standingOf(user);
		adapter.standingOf(user);
		assertThat(platform.tokensIssued()).isEqualTo(1);

		// A token that lives no longer than the safety margin is never reused.
		platform.tokenLifetimeSeconds(10);
		var shortLived = adapter(StubPlatform.CLIENT_SECRET);
		shortLived.standingOf(user);
		shortLived.standingOf(user);
		assertThat(platform.tokensIssued()).isEqualTo(3);
	}

	@Test
	void aRefusedTokenIsReplacedOnceAndTheLookupRepeated() {
		platform.user(user.toString(), "ACTIVE");
		var adapter = adapter(StubPlatform.CLIENT_SECRET);
		adapter.standingOf(user);

		platform.revokeIssuedToken();

		assertThat(adapter.standingOf(user)).isEqualTo(UserStanding.ACTIVE);
		assertThat(platform.tokensIssued()).isEqualTo(2);
		assertThat(platform.authorizationHeaders()).containsExactly("Bearer service-token-1", "Bearer service-token-1",
				"Bearer service-token-2");
	}

	@Test
	void aFailingUserServiceIsReportedAsUnavailableNotAsAnUnknownUser() {
		platform.user(user.toString(), "ACTIVE");
		platform.failUserLookupsWith(500);

		assertThatExceptionOfType(UserDirectoryUnavailableException.class)
				.isThrownBy(() -> adapter(StubPlatform.CLIENT_SECRET).standingOf(user))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("user-directory-unavailable"));
	}

	@Test
	void wrongOrMissingClientCredentialsAreReportedAsUnavailable() {
		platform.user(user.toString(), "ACTIVE");

		assertThatExceptionOfType(UserDirectoryUnavailableException.class)
				.isThrownBy(() -> adapter("wrong-secret").standingOf(user));
		assertThatExceptionOfType(UserDirectoryUnavailableException.class).isThrownBy(() -> adapter("").standingOf(user));
		assertThat(platform.authorizationHeaders()).isEmpty();
	}

	@Test
	void anUnreachablePlatformIsReportedAsUnavailable() {
		var properties = properties("http://localhost:1", StubPlatform.CLIENT_SECRET);
		var configuration = new PlatformDirectoryConfiguration();
		var builder = configuration.directRestClientBuilder(properties);
		var adapter = configuration.userServiceDirectoryAdapter(builder, properties,
				configuration.serviceTokenProvider(builder, properties));

		assertThatExceptionOfType(UserDirectoryUnavailableException.class).isThrownBy(() -> adapter.standingOf(user));
	}

	@Test
	void thePropertiesNeverPrintTheSecret() {
		assertThat(properties(platform.url(), "s3cret-value")).asString().doesNotContain("s3cret-value")
				.contains("books-service");
	}

	private UserServiceDirectoryAdapter adapter(String clientSecret) {
		var properties = properties(platform.url(), clientSecret);
		var configuration = new PlatformDirectoryConfiguration();
		RestClient.Builder builder = configuration.directRestClientBuilder(properties);
		return configuration.userServiceDirectoryAdapter(builder, properties,
				configuration.serviceTokenProvider(builder, properties));
	}

	private static PlatformDirectoryProperties properties(String url, String clientSecret) {
		return new PlatformDirectoryProperties(url, url, StubPlatform.CLIENT_ID, clientSecret, false,
				Duration.ofSeconds(2), Duration.ofSeconds(5));
	}
}

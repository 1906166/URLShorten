package code.practice.URLShorten;

import code.practice.URLShorten.model.RedirectResult;
import code.practice.URLShorten.model.UrlMetadata;
import code.practice.URLShorten.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UrlShortenApplicationTests {

	@Autowired
	private IdGeneratorService idGeneratorService;

	@Autowired
	private RedisCacheService cacheService;

	@Autowired
	private MockDatabaseService databaseService;

	@Autowired
	private ShortenService shortenService;

	@Autowired
	private RedirectService redirectService;

	@BeforeEach
	void setUp() {
		// Reset states before each test run
		cacheService.clear();
		databaseService.clear();
	}

	@Test
	void contextLoads() {
		// Basic context load test
		assertNotNull(idGeneratorService);
		assertNotNull(cacheService);
		assertNotNull(databaseService);
		assertNotNull(shortenService);
		assertNotNull(redirectService);
	}

	@Test
	void testIdGeneratorEncodingDecoding() {
		long originalId = 9876543210L;
		
		// Encode
		String encoded = idGeneratorService.encode(originalId);
		assertNotNull(encoded);
		assertFalse(encoded.isEmpty());
		
		// Decode
		long decoded = idGeneratorService.decode(encoded);
		assertEquals(originalId, decoded);
	}

	@Test
	void testMockDatabaseStorage() {
		UrlMetadata metadata = new UrlMetadata("testKey", "https://example.com", null, null);
		databaseService.save(metadata);
		
		Optional<UrlMetadata> retrieved = databaseService.findById("testKey");
		assertTrue(retrieved.isPresent());
		assertEquals("https://example.com", retrieved.get().getLongUrl());
	}

	@Test
	void testRedisCacheEvictionAndLRU() {
		// Cache capacity is 5. Let's insert 5 elements: k1 to k5
		cacheService.put("k1", "url1");
		cacheService.put("k2", "url2");
		cacheService.put("k3", "url3");
		cacheService.put("k4", "url4");
		cacheService.put("k5", "url5");

		assertEquals(5, cacheService.getCacheContents().size());

		// Access "k1" to make it recently used
		Optional<String> val = cacheService.get("k1");
		assertTrue(val.isPresent());

		// Insert 6th element: k6. Eldest entry ("k2" because k1 was recently accessed) should be evicted.
		cacheService.put("k6", "url6");

		assertEquals(5, cacheService.getCacheContents().size());
		assertFalse(cacheService.getCacheContents().containsKey("k2")); // k2 evicted
		assertTrue(cacheService.getCacheContents().containsKey("k1"));  // k1 retained
		assertTrue(cacheService.getCacheContents().containsKey("k6"));  // k6 present
		assertEquals("k2", cacheService.getLastEvictedKey());
	}

	@Test
	void testUrlShortenFlow() {
		String longUrl = "https://google.com";
		UrlMetadata metadata = shortenService.shortenUrl(longUrl, 7);
		
		assertNotNull(metadata);
		assertNotNull(metadata.getShortKey());
		assertEquals(longUrl, metadata.getLongUrl());
		
		// Verify persistence in DB
		Optional<UrlMetadata> dbRecord = databaseService.findById(metadata.getShortKey());
		assertTrue(dbRecord.isPresent());
		assertEquals(longUrl, dbRecord.get().getLongUrl());
	}

	@Test
	void testRedirectFlowCacheMissAndCacheHit() {
		String longUrl = "https://wikipedia.org";
		UrlMetadata metadata = shortenService.shortenUrl(longUrl, 7);
		String shortKey = metadata.getShortKey();

		// Cache is empty, so first redirect should result in a Cache Miss
		Optional<RedirectResult> missResultOpt = redirectService.handleRedirect(shortKey);
		assertTrue(missResultOpt.isPresent());
		
		RedirectResult missResult = missResultOpt.get();
		assertEquals(longUrl, missResult.getLongUrl());
		assertFalse(missResult.isCacheHit()); // Cache Miss
		assertTrue(missResult.getTraceSteps().stream().anyMatch(step -> step.contains("CACHE MISS")));
		
		// Verify it got written back to Cache
		assertTrue(cacheService.getCacheContents().containsKey(shortKey));

		// Second redirect should result in a Cache Hit
		Optional<RedirectResult> hitResultOpt = redirectService.handleRedirect(shortKey);
		assertTrue(hitResultOpt.isPresent());
		
		RedirectResult hitResult = hitResultOpt.get();
		assertEquals(longUrl, hitResult.getLongUrl());
		assertTrue(hitResult.isCacheHit()); // Cache Hit
		assertTrue(hitResult.getTraceSteps().stream().anyMatch(step -> step.contains("CACHE HIT")));
	}

	@Test
	void testRedirectForNonExistentKey() {
		Optional<RedirectResult> result = redirectService.handleRedirect("invalidKey");
		assertFalse(result.isPresent());
	}
}

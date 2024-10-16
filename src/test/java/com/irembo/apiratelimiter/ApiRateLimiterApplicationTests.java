package com.irembo.apiratelimiter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.irembo.apiratelimiter.limiter.ApiRateLimiter;
import com.irembo.apiratelimiter.limiter.LimitConfig;
import com.irembo.apiratelimiter.model.TimeWindow;
import com.irembo.apiratelimiter.repos.KeyValueRepository;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
class ApiRateLimiterApplicationTests {

    private static final String BASE_URI = "http://localhost:";
    private static final String GLOBAL_LIMIT_KEY = "global_limit";
    private static final int GLOBAL_LIMIT = 75;
    private static final String USER_LIMIT_KEY = "user_limit";
    private static final int USER_LIMIT = 10;
    private static final String USER1 = "user1";
    private static final int USER1_LIMIT = 15;
    private static final String USER2 = "user2";
    private static final int USER2_LIMIT = 20;
    private static final String USER3 = "user3";
    private static final int USER3_LIMIT = 25;
    private static final String USER4 = "user4";
    private static final int USER4_LIMIT = 30;
    private static final String USER5 = "user5";
    private static final int USER5_LIMIT = 35;
    private static final String USER6 = "user6";

    private static final int OK = HttpStatus.SC_OK;

    private static final int TEST_SECONDS = 10;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    KeyValueRepository keyValue;

    @Autowired
    private ObjectMapper jsonMapper;

    @LocalServerPort
    private Integer port;

    private String[] nextSeconds;

    private Map<String, AtomicLong> successCounts;
    private Map<String, Integer> requestsPerUser;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        addConfig(GLOBAL_LIMIT_KEY, GLOBAL_LIMIT, 7);
        addConfig(USER_LIMIT_KEY, USER_LIMIT);
        addUserConfig(USER1, USER1_LIMIT);
        addUserConfig(USER2, USER2_LIMIT);
        addUserConfig(USER3, USER3_LIMIT);
        addUserConfig(USER4, USER4_LIMIT);
        addUserConfig(USER5, USER5_LIMIT);

        initNextSeconds();
        successCounts = new HashMap<>();
        requestsPerUser = new HashMap<>();

        wireMockServer.stubFor(get(urlPathEqualTo("/limited")).willReturn(aResponse()));
    }

    private void initSuccessCounts(String key) {
        for (int i = 0; i < TEST_SECONDS; i++) {
            String timedKey = key + ":" + nextSeconds[i];
            successCounts.put(timedKey, new AtomicLong());
        }
    }

    private void initNextSeconds() {
        nextSeconds = new String[TEST_SECONDS];
        Instant now = Instant.now();
        for (int i = 0; i < TEST_SECONDS; i++) {
            nextSeconds[i] = toMMSS(now);
            now = now.plusSeconds(1);
        }
    }

    private String toMMSS(Instant instant) {
        String longFormat = DateTimeFormatter.ISO_INSTANT.format(instant);
        return toMMSS(longFormat);
    }

    private String toMMSS(String longFormat) {
        return longFormat.substring(14, 19);
    }

    private void addConfig(String key, long limit, int counter) throws JsonProcessingException {
        LimitConfig config = new LimitConfig();
        config.setLimit(limit);
        config.setSoftLimit(limit / 2);
        config.setCounters(counter);
        Map<TimeWindow, LimitConfig> configValue = Collections.singletonMap(TimeWindow.SECOND, config);
        keyValue.setValue(key, jsonMapper.writeValueAsString(configValue));
    }

    private void addConfig(String key, long limit) throws JsonProcessingException {
        addConfig(key, limit, 0);
    }

    private void addUserConfig(String user, long limit) throws JsonProcessingException {
        addConfig(USER_LIMIT_KEY + ":" + user, limit);
    }

    private void incrementSuccess(String key, String second) {
        String timedKey = key + ":" + second;
        if (successCounts.containsKey(timedKey)) {
            successCounts.get(timedKey).incrementAndGet();
        }
    }

    private int makeApiCall(String user) throws IOException {
        String url = BASE_URI + port + "/limited?user_id=" + user;
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse result = client.execute(new HttpGet(url));
        int status = result.getStatusLine().getStatusCode();
        if (status == OK) {
            String second = toMMSS(result.getFirstHeader(ApiRateLimiter.FILTER_TIME_HEADER).getValue());
            incrementSuccess(user, second);
        }
        return status;
    }

    private long getTotalSuccesses() {
        return successCounts.values().stream().mapToLong(AtomicLong::get).sum();
    }

    private long getTotalSuccesses(String second) {
        return successCounts.entrySet().stream().filter(entry -> entry.getKey().endsWith(":" + second))
                .mapToLong(entry -> entry.getValue().get()).sum();
    }

    private void addUserRequests(String user, int requests) {
        requestsPerUser.put(user, requests);
        initSuccessCounts(user);
    }

    private void runSimulation(int count) throws InterruptedException {
        for (int step = 0; step < count; step++) {
            requestsPerUser.entrySet().parallelStream().unordered().forEach(userRequests -> {
                final String user = userRequests.getKey();
                int requests = userRequests.getValue();
                IntStream.range(0, requests).parallel().unordered().forEach(i -> {
                    try {
                        makeApiCall(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });
            Thread.sleep(1000);//wait for 1 second time window
        }

    }

    @Test
    void singleRequestShouldBeOk() throws IOException {
        assertThat(makeApiCall("user1")).isEqualTo(OK);
    }

    @Test
    void singleRequestWithoutUserIdShouldBeForbidden() throws IOException {
        assertThat(makeApiCall("")).isEqualTo(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void usersShouldNotPassTheLimitForAnySecond() throws InterruptedException {
        addUserRequests(USER1, USER1_LIMIT * 2);
        addUserRequests(USER2, USER2_LIMIT * 2);
        addUserRequests(USER6, USER_LIMIT * 2);

        runSimulation(3);

        assertUnderLimitEverySecond(USER1, USER1_LIMIT);
        assertUnderLimitEverySecond(USER2, USER2_LIMIT);
        assertUnderLimitEverySecond(USER6, USER_LIMIT); // default limit
    }

    private void assertUnderLimitEverySecond(String user, int limit) {
        for (String second : nextSeconds) {
            String timedKey = user + ":" + second;
            assertThat(successCounts)
                    .hasEntrySatisfying(timedKey, count -> assertThat(count.get()).isLessThanOrEqualTo(limit));
        }
    }

    @Test
    void allUsersRequestsUnderTheLimitShouldBeSuccessful() throws InterruptedException {
        addUserRequests(USER1, USER1_LIMIT);
        addUserRequests(USER2, USER2_LIMIT);
        addUserRequests(USER3, USER3_LIMIT);
        addUserRequests(USER6, USER_LIMIT);

        runSimulation(3);

        //should have a total of (15+20+25+10)*3=210
        assertThat(getTotalSuccesses()).isEqualTo(210);
    }

    @Test
    void usersShouldNotPassTheSystemLimitForAnySecond() throws InterruptedException {
        addUserRequests(USER1, USER1_LIMIT);
        addUserRequests(USER2, USER2_LIMIT);
        addUserRequests(USER3, USER3_LIMIT);
        addUserRequests(USER4, USER4_LIMIT);
        addUserRequests(USER5, USER5_LIMIT);
        addUserRequests(USER6, USER_LIMIT);
        //Total number of requests per second = 15+20+25+30+35+10 = 135 > 100 (global limit)

        runSimulation(3);

        for (String second : nextSeconds) {
            System.out.println(getTotalSuccesses(second));
            assertThat(getTotalSuccesses(second)).isLessThanOrEqualTo(GLOBAL_LIMIT);
        }
    }

}

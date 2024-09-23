package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.AllArgsConstructor;
import redis.CityCountry;

import java.util.List;

@AllArgsConstructor
public class TestRedis {

    private final RedisClient redisClient;
    private final ObjectMapper mapper;

    public void testRedisData(final List<Integer> ids) {
        try (final StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            final RedisStringCommands<String, String> sync = connection.sync();

            for (final Integer id : ids) {
                final String value = sync.get(String.valueOf(id));

                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package ua.edu.viti.military.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            Object value = redisTemplate.opsForValue().get("health:check");
            if ("ok".equals(value)) {
                return Health.up()
                        .withDetail("redis", "Connected")
                        .withDetail("cache", "Available")
                        .build();
            }
            return Health.down()
                    .withDetail("redis", "Unexpected value")
                    .build();
        } catch (Exception e) {
            return Health.down(e).withDetail("redis", "Down").build();
        }
    }
}

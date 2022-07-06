package com.subsel.healthledger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class AuditorAwareConfig implements AuditorAware<UUID>
{
    @Override
    public Optional<UUID> getCurrentAuditor()
    {
        UUID uuid = UUID.randomUUID();
        return Optional.of(uuid);
    }
}

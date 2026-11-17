package com.pecucore.system.core.config;

import com.pecucore.system.core.model.Animal;
import com.pecucore.system.core.repository.AnimalRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = Animal.class)
@EnableJpaRepositories(basePackageClasses = AnimalRepository.class)
public class PersistenciaNucleoConfig {
}

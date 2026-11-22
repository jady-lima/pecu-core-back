package com.pecucore.system;

import com.pecucore.system.bovinos.BovinosConfig;
import com.pecucore.system.equinos.EquinosConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.pecucore.system.core")
@Import({BovinosConfig.class, EquinosConfig.class})
@EnableScheduling
public class SystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemApplication.class, args);
	}

}

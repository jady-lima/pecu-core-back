package com.pecucore.system.bovinos;

import com.pecucore.system.bovinos.model.MetaDesempenho;
import com.pecucore.system.bovinos.repository.MetaDesempenhoRepository;
import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.AplicacaoAgroCore;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AplicacaoAgroCore(Aplicacao.BOVINOS)
@ComponentScan(
        basePackageClasses = BovinosConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class)
)
@EntityScan(basePackageClasses = MetaDesempenho.class)
@EnableJpaRepositories(basePackageClasses = MetaDesempenhoRepository.class)
public class BovinosConfig {
}

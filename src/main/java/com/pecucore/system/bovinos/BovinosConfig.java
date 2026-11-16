package com.pecucore.system.bovinos;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.AplicacaoAgroCore;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@AplicacaoAgroCore(Aplicacao.BOVINOS)
@ComponentScan(
        basePackageClasses = BovinosConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class)
)
public class BovinosConfig {
}

package com.pecucore.system.equinos;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import com.pecucore.system.core.extensao.AplicacaoAgroCore;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/** Aplicação equinos: ligada com agrocore.aplicacao=equinos. Usa só as entidades do núcleo. */
@Configuration
@AplicacaoAgroCore(Aplicacao.EQUINOS)
@ComponentScan(
        basePackageClasses = EquinosConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class)
)
public class EquinosConfig {
}
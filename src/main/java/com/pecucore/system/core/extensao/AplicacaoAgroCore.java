package com.pecucore.system.core.extensao;

import com.pecucore.system.core.config.AgroCoreProperties.Aplicacao;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(CondicaoAplicacaoAgroCore.class)
public @interface AplicacaoAgroCore {

    Aplicacao value();
}

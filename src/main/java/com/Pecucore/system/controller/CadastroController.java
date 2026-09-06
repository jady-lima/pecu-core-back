package com.pecucore.system.controller;

import com.pecucore.system.model.Cadastro;
import com.pecucore.system.model.Animal;
import com.pecucore.system.repository.animalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller

public class CadastroController {

    @Autowired
    private animalRepository animalRepository;

    @PostMapping("/salvarCadastro")
    public void salvarCadastro(Cadastro cadastro){

//TRATAMENTO DE EXECEÇÕES DO CADASTRO INICIAL

        if(cadastro.getQuantidade() <=0){
           System.out.println("ERRO: A quantidade deve ser maior do que zero.") ;
           return;
        }

        if (cadastro.getSexo() == null || cadastro.getSexo().isBlank()) {
            System.out.println("ERRO: O sexo deve ser informado.");
            return;
        }

      for(int i=0; i< cadastro.getQuantidade(); i++){
          Animal  animal = new Animal();
          animal.setBrinco(cadastro.getBrincoInicial() + i);
          animal.setLote(cadastro.getLote());
          animal.setSexo(cadastro.getSexo());
          animalRepository.save(animal);

      }



    }
}


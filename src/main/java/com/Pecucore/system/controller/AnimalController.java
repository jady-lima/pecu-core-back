package com.pecucore.system.controller;

import com.pecucore.system.model.Animal;
import com.pecucore.system.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AnimalController {

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping("/cadastroAnimal")
    public ModelAndView cadastrar(Animal animal){
        ModelAndView mv = new ModelAndView("/gerenciadorDeEntidades/animal");
        mv.addObject("animal", animal);
        return mv;
    }

    @PostMapping("/salvarAnimal")
    public ModelAndView salvar(Animal animal, BindingResult result){
        if(result.hasErrors()){
            return cadastrar(animal);
        }
        animalRepository.saveAndFlush(animal);
        return cadastrar(new Animal());
    }



}

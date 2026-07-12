package com.dominiossolunet.service;

import com.dominiossolunet.repository.DominioRepository;
import lombok.Value;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
public class RenovacionService {
    private final DominioRepository dominioRepository;

    //Constructor
    public RenovacionService(DominioRepository dominioRepository){
        this.dominioRepository = dominioRepository;
    }

    public void enviarAvisosRenovacion(){
        private
    }

}

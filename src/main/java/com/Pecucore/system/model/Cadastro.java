package com.pecucore.system.model;

public class Cadastro {

private int quantidade;
private int brincoInicial;
private int lote;
private String sexo;

public int getQuantidade(){return quantidade;}

public int getBrincoInicial(){return brincoInicial;}

public int getLote(){return lote;}

public String getSexo(){return sexo;}

public void setQuantidade(int quantidade) {
    this.quantidade = quantidade;
}

public void setBrincoInicial(int brincoInicial) {
    this.brincoInicial = brincoInicial;
}

public void setLote(int lote) {
    this.lote = lote;
}

public void setSexo(String sexo) {
    this.sexo = sexo;
}

}



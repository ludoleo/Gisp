package com.example.myapplication.classi;

import java.util.Date;
import java.util.Objects;

public class Inquilino {

    private String studente;
    private String casa;
    private Date dataInizio;
    private Date dataFine;

    public Inquilino(String studente, String casa, Date dataInizio, Date dataFine) {
        this.studente = studente;
        this.casa = casa;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public String getCasa() {
        return casa;}

    public void setCasa(String casa) {
        this.casa = casa;}

    public String getStudente() {
        return studente;
    }

    public void setStudente(String studente) {
        this.studente = studente;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(Date dataInizio) {
        this.dataInizio = dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public void setDataFine(Date dataFine) {
        this.dataFine = dataFine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inquilino)) return false;
        Inquilino inquilino = (Inquilino) o;
        return getStudente().equals(inquilino.getStudente());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStudente());
    }
}

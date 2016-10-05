package br.com.ftth.gerenciaftth;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.zzf;

/**
 * Created by tiago on 11/09/2016.
 */
public class Marcador
{
    private int id = 0;
    private String tipo = "";
    private String setor = "";
    private String alimentacao = "";
    private String grupo = "";
    private String caixa = "";
    private String info = "";
    private String latitude = "";
    private String longitude = "";
    private String chaves = "";
    private String atualizado = "";




    public Marcador() {

    }


    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCaixa() {
        return caixa;
    }

    public void setCaixa(String caixa) {
        this.caixa = caixa;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getChaves() {
        return chaves;
    }

    public void setChaves(String chaves) {
        this.chaves = chaves;
    }

    public int getId() {  return id;  }

    public void setId(int id) {   this.id = id;  }

    public String getAlimentacao() {
        return alimentacao;
    }

    public void setAlimentacao(String alimentacao) {
        this.alimentacao = alimentacao;
    }

    public String getAtualizado() {
        return atualizado;
    }

    public void setAtualizado(String atualizado) {
        this.atualizado = atualizado;
    }
}

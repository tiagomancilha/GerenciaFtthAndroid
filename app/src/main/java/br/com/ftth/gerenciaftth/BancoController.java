package br.com.ftth.gerenciaftth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BancoController {

    private SQLiteDatabase db;
    private CriaBanco banco;
    private int quantidadeDeMarcadores = 0;
    public BancoController(Context context){
        banco = new CriaBanco(context);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public String gravarMarcador(Marcador marc){
        ContentValues valores;
        long resultado;
        db = banco.getWritableDatabase();
        valores = new ContentValues();
        valores.put(CriaBanco.TIPO,marc.getTipo());
        valores.put(CriaBanco.SETOR,marc.getSetor());
        valores.put(CriaBanco.ALIMENTACAO,marc.getAlimentacao());
        valores.put(CriaBanco.GRUPO,marc.getGrupo());
        valores.put(CriaBanco.CAIXA,marc.getCaixa());
        valores.put(CriaBanco.LATITUDE,marc.getLatitude());
        valores.put(CriaBanco.LONGITUDE,marc.getLongitude());
        valores.put(CriaBanco.INFO,marc.getInfo());
        valores.put(CriaBanco.ATUALIZADO,"1");
        resultado = db.insert(CriaBanco.TABELA_MARCADORES, null, valores);
        db.close();
        if (resultado ==-1) return "Erro ao inserir o marcador";
        else return "Marcador Inserido com sucesso";
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public void alterarMarcador(Marcador marc){
        ContentValues valores;
        db = banco.getWritableDatabase();
        String onde = CriaBanco._ID +  "=" + marc.getId();
        valores = new ContentValues();
        valores.put(CriaBanco.TIPO,marc.getTipo());
        valores.put(CriaBanco.SETOR,marc.getSetor());
        valores.put(CriaBanco.ALIMENTACAO,marc.getAlimentacao());
        valores.put(CriaBanco.GRUPO,marc.getGrupo());
        valores.put(CriaBanco.CAIXA,marc.getCaixa());
        valores.put(CriaBanco.LATITUDE,marc.getLatitude());
        valores.put(CriaBanco.LONGITUDE,marc.getLongitude());
        valores.put(CriaBanco.INFO,marc.getInfo());
        valores.put(CriaBanco.ATUALIZADO,"0");
        db.update(CriaBanco.TABELA_MARCADORES, valores,onde,null);
        db.close();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void excluirMarcador(Marcador marc){
        ContentValues valores;
        db = banco.getReadableDatabase();
        String onde = CriaBanco._ID +  "=" + marc.getId();

        db.delete(CriaBanco.TABELA_MARCADORES,onde,null);
        db.close();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    public Cursor carregarMarcadores(){
        Cursor cursor;
      //  String[] campos =  {banco.ID,banco.TITULO};
        db = banco.getReadableDatabase();
        cursor = db.query(banco.TABELA_MARCADORES, null, null, null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        db.close();
        quantidadeDeMarcadores = cursor.getCount();
        return cursor;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    public int getQuantidadeDeMarcadores(){
        return quantidadeDeMarcadores;
    }

    public void limparTabela(String nome){
        db = banco.getWritableDatabase();
        db.execSQL("delete * from " + nome);
    }
}
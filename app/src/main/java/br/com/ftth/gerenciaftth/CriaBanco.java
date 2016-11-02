package br.com.ftth.gerenciaftth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tiago on 12/09/2016.
 */
public class CriaBanco extends SQLiteOpenHelper {

    public static final int VERSAO_BD = 6;
    public static final String NOME_BANCO = "gerenciaftth";
    public static final String TABELA_MARCADORES = "marcadores";
    public static final String _ID = "id";
    public static final String TIPO = "tipo";
    public static final String SETOR = "setor";
    public static final String ALIMENTACAO = "alimentacao";
    public static final String GRUPO = "grupo";
    public static final String CAIXA = "caixa";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String INFO = "info";
    public static final String ATUALIZADO = "atualizado";

    public static final String HTTP_SET_MARKER = "http://www.gerenciaftth.tk/php/setMarker.php";
    public static final String HTTP_DELETE_MARKER = "http://www.gerenciaftth.tk/php/deleteMarker.php";
    public static final String HTTP_UPDATE_MARKER = "http://www.gerenciaftth.tk/php/updateMarker.php";
    public static final String HTTP_UPDATE_COORDENADAS_MARKER = "http://www.gerenciaftth.tk/php/setCoordenadasMarker.php";
    public static final String HTTP_GET_MARKERS = "http://www.gerenciaftth.tk/php/getMarkers.php";

    public CriaBanco(Context c){
        super(c,NOME_BANCO,null,VERSAO_BD);
    }
    public CriaBanco(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+TABELA_MARCADORES+"("
                + _ID       + " integer primary key autoincrement,"
                + TIPO      + " text,"
                + SETOR     + " text,"
                + ALIMENTACAO     + " text,"
                + GRUPO     + " text,"
                + CAIXA     + " text,"
                + LATITUDE  + " text,"
                + LONGITUDE + " text,"
                + INFO      + " text,"
                + ATUALIZADO+ " text"
                +")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE " + TABELA_MARCADORES);
        onCreate(db);
    }
}

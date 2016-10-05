package br.com.ftth.gerenciaftth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class VerificarCaixaNapActivity extends AppCompatActivity {

    private int id = 0;
    private ListView listaDePortas;
    private EditText quantidadePortas;
    private static final int MAXIMO_PORTAS = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verificar_caixa_nap);
        id = getIntent().getExtras().getInt("id");
        listaDePortas = (ListView)findViewById(R.id.listaDePortas);
        quantidadePortas = (EditText)findViewById(R.id.quantidadePortas);
        String[] values = new String[] {
                "1-Geraldo dos Santos",
                "2-Luis da Silva",
                "3-Aureliano",
                "4-Tiago Mancilha",
                "5-Maria dos santos",
                "6-Joana Machado",
                "7-Geraldo magela",
                "8-Geraldo dos Reis"
        };
        quantidadePortas.setText("" + values.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listaDePortas.setAdapter(adapter);
    }
}

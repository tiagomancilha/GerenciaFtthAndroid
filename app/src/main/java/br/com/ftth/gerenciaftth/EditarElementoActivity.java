package br.com.ftth.gerenciaftth;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditarElementoActivity extends Activity {

     int id = 0;
    String tipo, setor,alimentacao,grupo,caixa,info,status;
    Spinner tipoElemento;
    TextView tvCaixaElemento;
    EditText setorElemento,alimentacaoElemento,grupoElemento,caixaelemento,infoElemento;
    Button voltar,alterar;
    ArrayList<String> tiposDeElementos = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_elemento);

        tipo = getIntent().getExtras().getString("tipo");
        setor = getIntent().getExtras().getString("setor");
        alimentacao = getIntent().getExtras().getString("alimentacao");
        grupo = getIntent().getExtras().getString("grupo");
        caixa = getIntent().getExtras().getString("caixa");
        info = getIntent().getExtras().getString("info");
        status = getIntent().getExtras().getString("status");
        tipoElemento = (Spinner)findViewById(R.id.editar_tipo_elemento);
        setorElemento = (EditText)findViewById(R.id.editar_numero_setor);
        alimentacaoElemento = (EditText)findViewById(R.id.editar_alimentacao);
        grupoElemento = (EditText)findViewById(R.id.editar_numero_grupo);
        caixaelemento = (EditText)findViewById(R.id.editar_numero_caixa);
        tvCaixaElemento = (TextView) findViewById(R.id.tvCaixaElemento);
        infoElemento = (EditText)findViewById(R.id.editar_informacoes);
        voltar = (Button)findViewById(R.id.editar_voltar);
        alterar = (Button)findViewById(R.id.editar_alterar);
        tiposDeElementos.add("NAP");
        tiposDeElementos.add("BACKBONE");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposDeElementos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoElemento.setAdapter(adapter);
        tipoElemento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tip = tipoElemento.getItemAtPosition(position).toString();
                if(!tip.equals("BACKBONE")){
                    tvCaixaElemento.setVisibility(View.VISIBLE);
                    caixaelemento.setVisibility(View.VISIBLE);
                }else{
                    tvCaixaElemento.setVisibility(View.INVISIBLE);
                    caixaelemento.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        for(int d = 0; d < tiposDeElementos.size(); d++){
            if(tiposDeElementos.get(d).toString().equals(tipo)){
                tipoElemento.setSelection(d);
                break;
            }
        }
        setorElemento.setText(setor);
        alimentacaoElemento.setText(alimentacao);
        grupoElemento.setText(grupo);
        caixaelemento.setText(caixa);
        infoElemento.setText(info);

        alterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int _id = getIntent().getExtras().getInt("id");
                final int index = getIntent().getExtras().getInt("index");
                final String tipoElem =  tipoElemento.getSelectedItem().toString();
                final String alimentacaoElem =  alimentacaoElemento.getText().toString();
                final String setorElem       =  setorElemento.getText().toString();
                final String grupoElem =  grupoElemento.getText().toString();
                final String caixaElem = caixaelemento.getText().toString();
                final String informacoesElem = infoElemento.getText().toString();
                if(status.equals("LOC")){
                    Intent it = new Intent();
                    it.putExtra("id",_id);
                    it.putExtra("info",informacoesElem);
                    it.putExtra("tipo",tipoElem);
                    it.putExtra("setor",setorElem);
                    it.putExtra("alimentacao",alimentacaoElem);
                    it.putExtra("grupo",grupoElem);
                    it.putExtra("caixa",caixaElem);
                    it.putExtra("index",index);
                    setResult(Constantes.RESULT_EDITAR_NO_BANCO,it);
                    finish();
                }
                final Intent it = new Intent();
                new Thread(new Runnable()
                {
                    public void run() {
                        try{
                            String address = CriaBanco.HTTP_UPDATE_MARKER;
                            HttpClient client = new DefaultHttpClient();
                            HttpPost post = new HttpPost(address);
                            List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                            pairs.add(new BasicNameValuePair("id", "" + _id));
                            pairs.add(new BasicNameValuePair("tipo", tipoElem));
                            pairs.add(new BasicNameValuePair("alimentacao", alimentacaoElem));
                            pairs.add(new BasicNameValuePair("setor", setorElem.toUpperCase()));
                            pairs.add(new BasicNameValuePair("grupo", grupoElem));
                            pairs.add(new BasicNameValuePair("caixa", caixaElem));
                            pairs.add(new BasicNameValuePair("info", informacoesElem));
                            String chaves = "";
                            if(tipoElemento.equals("BACKBONE")){
                                chaves =  setorElem +  " SD" + grupoElem;
                            }
                            else if(tipoElemento.equals("NAP")){
                                chaves =  setorElem +  " SD" + grupoElem + " SA" + caixaElem;
                            }
                            else{
                                chaves = tipoElem;
                            }
                            pairs.add(new BasicNameValuePair("chaves", chaves));
                            post.setEntity(new UrlEncodedFormEntity(pairs));
                            HttpResponse response = client.execute(post);
                            String responseText = null;
                            try {
                                responseText = EntityUtils.toString(response.getEntity());
                                final JSONObject json = new JSONObject(responseText);
                                if(json.getBoolean("status") == true){
                                    it.putExtra("id",_id);
                                    it.putExtra("info",informacoesElem);
                                    it.putExtra("tipo",tipoElem);
                                    it.putExtra("setor",setorElem);
                                    it.putExtra("alimentacao",alimentacaoElem);
                                    it.putExtra("grupo",grupoElem);
                                    it.putExtra("caixa",caixaElem);
                                    it.putExtra("index",index);


                                    setResult(Activity.RESULT_OK, it);
                                }
                                else{
                                    setResult(Activity.RESULT_CANCELED, it);
                                }
                            }catch (ParseException e) {
                                it.putExtra("EXCECAO",e.toString());
                                setResult(Activity.RESULT_CANCELED, it);
                            }
                        }
                        catch(ClientProtocolException cpe){
                            it.putExtra("EXCECAO",cpe.toString());
                            setResult(Activity.RESULT_CANCELED,it);

                        }
                        catch (IOException ioe){
                            it.putExtra("EXCECAO",ioe.toString());
                            setResult(Activity.RESULT_CANCELED, it);
                        }
                        catch(Exception erro){
                            it.putExtra("EXCECAO",erro.toString());
                            setResult(Activity.RESULT_CANCELED, it);
                        }
                        finish();
                    }
                }).start();
            }
        });voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

package br.com.ftth.gerenciaftth;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

public class AddElementoActivity extends AppCompatActivity {

    Spinner tipo;
    EditText setor, grupo, caixa, informacoes, alimentacao;
    TextView tvCaixa;
    Button cancelar, gravar;

    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_elemento);
        extras = getIntent().getExtras();
        tipo = (Spinner)findViewById(R.id.spinner_tipo_elemento);
        alimentacao = (EditText)findViewById(R.id.edittext_alimentacao);
        setor = (EditText)findViewById(R.id.edittext_numero_setor);
        grupo = (EditText)findViewById(R.id.edittext_numero_grupo);
        tvCaixa = (TextView)findViewById(R.id.tvCaixa);
        caixa = (EditText)findViewById(R.id.edittext_numero_caixa);
        informacoes = (EditText)findViewById(R.id.edittext_informacoes);
        ArrayList<String> tipos= new ArrayList<String>();
        tipos.add("NAP");
        tipos.add("BACKBONE");
     //   tipos.add("POSTE");
        ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipo.setAdapter(adapter1);
        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tip = tipo.getItemAtPosition(position).toString();
                if(!tip.equals("BACKBONE")){
                    tvCaixa.setVisibility(View.VISIBLE);
                    caixa.setVisibility(View.VISIBLE);
                }else{
                    tvCaixa.setVisibility(View.INVISIBLE);
                    caixa.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelar = (Button)findViewById(R.id.button_cancelar);
        gravar = (Button)findViewById(R.id.button_gravar);

        cancelar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        gravar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    final String ONDE_GRAVAR = "" + extras.getString("ONDE_GRAVAR");
                    final String tipoElemento = "" +  tipo.getSelectedItem().toString();
                    final String alimentacaoElemento = "" +   alimentacao.getText().toString();
                    final String setorElemento = "" +   setor.getText().toString();
                    final String grupoElemento = "" +    grupo.getText().toString();
                    final String caixaElemento = "" +  caixa.getText().toString();
                    final String latitude = "" + extras.getDouble("latitude");
                    final String longitude = "" + extras.getDouble("longitude");
                    final String informacoesElemento = "" +  informacoes.getText().toString();
                    Log.e("POST", tipoElemento + " " + setorElemento  + " " + grupoElemento   + " " + latitude);
                    if(ONDE_GRAVAR.equals("LOC")){
                        Intent it = new Intent();
                        it.putExtra("info",informacoesElemento);
                        it.putExtra("latitude",Double.parseDouble(latitude));
                        it.putExtra("longitude",Double.parseDouble(longitude));
                        it.putExtra("tipo",tipoElemento);
                        it.putExtra("setor",setorElemento);
                        it.putExtra("alimentacao",alimentacaoElemento);
                        it.putExtra("grupo",grupoElemento);
                        it.putExtra("caixa",caixaElemento);
                        setResult(Constantes.RESULT_GRAVAR_NO_BANCO,it);
                        finish();
                    }

                    final Intent it = new Intent();
                    new Thread(new Runnable()
                    {
                        public void run() {
                            try{
                                String address = "http://www.gerenciaftth.tk/php/setMarker.php";
                                HttpClient client = new DefaultHttpClient();
                                HttpPost post = new HttpPost(address);
                                List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                                pairs.add(new BasicNameValuePair("tipo", tipoElemento));
                                pairs.add(new BasicNameValuePair("alimentacao", alimentacaoElemento));
                                pairs.add(new BasicNameValuePair("setor", setorElemento.toUpperCase()));
                                pairs.add(new BasicNameValuePair("grupo", grupoElemento));
                                pairs.add(new BasicNameValuePair("caixa", caixaElemento));
                                pairs.add(new BasicNameValuePair("latitude", latitude));
                                pairs.add(new BasicNameValuePair("longitude", longitude));
                                pairs.add(new BasicNameValuePair("info", informacoesElemento));
                                String chaves = "";
                                if(tipoElemento.equals("BACKBONE")){
                                    chaves =  setorElemento +  " SD" + grupoElemento;
                                }
                                else if(tipoElemento.equals("NAP")){
                                    chaves =  setorElemento +  " SD" + grupoElemento + " SA" + caixaElemento;
                                }
                                else{
                                    chaves = tipoElemento;
                                }
                                pairs.add(new BasicNameValuePair("chaves", chaves));
                                post.setEntity(new UrlEncodedFormEntity(pairs));
                                HttpResponse response = client.execute(post);
                                String responseText = null;
                                try {
                                    responseText = EntityUtils.toString(response.getEntity());
                                    final JSONObject json = new JSONObject(responseText);
                                    if(json.getBoolean("status") == true){
                                        it.putExtra("id",json.getInt("id"));
                                        it.putExtra("info",informacoesElemento);
                                        it.putExtra("latitude",Double.parseDouble(latitude));
                                        it.putExtra("longitude",Double.parseDouble(longitude));
                                        it.putExtra("tipo",tipoElemento);
                                        it.putExtra("setor",setorElemento);
                                        it.putExtra("alimentacao",alimentacaoElemento);
                                        it.putExtra("grupo",grupoElemento);
                                        it.putExtra("caixa",caixaElemento);


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
                                setResult(Constantes.RESULT_GRAVAR_NO_BANCO, it);
                            }
                            catch(Exception erro){
                                it.putExtra("EXCECAO",erro.toString());
                                setResult(Activity.RESULT_CANCELED, it);
                            }
                            it.putExtra("info",informacoesElemento);
                            it.putExtra("latitude",Double.parseDouble(latitude));
                            it.putExtra("longitude",Double.parseDouble(longitude));
                            it.putExtra("tipo",tipoElemento);
                            it.putExtra("setor",setorElemento);
                            it.putExtra("alimentacao",alimentacaoElemento);
                            it.putExtra("grupo",grupoElemento);
                            it.putExtra("caixa",caixaElemento);
                            finish();
                        }
                    }).start();

            }
        });
    }
}


package br.com.ftth.gerenciaftth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class MapasActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {


    private SupportMapFragment mapFrag;
    private GoogleMap mapa = null;
    private AlertDialog alerta;
    private boolean jaCarregouAActivity = false;
    private Marker posicaoAtual = null, minhaPosicao = null, marcadorSelecionado = null;
    private AutoCompleteTextView campoDePesquisa;
    FloatingActionButton editarElemento, excluirElemento, adicionarElemento;
    private ArrayList<Marcador> tabelaDeMarcadores;
    private List<Marker> marcadores;
    private LocationManager locationManager;
    private boolean allowNetwork;
    private MinhaLocalizacao myLocation;
    public  String[] arrayPalavrasChave;
    Handler manipulador = new Handler();
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (jaCarregouAActivity == false) {
            setContentView(R.layout.activity_mapas);
            editarElemento = (FloatingActionButton) findViewById(R.id.editarElemento);
            excluirElemento = (FloatingActionButton) findViewById(R.id.excluirElemento);
            adicionarElemento = (FloatingActionButton) findViewById(R.id.btAdicionarElementoNoMapa);
          /*  campoDePesquisa = (AutoCompleteTextView)findViewById(R.id.campoDePesquisa);

            campoDePesquisa.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(campoDePesquisa.getText().toString().trim().equals("")){return false;}
                //    if(campoDePesquisa.getTextSize() > 4){
                        Toast.makeText(MapasActivity.this," " + keyCode,Toast.LENGTH_LONG).show();
                        atualizaSugestoes(getSugestoes(campoDePesquisa.getText().toString()));
                 //   }
                    return false;
                }
            });
            campoDePesquisa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(MapasActivity.this,arrayPalavrasChave[position],Toast.LENGTH_LONG).show();
                }
            }); */
            if (mapa == null) {
                mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFrag.getMapAsync(this);
            }

            marcadores = new ArrayList<Marker>();
            editarElemento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int f = 0; f < tabelaDeMarcadores.size(); f++) {
                        if (marcadores.get(f).equals(marcadorSelecionado)) {
                            Marcador m = tabelaDeMarcadores.get(f);
                            Marker mark = marcadores.get(f);
                            Intent it = new Intent(MapasActivity.this, EditarElementoActivity.class);
                            it.putExtra("id", m.getId());
                            it.putExtra("tipo", m.getTipo());
                            it.putExtra("setor", m.getSetor());
                            it.putExtra("alimentacao", m.getAlimentacao());
                            it.putExtra("grupo", m.getGrupo());
                            it.putExtra("caixa", m.getCaixa());
                            it.putExtra("info", m.getInfo());
                            it.putExtra("index", f);
                            startActivityForResult(it, Constantes.EDITAR_ELEMENTO);
                            break;
                        }
                    }
                }
            });
            excluirElemento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = 0;
                    int index = 0;
                    for (int f = 0; f < tabelaDeMarcadores.size(); f++) {
                        if (marcadores.get(f).equals(marcadorSelecionado)) {
                            Marcador m = tabelaDeMarcadores.get(f);
                            id = m.getId();
                            index = f;
                            break;
                        }
                    }
                    BancoController bc = new BancoController(getBaseContext());
                    bc.excluirMarcador(tabelaDeMarcadores.get(index));
                    final int _id = id;
                    final int _index = index;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    new Thread(new Runnable() {
                                        public void run() {
                                            try {
                                                String address = "http://www.gerenciaftth.tk/php/deleteMarker.php";
                                                HttpClient client = new DefaultHttpClient();
                                                HttpPost post = new HttpPost(address);
                                                List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                                                pairs.add(new BasicNameValuePair("id", "" + _id));
                                                post.setEntity(new UrlEncodedFormEntity(pairs));
                                                HttpResponse response = client.execute(post);
                                                String responseText = null;
                                                try {
                                                    responseText = EntityUtils.toString(response.getEntity());
                                                    final JSONObject json = new JSONObject(responseText);
                                                    if (json.getBoolean("status") == true) {
                                                        manipulador.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                marcadores.get(_index).remove();
                                                                marcadores.remove(_index);
                                                                tabelaDeMarcadores.remove(_index);
                                                                Toast.makeText(MapasActivity.this, "Marcador excluido com sucesso", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                      //  marcadorSelecionado = null;
                                                    } else {
                                                        Toast.makeText(MapasActivity.this, "Ocorreu um erro ao excluir o elemento", Toast.LENGTH_LONG).show();
                                                    }
                                                } catch (ParseException e) {
                                                    Toast.makeText(MapasActivity.this, "Ocorreu um erro ao excluir o elemento." + e.toString(), Toast.LENGTH_LONG).show();
                                                }
                                            } catch (ClientProtocolException cpe) {
                                                Toast.makeText(MapasActivity.this, "Ocorreu um erro ao excluir o elemento." + cpe.toString(), Toast.LENGTH_LONG).show();
                                            } catch (IOException ioe) {
                                                Toast.makeText(MapasActivity.this, "Ocorreu um erro ao excluir o elemento." + ioe.toString(), Toast.LENGTH_LONG).show();
                                            } catch (Exception erro) {
                                                Toast.makeText(MapasActivity.this, "Ocorreu um erro ao excluir o elemento." + erro.toString(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).start();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:

                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapasActivity.this);
                    builder.setTitle("Confirmação de exclusão");
                    builder.setMessage("Deseja realmente excluir o elemento?").setPositiveButton("Sim", dialogClickListener)
                            .setNegativeButton("Não", dialogClickListener);
                    AlertDialog alerta = builder.create();
                    alerta.show();
                }
            });
            adicionarElemento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (marcadorSelecionado != null) {
                        mostraOpcoesDeMarcadores(marcadorSelecionado.getPosition());
                    } else {
                        mostraOpcoesDeMarcadores(posicaoAtual.getPosition());
                    }
                }
            });

            mostraMenuLateral(false, false, false);
            jaCarregouAActivity = true;
        }//fim do if
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMapOptions options = new GoogleMapOptions();
        options.zOrderOnTop(true);
        mapa = googleMap;
        mapa.setMyLocationEnabled(true);
        options.compassEnabled(true);
        //mapa.getUiSettings().setMyLocationButtonEnabled(true);
        //mapa.getUiSettings().setCompassEnabled(true);
        carregarMarcadores();
        mapa.setOnMapClickListener(this);
        mapa.setOnMarkerClickListener(this);
        mapa.setOnMapLongClickListener(this);

        LatLng saoLourenco = new LatLng(-22.121, -45.051);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(saoLourenco).zoom(14).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mapa.animateCamera(update);

        myLocation = new MinhaLocalizacao();
        mapa.setLocationSource(myLocation);
        myLocation.setLocation(saoLourenco);

    }

    public void setaLocalizacao(LatLng ll) {

    }

    @Override
    public boolean onMyLocationButtonClick() {

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Mapas Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://br.com.ftth.gerenciaftth/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Mapas Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://br.com.ftth.gerenciaftth/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public class MinhaLocalizacao implements LocationSource {
        private OnLocationChangedListener listener;

        @Override
        public void activate(OnLocationChangedListener listener) {
            this.listener = listener;
            Log.i("Script", "activate()");
        }

        @Override
        public void deactivate() {
            Log.i("Script", "deactivate()");
        }


        public void setLocation(LatLng latLng) {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            if (listener != null) {
                listener.onLocationChanged(location);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        allowNetwork = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(it);
        } else {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (posicaoAtual != null) {
            posicaoAtual.remove();
        }
        posicaoAtual = mapa.addMarker(new MarkerOptions().position(latLng).title("Local").snippet("" + latLng.toString()));
        marcadorSelecionado = null;
        mostraMenuLateral(false, false, true);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mostraOpcoesDeMarcadores(latLng);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marcadorSelecionado = marker;
        if (marcadorSelecionado.equals(posicaoAtual)) {
            mostraMenuLateral(false, false, true);
        } else {
            mostraMenuLateral(true, true, false);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.e("DRAG", "Comecou a arrastar");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.e("DRAG", "Arrastando...");
    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String address = "http://www.gerenciaftth.tk/php/setCoordenadasMarker.php";
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(address);
                    List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                    pairs.add(new BasicNameValuePair("id", "" + marker.getSnippet()));
                    pairs.add(new BasicNameValuePair("latitude", "" + marker.getPosition().latitude));
                    pairs.add(new BasicNameValuePair("longitude", "" + marker.getPosition().longitude));
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(post);
                    String responseText = "";
                    Log.e("UPDATE", responseText);
                    responseText = EntityUtils.toString(response.getEntity());
                    final JSONObject json = new JSONObject(responseText);
                    if (json.getBoolean("status") == true) {
                        Log.e("Marcador", "Marcador " + marker.getSnippet() + " alterado com sucesso");
                    }
                } catch (ParseException e) {
                    Log.i("Parse Exception", e + "");
                } catch (ClientProtocolException cpe) {
                    Log.e("ERRO_CPE", cpe.toString());
                } catch (IOException ioe) {
                    Log.e("ERRO_IO", ioe.toString());
                } catch (Exception erro) {
                    // Log.e("ERRO",erro.toString());
                    Log.e("ERRO_", erro.getMessage());
                }
            }
        }).start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constantes.ADICIONAR_ELEMENTO) {
            if (resultCode == Activity.RESULT_OK) {
                String lat = "" + data.getDoubleExtra("latitude", 23.345);
                String longi = "" + data.getDoubleExtra("longitude", 23.345);
                LatLng posi = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                String setor = data.getStringExtra("setor");
                String alimentacao = data.getStringExtra("alimentacao");
                String grupo = data.getStringExtra("grupo");
                String caixa = data.getStringExtra("caixa");
                String info = data.getStringExtra("info");
                int _id = data.getIntExtra("id", 13);
                String tipo = data.getStringExtra("tipo");
                MarkerOptions opcoes = new MarkerOptions().position(posi).snippet("" + info);
                Marcador m = new Marcador();
                m.setId(_id);
                m.setTipo(tipo);
                m.setSetor(setor);
                m.setAlimentacao(alimentacao);
                m.setGrupo(grupo);
                m.setCaixa(caixa);
                m.setInfo(info);
                m.setLatitude(lat);
                m.setLongitude(longi);
                m.setAtualizado("1");
                BancoController bc = new BancoController(getBaseContext());
                bc.gravarMarcador(m);
                if (tipo.equals("BACKBONE")) {
                    opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.backbone));
                    opcoes.title(setor + " SD" + grupo);
                } else if (tipo.equals("NAP")) {
                    opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.nap));
                    opcoes.title(setor + " SD" + grupo +  " SA" + caixa);
                } else if (tipo.equals("POSTE")) {
                    opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                    opcoes.title("Poste");
                }
                Marker marcador = mapa.addMarker(opcoes);
                //  marcador.setDraggable(true);
                marcadores.add(marcador);
                tabelaDeMarcadores.add(m);

                Toast.makeText(getApplicationContext(), "Marcador adicionado com sucesso em " + posi.latitude + "," + posi.longitude, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Ocorreu um erro ao adicionar o marcador", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == Constantes.EDITAR_ELEMENTO) {
            if (resultCode == Activity.RESULT_OK) {
                String setor = data.getStringExtra("setor");
                int index = data.getIntExtra("index", 0);
                String alimentacao = data.getStringExtra("alimentacao");
                String grupo = data.getStringExtra("grupo");
                String caixa = data.getStringExtra("caixa");
                String info = data.getStringExtra("info");
                int _id = data.getIntExtra("id", 13);
                String tipo = data.getStringExtra("tipo");

                Marcador m = tabelaDeMarcadores.get(index);
                m.setTipo(tipo);
                m.setSetor(setor);
                m.setAlimentacao(alimentacao);
                m.setGrupo(grupo);
                m.setCaixa(caixa);
                m.setInfo(info);
                tabelaDeMarcadores.set(index, m);
                BancoController bc = new BancoController(getBaseContext());
                bc.alterarMarcador(m);
                final Marker ma = marcadores.get(index);
                if (tipo.equals("BACKBONE")) {
                    ma.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.backbone));
                    ma.setTitle(setor + " SD" + grupo);
                } else if (tipo.equals("NAP")) {
                    ma.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.nap));
                    ma.setTitle(setor + " SD" + grupo + " SA" + caixa);
                } else if (tipo.equals("POSTE")) {
                    ma.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                    ma.setTitle("Poste");
                }
                ma.setSnippet(info);
                marcadores.set(index, ma);
            }
        }
    } ///////////////////////////////// FIM DO ACTIVITY RESULT //////////////////////////////////

    private void mostraOpcoesDeMarcadores(final LatLng coordenadas) {
        Bundle params = new Bundle();
        params.putDouble("latitude", coordenadas.latitude);
        params.putDouble("longitude", coordenadas.longitude);
        Intent intencao;
        intencao = new Intent(MapasActivity.this, AddElementoActivity.class);
        intencao.putExtras(params);
        startActivityForResult(intencao, Constantes.ADICIONAR_ELEMENTO);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private void mostraMenuLateral(boolean editar, boolean excluir, boolean adicionar) {
        if (editar == true) {
            editarElemento.setVisibility(View.VISIBLE);
        } else {
            editarElemento.setVisibility(View.GONE);
        }
        if (excluir == true) {
            excluirElemento.setVisibility(View.VISIBLE);
        } else {
            excluirElemento.setVisibility(View.GONE);
        }
        if (adicionar == true) {
            adicionarElemento.setVisibility(View.VISIBLE);
        } else {
            adicionarElemento.setVisibility(View.GONE);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private void carregarMarcadores() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String address = "http://www.gerenciaftth.tk/php/getMarkers.php";
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(address);
                    List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    HttpResponse response = client.execute(post);
                    String responseText = "";
                    responseText = EntityUtils.toString(response.getEntity());
                    final JSONObject json = new JSONObject(responseText);
                    final JSONArray array = json.getJSONArray("markers");
                    tabelaDeMarcadores = new ArrayList<Marcador>();
                    //BancoController bc = new BancoController(getBaseContext());

                    for (int i = 0; i < array.length(); i++) {
                        Marcador marcador = new Marcador();
                        JSONObject e = array.getJSONObject(i);
                        int id = e.getInt("id");
                        String tipo = e.getString("tipo");
                        String setor = e.getString("setor").toUpperCase();
                        String alimentacao = e.getString("alimentacao");
                        String grupo = e.getString("grupo");
                        String latitude = e.getString("latitude");
                        String longitude = e.getString("longitude");
                        String info = e.getString("informacoes");
                        String caixa = "";
                        if (tipo.equals("NAP")) {  caixa = e.getString("caixa"); }
                        marcador.setId(id);
                        marcador.setTipo(tipo);
                        marcador.setSetor(setor);
                        marcador.setAlimentacao(alimentacao);
                        marcador.setGrupo(grupo);
                        marcador.setCaixa(caixa);
                        marcador.setLatitude(latitude);
                        marcador.setLongitude(longitude);
                        marcador.setInfo(info);
                        String palavrasChave = "";
                        LatLng posi = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        final MarkerOptions opcoes = new MarkerOptions().position(posi).title("" + tipo).snippet(info);
                        if (tipo.equals("BACKBONE")) {
                            opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.backbone));
                            palavrasChave = setor + " SD" + grupo;
                            opcoes.title(palavrasChave);
                        } else if (tipo.equals("NAP")) {
                            opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.nap));
                            palavrasChave = setor + " SD" + grupo +  " SA" + caixa;
                            opcoes.title(palavrasChave);
                        } else if (tipo.equals("POSTE")) {
                            opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                            opcoes.title("Poste");
                        } else {
                        }
                        marcador.setChaves(palavrasChave);
                       // bc.gravarMarcador(marcador);
                        tabelaDeMarcadores.add(marcador);
                        manipulador.post(new Runnable() {
                            @Override
                            public void run() {
                                Marker marc = mapa.addMarker(opcoes);
                                //   marc.setDraggable(true);
                                marcadores.add(marc);
                            }
                        });
                    }
                } catch (ParseException e) {
                    Log.i("Parse Exception", e + "");
                } catch (ClientProtocolException cpe) {
                    Log.e("ERRO_CPE", cpe.toString());
                } catch (IOException ioe) {
                    Log.e("ERRO_IO", ioe.toString());
                } catch (Exception erro) {
                    Log.e("ERRO_", erro.getMessage());
                }
            }
        }).start();
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private String[] getSugestoes(String filtro){
        List<String> retorno = new ArrayList<String>();
        for(int cc = 0; cc < tabelaDeMarcadores.size(); cc++){
            Marcador mar = tabelaDeMarcadores.get(cc);
            String chave = mar.getChaves();
            if(chave.contains(filtro)){
                retorno.add(chave);
            }
        }
        return retorno.toArray(new String[retorno.size()]);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private void atualizaSugestoes(String[] sugestoes){
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, sugestoes);
        campoDePesquisa.setAdapter(aa);
        aa.notifyDataSetChanged();
    }
    ////////////////////////////////////////////////////////////////////////////////////
    public void atualizaPosicaoDoGPS(Location loc) {
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        myLocation.setLocation(ll);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            allowNetwork = false;
        }
        if (allowNetwork || location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            atualizaPosicaoDoGPS(location);
        }

    }
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onProviderEnabled(String provider) {

    }
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onProviderDisabled(String provider) {

    }


}

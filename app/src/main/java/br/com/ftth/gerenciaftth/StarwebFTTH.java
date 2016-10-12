package br.com.ftth.gerenciaftth;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
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
import java.util.ArrayList;
import java.util.List;

public class StarwebFTTH extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        SearchView.OnQueryTextListener {

    SupportMapFragment mapFragment;
    GoogleMap mapa;
    private AlertDialog alerta;
    private boolean jaCarregouAActivity = false;
    private Marker posicaoAtual = null, minhaPosicao = null, marcadorSelecionado = null;
    FloatingActionButton editarElemento, excluirElemento, adicionarElemento;
    private ArrayList<Marcador> tabelaDeMarcadores = null,tabelaDeMarcadoresOffline = null;
    private List<Marker> marcadores, marcadoresOffline;
    private LocationManager locationManager = null;
    private boolean jaPerguntouSobreOGPS = false;
    private boolean oGPSEstaLigado = false;
    private MinhaLocalizacao myLocation;
    public String[] arrayPalavrasChave;
    Handler manipulador = new Handler();

    /////////////////////////_____________________________________________/////////////////////////
    /////////////////////////_____________________________________________/////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        verificaEPerguntaSobreOGPS();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void verificaEPerguntaSobreOGPS(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!jaPerguntouSobreOGPS) {
                perguntaSobreLigarOPGOS();
            }
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void  perguntaSobreLigarOPGOS(){
        DialogInterface.OnClickListener evento = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(it);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(StarwebFTTH.this);
        builder.setTitle("Informações").setCancelable(false);
        builder.setMessage("Para obter uma melhor precisão na sua localização, o GPS precisa estar ligado. Deseja ligar agora?").setPositiveButton("Sim", evento)
                .setNegativeButton("Não", evento);
        AlertDialog alerta = builder.create();
        jaPerguntouSobreOGPS = true;
        alerta.show();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            atualizaPosicaoDoGPS(location);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////
    public void atualizaPosicaoDoGPS(Location loc) {
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        myLocation.setLocation(ll);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onProviderEnabled(String provider) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onProviderDisabled(String provider) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapClick(LatLng latLng) {
        if (posicaoAtual != null) {
            posicaoAtual.remove();
        }
        posicaoAtual = mapa.addMarker(new MarkerOptions().position(latLng).title("Local").snippet("" + latLng.toString()));
        marcadorSelecionado = null;
        mostraMenuLateral(false, false, true);
        Log.d("Clique no mapa",latLng.toString());
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapLongClick(LatLng latLng) {
        adicionarDeMarcador(latLng);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onMarkerClick(Marker marker) {
        marcadorSelecionado = marker;
        if (marcadorSelecionado.equals(posicaoAtual)) {
            mostraMenuLateral(false, false, true);
        } else {
            mostraMenuLateral(true, true, false);
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMarkerDragStart(Marker marker) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMarkerDrag(Marker marker) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onQueryTextSubmit(String query) {
        toast(query,0);
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onQueryTextChange(String newText) {
        toast(newText,0);
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void toast(String s,int tempo) {
        Toast.makeText(this, s, ((tempo == 1) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MinhaLocalizacao implements LocationSource {
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starweb_ftth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        editarElemento = (FloatingActionButton) findViewById(R.id.editarElemento);
        excluirElemento = (FloatingActionButton) findViewById(R.id.excluirElemento);
        adicionarElemento = (FloatingActionButton) findViewById(R.id.btAdicionarElementoNoMapa);
        editarElemento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabelaDeMarcadores != null){
                    for (int f = 0; f < tabelaDeMarcadores.size(); f++) {
                        if (marcadores.get(f).equals(marcadorSelecionado)) {
                            Marcador m = tabelaDeMarcadores.get(f);
                            Marker mark = marcadores.get(f);
                            Intent it = new Intent(StarwebFTTH.this, EditarElementoActivity.class);
                            it.putExtra("id", m.getId());
                            it.putExtra("tipo", m.getTipo());
                            it.putExtra("setor", m.getSetor());
                            it.putExtra("alimentacao", m.getAlimentacao());
                            it.putExtra("grupo", m.getGrupo());
                            it.putExtra("caixa", m.getCaixa());
                            it.putExtra("info", m.getInfo());
                            it.putExtra("index", f);
                            it.putExtra("status", "WEB");
                            startActivityForResult(it, Constantes.EDITAR_ELEMENTO);
                            return;
                        }
                    }
                }
                for (int f = 0; f < tabelaDeMarcadoresOffline.size(); f++) {
                    if (marcadoresOffline.get(f).equals(marcadorSelecionado)) {
                        Marcador m = tabelaDeMarcadoresOffline.get(f);
                        Marker mark = marcadoresOffline.get(f);
                        Intent it = new Intent(StarwebFTTH.this, EditarElementoActivity.class);
                        it.putExtra("id", m.getId());
                        it.putExtra("tipo", m.getTipo());
                        it.putExtra("setor", m.getSetor());
                        it.putExtra("alimentacao", m.getAlimentacao());
                        it.putExtra("grupo", m.getGrupo());
                        it.putExtra("caixa", m.getCaixa());
                        it.putExtra("info", m.getInfo());
                        it.putExtra("index", f);
                        it.putExtra("status", "LOC");
                        startActivityForResult(it, Constantes.EDITAR_ELEMENTO);
                        return;

                    }
                }

            }
        });
        excluirElemento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = 0;
                int index = 0;
                String statusDoMarcador = "";
                if(estaConectdoAInternet()){
                    if(tabelaDeMarcadores != null){
                        for (int f = 0; f < tabelaDeMarcadores.size(); f++) {
                            if (marcadores.get(f).equals(marcadorSelecionado)) {
                                Marcador m = tabelaDeMarcadores.get(f);
                                id = m.getId();
                                index = f;
                                statusDoMarcador = "WEB";
                                break;
                            }
                        }
                    }
                }
                if(statusDoMarcador.equals("")){
                    if(tabelaDeMarcadoresOffline != null){
                        for (int f = 0; f < tabelaDeMarcadoresOffline.size(); f++) {
                            if (marcadoresOffline.get(f).equals(marcadorSelecionado)) {
                                Marcador m = tabelaDeMarcadoresOffline.get(f);
                                id = m.getId();
                                index = f;
                                statusDoMarcador = "LOC";
                                break;
                            }
                        }
                        if(statusDoMarcador.equals("")){return;}
                    }
                }
                final int _id = id;
                final int _index = index;
                final String status = statusDoMarcador;
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if(status.equals("WEB")){
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
                                                                toast("Marcador excluido com sucesso", 1);
                                                            }
                                                        });
                                                    } else {
                                                        toast( "Ocorreu um erro ao excluir o elemento", 1);
                                                    }
                                                } catch (ParseException e) {
                                                    Toast.makeText(StarwebFTTH.this, "Ocorreu um erro ao excluir o elemento." + e.toString(), Toast.LENGTH_LONG).show();
                                                }
                                            } catch (ClientProtocolException cpe) {
                                                Toast.makeText(StarwebFTTH.this, "Ocorreu um erro ao excluir o elemento." + cpe.toString(), Toast.LENGTH_LONG).show();
                                            } catch (IOException ioe) {
                                                Toast.makeText(StarwebFTTH.this, "Ocorreu um erro ao excluir o elemento." + ioe.toString(), Toast.LENGTH_LONG).show();
                                            } catch (Exception erro) {
                                                Toast.makeText(StarwebFTTH.this, "Ocorreu um erro ao excluir o elemento." + erro.toString(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }).start();
                                }
                                else if(status.equals("LOC")){
                                    BancoController bc = new BancoController(getBaseContext());
                                    int _id = tabelaDeMarcadoresOffline.get(_index).getId();
                                    bc.excluirMarcadorPeloId(_id);
                                    marcadoresOffline.get(_index).remove();
                                    marcadoresOffline.remove(_index);
                                    tabelaDeMarcadoresOffline.remove(_index);
                                    toast("Marcador offline excluido com sucesso",1);
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(StarwebFTTH.this);
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
                    adicionarDeMarcador(marcadorSelecionado.getPosition());
                } else {
                    adicionarDeMarcador(posicaoAtual.getPosition());
                }
            }
        });
        mostraMenuLateral(false, false, false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    //////////////////////////////// FIM DO ONCREATE //////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constantes.ADICIONAR_ELEMENTO) {
            if(resultCode == Activity.RESULT_CANCELED){ return;}
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

            if(tipo.equals("BACKBONE")) {
                opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.backbone));
                opcoes.title(setor + " SD" + grupo);
            } else if(tipo.equals("NAP")) {
                opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.nap));
                opcoes.title(setor + " SD" + grupo + " SA" + caixa);
            } else {
                opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                opcoes.title("Poste");
            }
            if (resultCode == Activity.RESULT_OK) {
                toast( "Marcador adicionado com sucesso em " + posi.latitude + "," + posi.longitude,1);
            }
            else if(resultCode == Constantes.RESULT_GRAVAR_NO_BANCO){
                m.setAtualizado("NAO");
                BancoController bc = new BancoController(this);
                bc.gravarMarcador(m);
                Marker mark = mapa.addMarker(opcoes);
                marcadoresOffline.add(mark);
                tabelaDeMarcadoresOffline.add(m);
                toast("Marcador gravado localmente em seu aparelho",1);
                return;
            }
                Marker marcador = mapa.addMarker(opcoes);
                marcadores.add(marcador);
                tabelaDeMarcadores.add(m);
        }
        if (requestCode == Constantes.EDITAR_ELEMENTO) {
            if(resultCode == Activity.RESULT_CANCELED){return;}
            Marcador m = null;
            String setor = data.getStringExtra("setor");
            int index = data.getIntExtra("index", 0);
            String alimentacao = data.getStringExtra("alimentacao");
            String grupo = data.getStringExtra("grupo");
            String caixa = data.getStringExtra("caixa");
            String info = data.getStringExtra("info");
            int _id = data.getIntExtra("id", 13);
            String tipo = data.getStringExtra("tipo");
            if (resultCode == Activity.RESULT_OK) {
                m = tabelaDeMarcadores.get(index);
                m.setTipo(tipo);
                m.setSetor(setor);
                m.setAlimentacao(alimentacao);
                m.setGrupo(grupo);
                m.setCaixa(caixa);
                m.setInfo(info);
                tabelaDeMarcadores.set(index, m);
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
            else if(resultCode == Constantes.RESULT_EDITAR_NO_BANCO){
                m = tabelaDeMarcadoresOffline.get(index);
                m.setTipo(tipo);
                m.setSetor(setor);
                m.setAlimentacao(alimentacao);
                m.setGrupo(grupo);
                m.setCaixa(caixa);
                m.setInfo(info);
                tabelaDeMarcadoresOffline.set(index, m);
                BancoController bc = new BancoController(this);
                bc.alterarMarcador(m);
                final Marker ma = marcadoresOffline.get(index);
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
                marcadoresOffline.set(index, ma);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private void verificaConexaoEEscolheOProvedorDeDados(){
            BancoController bc = new BancoController(this);
            Cursor c = bc.carregarMarcadores();
            marcadoresOffline = new ArrayList<Marker>();
            tabelaDeMarcadoresOffline = new ArrayList<Marcador>();
            if(c.getCount() > 0){
                Log.i("QT REGISTROS NO SQLITE:","" + c.getCount());
                c.moveToFirst();
                do{
                    String lat = "" + c.getString(c.getColumnIndex(CriaBanco.LATITUDE));
                    String longi = "" + c.getString(c.getColumnIndex(CriaBanco.LONGITUDE));
                    LatLng posi = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                    String setor = c.getString(c.getColumnIndex(CriaBanco.SETOR));
                    String alimentacao = c.getString(c.getColumnIndex(CriaBanco.ALIMENTACAO));
                    String grupo = c.getString(c.getColumnIndex(CriaBanco.GRUPO));
                    String caixa = c.getString(c.getColumnIndex(CriaBanco.CAIXA));
                    String info = c.getString(c.getColumnIndex(CriaBanco.INFO));
                    int _id = c.getInt(c.getColumnIndex(CriaBanco._ID));
                    String tipo = c.isNull(c.getColumnIndex(CriaBanco.TIPO)) ? "" : c.getString(c.getColumnIndex(CriaBanco.TIPO));
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
                    m.setAtualizado("NAO");
                    if (tipo.equals("BACKBONE")) {
                        opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.backbone));
                        opcoes.title(setor + " SD" + grupo);
                    } else if (tipo.equals("NAP")) {
                        opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.nap));
                        opcoes.title(setor + " SD" + grupo + " SA" + caixa);
                    } else if (tipo.equals("POSTE")) {
                        opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                        opcoes.title("Poste");
                    }
                    Marker marcador = mapa.addMarker(opcoes);
                    marcadoresOffline.add(marcador);
                    tabelaDeMarcadoresOffline.add(m);
                }
                while(c.moveToNext());
            }
            if(estaConectdoAInternet()){carregarMarcadoresDaInternet();}
    }
    ////////////////////////////////////////////////////////////////////////////////////
    public  boolean estaConectdoAInternet() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {  conectado = false;  }
        return conectado;
    }
    ////////////////////////////////////////////////////////////////////////////////////
    private void carregarMarcadoresDaInternet() {
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
                    marcadores = new ArrayList<Marker>();
                    tabelaDeMarcadores = new ArrayList<Marcador>();
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
                        if (tipo.equals("NAP")) {
                            caixa = e.getString("caixa");
                        }
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
                            palavrasChave = setor + " SD" + grupo + " SA" + caixa;
                            opcoes.title(palavrasChave);
                        } else if (tipo.equals("POSTE")) {
                            opcoes.icon(BitmapDescriptorFactory.fromResource(R.drawable.poste));
                            opcoes.title("Poste");
                        } else {
                        }
                        marcador.setChaves(palavrasChave);
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
    /////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void mostraEscolhaDeOndeGravarOMarcador(final LatLng coordenadas){
        AlertDialog.Builder builder = new AlertDialog.Builder(StarwebFTTH.this);
        builder.setTitle("Confirmação");
        builder.setMessage("Como deseja gravar o marcador?");
        String[] opcoes ={"Marcador online","Marcador offline"};
        builder.setItems(opcoes,new DialogInterface.OnClickListener() {
            Bundle params = new Bundle();
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        params.putString("ONDE_GRAVAR", "WEB");
                        break;
                    case 1:
                        params.putString("ONDE_GRAVAR", "LOC");
                        break;
                }
                params.putDouble("latitude", coordenadas.latitude);
                params.putDouble("longitude", coordenadas.longitude);
                Intent  intencao = new Intent(StarwebFTTH.this, AddElementoActivity.class);
                intencao.putExtras(params);
                startActivityForResult(intencao, Constantes.ADICIONAR_ELEMENTO);
            }
        });

        Dialog alerta = builder.create();
        alerta.show();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void adicionarDeMarcador(final LatLng coordenadas) {
        Bundle params = new Bundle();
        params.putDouble("latitude", coordenadas.latitude);
        params.putDouble("longitude", coordenadas.longitude);
        Intent  intencao = new Intent(StarwebFTTH.this, AddElementoActivity.class);
        intencao.putExtras(params);
        startActivityForResult(intencao, Constantes.ADICIONAR_ELEMENTO);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.starweb_ftth, menu);
        inflater.inflate(R.menu.barra_de_pesquisa, menu);
        SearchView mSearchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        mSearchView.setQueryHint("");
        mSearchView.setOnQueryTextListener(this);
        return true;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.informacoes) {
            return true;
        }
        else if (id == R.id.downloads) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mapa_hibrido) {
            mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (id == R.id.mapa_normal) {
            mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.mapa_terreno) {
            mapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.mapa_satelite) {
            mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        if (mapa != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
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
            verificaConexaoEEscolheOProvedorDeDados();

        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
}

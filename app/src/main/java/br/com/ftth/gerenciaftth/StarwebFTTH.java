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
    private ArrayList<Marcador> tabelaDeMarcadores;
    private List<Marker> marcadores;
    private LocationManager locationManager;
    private boolean jaPerguntouSobreOGPS = false;
    private boolean oGPSEstaLigado = false;
    private MinhaLocalizacao myLocation;
    public String[] arrayPalavrasChave;
    Handler manipulador = new Handler();
    private GoogleApiClient client;

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!jaPerguntouSobreOGPS) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
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
                builder.setMessage("Para uma melhor precisão na sua localização, o GPS precisa estar ligado. Deseja ligar agora?").setPositiveButton("Sim", dialogClickListener)
                        .setNegativeButton("Não", dialogClickListener);
                AlertDialog alerta = builder.create();
                jaPerguntouSobreOGPS = true;
                alerta.show();
            }
        } else {
            oGPSEstaLigado = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
    public boolean onMarkerClick(Marker marker) {
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

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        toast(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        toast(newText);
        return false;
    }

    private void toast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
    }

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
        marcadores = new ArrayList<Marker>();
        editarElemento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                                            Toast.makeText(StarwebFTTH.this, "Marcador excluido com sucesso", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                    //  marcadorSelecionado = null;
                                                } else {
                                                    Toast.makeText(StarwebFTTH.this, "Ocorreu um erro ao excluir o elemento", Toast.LENGTH_LONG).show();
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
                    mostraOpcoesDeMarcadores(marcadorSelecionado.getPosition());
                } else {
                    mostraOpcoesDeMarcadores(posicaoAtual.getPosition());
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
                    opcoes.title(setor + " SD" + grupo + " SA" + caixa);
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

    private void mostraOpcoesDeMarcadores(final LatLng coordenadas) {
        Bundle params = new Bundle();
        params.putDouble("latitude", coordenadas.latitude);
        params.putDouble("longitude", coordenadas.longitude);
        Intent intencao;
        intencao = new Intent(StarwebFTTH.this, AddElementoActivity.class);
        intencao.putExtras(params);
        startActivityForResult(intencao, Constantes.ADICIONAR_ELEMENTO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.starweb_ftth, menu);

        inflater.inflate(R.menu.barra_de_pesquisa, menu);

        //Pega o Componente.
        SearchView mSearchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        //Define um texto de ajuda:
        mSearchView.setQueryHint("teste");

        // exemplos de utilização:
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        if (mapa != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapa.setMyLocationEnabled(true);
            mapa.getUiSettings().setCompassEnabled(true);
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
    }
}

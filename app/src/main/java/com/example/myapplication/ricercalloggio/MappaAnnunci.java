package com.example.myapplication.ricercalloggio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.Helper;
import com.example.myapplication.R;
import com.example.myapplication.classi.Annuncio;
import com.example.myapplication.classi.Casa;
import com.example.myapplication.home.Home;
import com.example.myapplication.profilo.ProfiloAnnuncio;
import com.example.myapplication.profilo.ProfiloCasa;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

public class MappaAnnunci extends AppCompatActivity implements OnMapReadyCallback {

    //FILTRI DEFAULT
    private static final int PREZZO_STANDARD = 550;
    private static final int RATING_MINIMO = 0;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "Mappe";
    private MapView mapView;
    GoogleMap gmap;
    //Database
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    //LISTE
    List<Casa> listaCase;
    List<Annuncio> listaAnnunci;
    ListView listView;
    Button b_visualizzaCasa;
    Casa casa;
    static Filtro filtro = new Filtro();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappa_annunci);
        createMapView(savedInstanceState);

        database = FirebaseDatabase.getInstance("https://appartamento-81c2d-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();

        b_visualizzaCasa = (Button) findViewById(R.id.visualizzaCasa);
        b_visualizzaCasa.setVisibility(View.GONE);

        listaAnnunci = new LinkedList<>();
        listaCase = new LinkedList<>();
        initListe();


    }




    private void initListe() {

        myRef.child("Annunci").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot annData: dataSnapshot.getChildren()) {
                    Annuncio ann = annData.getValue(Annuncio.class);
                    listaAnnunci.add(ann);}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        myRef.child("Case").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot annData: dataSnapshot.getChildren()) {
                    Casa ann = annData.getValue(Casa.class);
                    listaCase.add(ann);}
                caricaMappa();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void caricaMappa() {

        //CARICO SOLO LE CASE CHE HANNO DEGLI ANNUNCI
        for(Casa a : listaCase){
            boolean annucnio = false;
            for(Annuncio ann : listaAnnunci){
                    //Se per la casa vi è un annuncio
                if(ann.getIdCasa().compareTo(a.getNomeCasa())==0
                   && ann.getPrezzoMensile()<=filtro.prezzo
                        && a.getValutazione()>=filtro.rating &&
                        ((ann.getTipologiaAlloggio().equals("Intero appartamento") && filtro.intero)
                                || (ann.getTipologiaAlloggio().equals("Stanza singola") && filtro.singola)
                                    || (ann.getTipologiaAlloggio().equals("Stanza doppia") && filtro.doppia)
                                        || (ann.getTipologiaAlloggio().equals("Posto letto") && filtro.posto))){
                    annucnio = true;
                }
            }
            if(annucnio){
                //METTO I FILTRI
                MarkerOptions mo = new MarkerOptions();
                Marker perth = gmap.addMarker(
                        mo
                                .position(new LatLng(a.getLat(),a.getLng()))
                                .draggable(true));
                gmap.addMarker(mo);
                perth.setTitle(a.getNomeCasa());
            }
        }
    }

    public void indietro(View view) {
        Intent intent = new Intent(MappaAnnunci.this , Home.class);
        startActivity(intent); }


    public void visualizzaCasa(View view) {
        //TODO collegare l'Annuncio alla mappa
        Intent intent = new Intent(MappaAnnunci.this , ProfiloCasa.class);
        intent.putExtra("nomeCasa",casa.getNomeCasa());
        startActivity(intent);
    }
    public void listaCase(View view) {
        Intent intent = new Intent(MappaAnnunci.this , ListaAnnunci.class);
        startActivity(intent); }

    private void createMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    
    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;
        caricaMappa();
        //configurazioni
        gmap.setMinZoomPreference(12);
        //gmap.setTrafficEnabled(true);
        UiSettings uiSettings = gmap.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setCompassEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);

        //Aggiungo coordinate del poli
        LatLng poliTo = new LatLng(45.057856432, 7.65664237342);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(poliTo,15)); // 0-20
        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String nomeCasa = marker.getTitle();
                List<Annuncio> annunciCasa = new LinkedList<>();

                for(Casa c : listaCase){
                    if(c.getNomeCasa().compareTo(nomeCasa)==0){
                        casa = c;
                        for(Annuncio a : listaAnnunci){
                            if(a.getIndirizzo().compareTo(casa.getIndirizzo())==0)
                                annunciCasa.add(a);
                        }
                    }

                }

                aggiornaListView(annunciCasa);
                //ABBIAMO LA CASA E GLI ANNUNCI PER LA CASA
                b_visualizzaCasa.setVisibility(View.VISIBLE);
                b_visualizzaCasa.setText("Vedi "+casa.getNomeCasa());
                return false;
            }
        });

    }

    private void aggiornaListView(List<Annuncio> annunciCasa) {

        listView = (ListView) findViewById(R.id.listaAnnunciCasa);
        ListaAnnunci.CustomItem[] items = createItems(annunciCasa);

        ArrayAdapter<ListaAnnunci.CustomItem> arrayAdapter = new ArrayAdapter<ListaAnnunci.CustomItem>(
                this, R.layout.row_lv_lista_annunci, R.id.textViewNomeCasaLista, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                return getViewNotOptimized(position,convertView,parent); }

            public View getViewNotOptimized(int position, View convertView, ViewGroup par){
                ListaAnnunci.CustomItem item = getItem(position); // Rif. alla riga attualmente
                LayoutInflater inflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.row_lv_lista_annunci, null);
                ImageView imageViewCasa =
                        (ImageView)rowView.findViewById(R.id.immagineAnnuncioLista);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference profileRef = storageReference.child("Annuncio/"+item.nomeCasa+"/foto0.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG,"URI "+uri);
                        Picasso.get().load(uri).into(imageViewCasa);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                TextView nomeCasaView =
                        (TextView)rowView.findViewById(R.id.textViewNomeCasaLista);
                TextView prezzoCasaView =
                        (TextView)rowView.findViewById(R.id.textViewPrezzCasaLista);
                nomeCasaView.setText(item.nomeCasa);
                prezzoCasaView.setText(item.prezzoCasa);
                return rowView;
            }
        };
        listView.setAdapter(arrayAdapter);
        Helper.getListViewSize(listView);
        //aggista--------
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //TODO prendo l'id della casa che ho cliccato vado ad aggiungi annuncio, pushando con l'intent l'id
                ListaAnnunci.CustomItem annuncio = (ListaAnnunci.CustomItem) adapterView.getItemAtPosition(pos);
                String nomeCasa = annuncio.nomeCasa;
                creaAnnuncio(nomeCasa);
            }
        });
    }

    private ListaAnnunci.CustomItem[] createItems(List<Annuncio> annunciCasa) {
        Log.i(TAG, ""+annunciCasa.size());
        int size = annunciCasa.size();

        ListaAnnunci.CustomItem[] items = new ListaAnnunci.CustomItem[size]; //numero di annunci possibili
        for (int i = 0; i < items.length; i++) {
            //mi prendo il riferimento all'annuncio
            Annuncio a = annunciCasa.get(i);
            items[i] = new ListaAnnunci.CustomItem();
            items[i].nomeCasa = a.getIdAnnuncio();
            items[i].prezzoCasa = a.getPrezzoMensile()+" Euro al mese";
            Log.i(TAG, items[i].nomeCasa+" "+items[i].prezzoCasa);
        }
        return items;
    }

    private void creaAnnuncio(String casa) {
        Intent intent = new Intent(this, ProfiloAnnuncio.class);
        Log.i(TAG,"VADO A CASA "+casa);
        //TODO cosa mi conviene passare, idAnnuncio
        intent.putExtra("idAnnuncio", casa);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if(gmap!=null){
            gmap.clear();
            caricaMappa();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (gmap != null) {
            gmap.clear();
            caricaMappa();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop(); }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy(); }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory(); }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); }

    public void modificaFiltri(View view) {
        Intent intent = new Intent(this, FiltriRicerca.class);
        startActivity(intent);
    }

    protected static class Filtro {

        int prezzo;
        int rating;
        boolean intero;
        boolean singola;
        boolean doppia;
        boolean posto;


        public Filtro() {
            this.prezzo = PREZZO_STANDARD;
            this.rating = RATING_MINIMO;
            this.intero = true;
            this.singola = true;
            this.doppia = true;
            this.posto = true;
        }
    }
}
package com.example.appreproductor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button btnReproducir;
    private Button btnAnterior;
    private Button btnSiguiente;
    private TextView lblCancionActual;
    private SeekBar seekbarCancion;
    private SeekBar seekbarVolumen;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Runnable runnable;
    private Handler handler;

    private ArrayList<String> listaArchivos = new ArrayList();
    private ArrayList<ArrayList<String>> listaCanciones = new ArrayList<ArrayList<String>>();
    private ArrayList<String> listaDatosCancionesView = new ArrayList();
    private ArrayList<String> listaDatosCancionesLabel = new ArrayList();
    private ArrayList<String> idsCanciones = new ArrayList<>();
    private int indiceCancionActual = 0;


    public void playClick(View view){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                btnReproducir.setBackgroundResource(R.drawable.btnplay);
            }else{
                mediaPlayer.start();
                btnReproducir.setBackgroundResource(R.drawable.btnpause);
                modificarSeekbarCanción();
            }
        }
        if(mediaPlayer==null){
            btnReproducir.setBackgroundResource(R.drawable.btnpause);
            reproducirCancion();
        }
    }
    public void pause(){
        mediaPlayer.pause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnReproducir = (Button) findViewById(R.id.btnPlay);
        btnSiguiente = (Button) findViewById(R.id.btnFollowing);
        btnAnterior = (Button) findViewById(R.id.btnPrevious);
        lblCancionActual = (TextView) findViewById(R.id.lblDatos);
        handler = new Handler();
        seekbarCancion = findViewById(R.id.seekbarDuracion);
        obtenerNombresArchivos();
        obtenerDetallesCanciones();
        poblarListaCanciones();
        animarTexto();
        //lblCancionActual.setText(listaDatosCancionesLabel.get(indiceCancionActual));
        controlarVolumen();
    }

    public void reproducirCancion(){
        int cancion = Integer.parseInt(idsCanciones.get(indiceCancionActual));
        mediaPlayer = MediaPlayer.create(this,cancion);
        //animarTexto();
        //lblCancionActual.setText(listaDatosCancionesLabel.get(indiceCancionActual));
        try{
            mediaPlayer.prepare();
        }catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekbarCancion.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                modificarSeekbarCanción();
            }
        });
        seekbarCancion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                cancionSiguiente();
            }
        });
    }
    public void modificarSeekbarCanción(){
        seekbarCancion.setProgress(mediaPlayer.getCurrentPosition());
        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    modificarSeekbarCanción();
                }
            };
            handler.postDelayed(runnable,1000);
        }
    }
    public void controlarVolumen(){
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        SeekBar volumeSeekBar = findViewById(R.id.seekbarVolumen);

        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                Log.d("volume:", Integer.toString(progress));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public static int obtenerIDs(String song) throws IllegalArgumentException
    {
        HashMap resmap = new HashMap();
        Field[] fields = R.raw.class.getFields();
        try
        {
            for(int i = 0; i < fields.length; i++)
            {
                if(song != null)
                    if(fields[i].getName().startsWith(song))
                        resmap.put(fields[i].getName(), fields[i].getInt(null));
                    else
                        resmap.put(fields[i].getName(), fields[i].getInt(null));
            }
        } catch (Exception e)
        {
            throw new IllegalArgumentException();
        }
        Integer one = (Integer) resmap.get(song);
        int songid = one.intValue();
        return songid;
    }
    public void obtenerDetallesCanciones(){
        int cantidadCanciones = listaArchivos.size()-1;

        for(int i=0;i<=cantidadCanciones;i++){
            //Se debe poner el arraylist aquí porque sino borra los datos porque apunta a la misma referencia
            ArrayList<String> datoCancion= new ArrayList<String>();
            String nombreArchivo = listaArchivos.get(i);
            Log.i("ENTRE CON",nombreArchivo);
            int idCancion = obtenerIDs(nombreArchivo);
            Uri mediaPath = Uri.parse("android.resource://" + getPackageName() + "/" + idCancion);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, mediaPath);
            String tituloCancion = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String tituloArtista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            idsCanciones.add(String.valueOf(idCancion));

            datoCancion.add(tituloCancion);

            datoCancion.add(tituloArtista);
            listaDatosCancionesView.add(tituloCancion+"\n"+tituloArtista);
            listaDatosCancionesLabel.add(tituloArtista+" - "+tituloCancion);
            listaCanciones.add(datoCancion);

            Log.i("LISTA",listaCanciones.toString());
            Log.i("LISTA",listaDatosCancionesView.toString());
        }

    }
    public void obtenerNombresArchivos(){
        Field[] campos = R.raw.class.getFields();

        for(int count=0; count < campos.length; count++){
            String nombre = campos[count].getName();
            listaArchivos.add(nombre);
        }
        Log.i("ARCHIVOS",listaArchivos.toString());
    }
    public void poblarListaCanciones(){
        ListView listView = findViewById(R.id.lvListaCanciones);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listaDatosCancionesView);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cancionPresionada(i);
            }
        });
    }
    public void btnSiguienteClicked(View view){
        cancionSiguiente();
    }
    public void btnAnteriorClicked(View view){
        cancionAnterior();
    }
    public void cancionSiguiente(){
        if(mediaPlayer!=null){

            if(mediaPlayer.isPlaying()==false){
                btnReproducir.setBackgroundResource(R.drawable.btnplay);
            }
            if(indiceCancionActual==idsCanciones.size()-1){
                indiceCancionActual=0;
                mediaPlayer.stop();
                btnReproducir.setBackgroundResource(R.drawable.btnpause);
                animarTexto();
                reproducirCancion();
            }
            else{
                indiceCancionActual=indiceCancionActual+1;
                mediaPlayer.stop();
                btnReproducir.setBackgroundResource(R.drawable.btnpause);
                animarTexto();
                reproducirCancion();
            }
        }else{
            indiceCancionActual=indiceCancionActual+1;
            animarTexto();
        }
    }
    public void cancionAnterior(){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()==false){
                btnReproducir.setBackgroundResource(R.drawable.btnplay);
            }
            if(indiceCancionActual==0){
                indiceCancionActual=idsCanciones.size()-1;
                mediaPlayer.stop();
                btnReproducir.setBackgroundResource(R.drawable.btnpause);
                animarTexto();
                reproducirCancion();
            }
            else{
                indiceCancionActual=indiceCancionActual-1;
                mediaPlayer.stop();
                btnReproducir.setBackgroundResource(R.drawable.btnpause);
                animarTexto();
                reproducirCancion();
            }
        }else{
            indiceCancionActual=indiceCancionActual-1;
            animarTexto();
        }
    }
    public void cancionPresionada(int indice){
        if(mediaPlayer!=null){
            indiceCancionActual=indice;
            mediaPlayer.stop();
            btnReproducir.setBackgroundResource(R.drawable.btnpause);
            animarTexto();
            reproducirCancion();
        }else{
            indiceCancionActual=indice;
            btnReproducir.setBackgroundResource(R.drawable.btnpause);
            animarTexto();
            reproducirCancion();
        }
    }
    public void animarTexto(){
        lblCancionActual.setText(listaDatosCancionesLabel.get(indiceCancionActual));
        lblCancionActual.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.slide_in_left));
    }
}

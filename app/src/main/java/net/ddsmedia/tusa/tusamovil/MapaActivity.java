package net.ddsmedia.tusa.tusamovil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Orden;
import net.ddsmedia.tusa.tusamovil.model.People;
import net.ddsmedia.tusa.tusamovil.model.PuntoRuta;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.VISIBLE;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.sendPushOP;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //private Context mContext;
    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private Orden mOrden;
    private  int tipoEvidencia;
    private int mZoom = 10;

    private Marker mMarker;
    private ImageView imgOp;
    private ProgressBar pbOp;

    private Boolean showRate = false;
    private Boolean showShare = true;
    private SaveCalif mUpdTask;

    private Handler handler;
    private Runnable runnable;

    LatLngBounds.Builder builder;

    Boolean bandera = true, bandera2=true, bandera3=true, bandera4=true, checks=false;
    ProgressDialog progressDialog, progressDialog2;
    List<Integer> listaAgencias, listaUsuarios;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        tipoEvidencia = b.getInt("tipoEvidencia");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            //mOrdenStr = "";//mUserInfo.getOrden();
            mOrden = new Orden(new JSONObject(b.getString("orden")));
            if(mOrden.getEstado() == Globals.ORDEN_FINALIZADA && mOrden.getCalificacion() == 0)
                showRate = true;
            Log.i("ORDEN_INFO",mOrden.toJSON().toString());
            //if(mOrden.getEstado() == Globals.ORDEN_EN_SITIO)
            if(mOrden.getEstado() == Globals.ORDEN_EN_SITIO || mOrden.getEstado() == Globals.ORDEN_FINALIZADA)
                mZoom = 15;
            if(mOrden.getEstado() == Globals.ORDEN_FINALIZADA || mOrden.getFk_compartido() > -1)
                showShare = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Ubicación VIN " + mOrden.getVIN());
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng ubicacion = new LatLng(Double.parseDouble(mOrden.getLatitud()), Double.parseDouble(mOrden.getLongitud()));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, mZoom));

        /*String msg = mOrden.getEstadoStr() + " "+mOrden.getFecha();
        if(mOrden.getVelocidad() > 1)
            msg += "\n"+Math.round(mOrden.getVelocidad())+" km/h";


        mMarker = mMap.addMarker(new MarkerOptions()
                .title("VIN "+mOrden.getVIN())
                .snippet(msg)
                .position(ubicacion)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMarker.showInfoWindow();*/

        //if(mUserInfo.getTipo() > Globals.CLIENTE_PROPIETARIO){
            mGetRutaTask = new GetOrdenRuta();
            mGetRutaTask.execute();
        //}

        builder = new LatLngBounds.Builder();
        builder.include(ubicacion);
        if(showRate){
            handler = new Handler();
            runnable = new Runnable(){
                public void run() {
                    showCalificar();
                }
            };

            handler.postDelayed(runnable, 5000);
        }
    }

    private static String mTiempo = "";
    public static void getTimeLeft(Orden orden){
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                //"key=AIzaSyBKRD4FThgZFd0cQSTH1EoU8RdAYTugQng&" +
                "key=AIzaSyCdu0hRe-uh_PTW7bT-0xpDCl5yRzAY1Qw&" +
                "origins="+orden.getLatitud()+","+orden.getLongitud()+"&" +
                "destinations="+orden.getGeoDestino();
        Log.i("GET_TIMELEFT",url);
        mTiempo = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = null;
            inputStream = httpResponse.getEntity().getContent();
            String result = Globals.convertInputStreamToString(inputStream);

            Log.i("GET_TIMELEFT",result);

            JSONObject res = new JSONObject(result);
            if(res.getString("status").equals("OK")){
                JSONArray arr = res.getJSONArray("rows");
                if(arr.length() > 0){
                    JSONObject elem = arr.getJSONObject(0);
                    JSONArray elemArr = elem.getJSONArray("elements");
                    JSONObject elem0 = elemArr.getJSONObject(0);
                    if(elem0.get("status").equals("OK")){
                        JSONObject dura = elem0.getJSONObject("duration");
                        mTiempo = dura.getString("text");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private GetOrdenRuta mGetRutaTask;
    public class GetOrdenRuta extends AsyncTask<Void, Void, ArrayList<PuntoRuta>> {
        String z = "";
        Boolean isSuccess = false;
        ArrayList<PuntoRuta> puntos = new ArrayList<PuntoRuta>();

        @Override
        protected ArrayList<PuntoRuta> doInBackground(Void... params) {
            String query = "SELECT latitud, longitud, FORMAT(fecha,'dd/MM HH:mm') AS fechaf, velocidad, " +
                        "ABS(DATEDIFF(second,fecha,(SELECT iniciada FROM orden_status WHERE fk_orden = fk_ot))) AS iniciada, " +
                        "ABS(DATEDIFF(second,fecha,(SELECT en_sitio FROM orden_status WHERE fk_orden = fk_ot))) AS destino " +
                    "FROM geo_op " +
                    "WHERE fk_ot = '" + mOrden.getID() + "' AND latitud != '0.0' AND longitud != '0.0' ORDER BY fecha";
            Log.i("PUNTOOOOS",query);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()){
                        PuntoRuta punto = new PuntoRuta(rs);
                        puntos.add(punto);
                    }
                    isSuccess=true;
                }
                mTiempo = "";
                if(!mOrden.getGeoDestino().equals("0,0") && !mOrden.getGeoDestino().equals("0.0,0.0"))
                    getTimeLeft(mOrden);
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return puntos;
        }

        @Override
        protected void onPostExecute(final ArrayList<PuntoRuta> puntos) {
            mGetRutaTask = null;
            PolylineOptions poly = new PolylineOptions();
            poly.geodesic(true);

            if(puntos.size() > 0) {
                PuntoRuta iniciada = puntos.get(0);
                PuntoRuta destino = puntos.get(0);

                for (PuntoRuta punto : puntos) {
                    poly.add(new LatLng(punto.getLatitud(), punto.getLongitud()));
                    if (punto.getIniciadaDiff() < iniciada.getIniciadaDiff()) {
                        iniciada = punto;
                    }
                    if (punto.getDestinoDiff() < destino.getDestinoDiff()) {
                        destino = punto;
                    }
                }
                mMap.addPolyline(poly);

                PuntoRuta origen = puntos.get(0);
                mMap.addMarker(new MarkerOptions()
                        .title("ORIGEN")
                        .snippet(origen.getFecha())
                        .position(new LatLng(origen.getLatitud(), origen.getLongitud()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                if (mOrden.getEstado() == Globals.ORDEN_FINALIZADA) {
                    builder.include(new LatLng(origen.getLatitud(), origen.getLongitud()));
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 15);

                    mMap.animateCamera(cu);

                    mMap.addMarker(new MarkerOptions()
                            .title("INICIADA")
                            .snippet(iniciada.getFecha())
                            .position(new LatLng(iniciada.getLatitud(), iniciada.getLongitud()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                    mMap.addMarker(new MarkerOptions()
                            .title("DESTINO")
                            .snippet(destino.getFecha())
                            .position(new LatLng(destino.getLatitud(), destino.getLongitud()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                    Random r = new Random();
                    PuntoRuta azar;
                    for (int i = 0; i < 6; i++) {
                        int indice = r.nextInt(puntos.size());
                        azar = puntos.get(indice);
                        mMap.addMarker(new MarkerOptions()
                                .title(azar.getFecha())
                                //.snippet(azar.getFecha())
                                .position(new LatLng(azar.getLatitud(), azar.getLongitud()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    }

                } else {
                    PuntoRuta ultimo = puntos.get(puntos.size() - 1);
                    LatLng ubicacion = new LatLng(ultimo.getLatitud(), ultimo.getLongitud());
                    String msg = mOrden.getEstadoStr() + " " + mOrden.getFecha();
                    long velo = Math.round(mOrden.getVelocidad());
                    if (velo > 85) velo = 85;
                    if (mOrden.getVelocidad() > 1)
                        msg += "<br>" + velo + " km/h <small>Aprox</small>";
                    if (!mTiempo.equals(""))
                        msg += "<br>" + mTiempo + " <small>Aprox</small>";


                    mMarker = mMap.addMarker(new MarkerOptions()
                            .title("VIN " + mOrden.getVIN())
                            .snippet(msg)
                            .position(ubicacion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            LinearLayout info = new LinearLayout(MapaActivity.this);
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(MapaActivity.this);
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(MapaActivity.this);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(Html.fromHtml(marker.getSnippet()));
                            snippet.setGravity(Gravity.CENTER);

                            info.addView(title);
                            info.addView(snippet);

                            return info;
                        }
                    });

                    mMarker.showInfoWindow();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGetRutaTask = null;
        }
    }

    private void showOperador(){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Operador");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_operador, null);
        b.setView(dialogView);

        final TextView txtNombre = (TextView) dialogView.findViewById(R.id.txtNombre);
        txtNombre.setText(mOrden.getNombreOperador());

        final ImageButton btnCall = (ImageButton) dialogView.findViewById(R.id.btnCall);
        btnCall.setVisibility(View.GONE);
        btnCall.setImageResource(R.drawable.ic_phone_call);

        TextView txtCel = (TextView) dialogView.findViewById(R.id.txtCel);
        txtCel.setText(mOrden.getCelOperador());

        pbOp = (ProgressBar) dialogView.findViewById(R.id.pbOp);

        imgOp = (ImageView) dialogView.findViewById(R.id.imgOperador);
        imgOp.setVisibility(View.GONE);

        if(!mOrden.getCelOperador().isEmpty()){
            btnCall.setVisibility(VISIBLE);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+mOrden.getCelOperador()));
                    startActivity(callIntent);
                }
            });
        }

        b.setNegativeButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = b.create();
        dialog.show();

        new GetImage().execute();
    }

    private void showCalificar(){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Calificar Traslado");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_rate, null);
        b.setView(dialogView);

        final RatingBar rate = (RatingBar) dialogView.findViewById(R.id.rateStar);
        final EditText txtComm = (EditText) dialogView.findViewById(R.id.rateComment);

        b.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        b.setPositiveButton("Aceptar",null);

        final AlertDialog dialog = b.create();
        Log.i("WINDOWWWW","MOSTRADAAAAAAAAAAAAAAAAAAaaa");
        if(!((Activity) this).isFinishing()) {
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String comm = txtComm.getText().toString();
                    int calif = (int) rate.getRating();

                    Log.i("NOENTRE","Calif: ."+calif+"::"+comm+".");
                    if(calif > 0){
                        Toast.makeText(MapaActivity.this,"Enviando calificación",Toast.LENGTH_SHORT).show();
                        mUpdTask = new SaveCalif(calif,comm);
                        mUpdTask.execute();
                        dialog.dismiss();
                    }else{
                        Toast.makeText(MapaActivity.this,"Debe indicar la calificación",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private class GetImage extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = Globals.getImageFromFTP(String.valueOf(mOrden.getOperador()),getApplicationContext());
            //Log.i("IMAGEEEEE",bitmap.getByteCount()+"");
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            pbOp.setVisibility(View.GONE);
            imgOp.setVisibility(View.VISIBLE);
            imgOp.setImageBitmap(bitmap);
        }
    }

    public class SaveCalif extends AsyncTask<Void, Void, Boolean> {
        private final int mCalif;
        private final String mComment;
        String z = "";
        Boolean isSuccess = false;

        SaveCalif(int calif, String comment) {
            mCalif = calif;
            mComment = comment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                String query = "UPDATE orden_status SET calificacion = '" + mCalif + "', comentario = '" + mComment + "', calificada = GETDATE() " +
                        "WHERE fk_orden = '" + mOrden.getID() + "' ";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;

                    String version = Globals.getVersion(getApplicationContext());
                    //String[] paramLog = {String.valueOf(mUserInfo.getMatricula()), mUserInfo.getEmail(), mOrden.getID()+" calificada", version};
                    String[] paramLog = {String.valueOf(mUserInfo.getCliente()), mUserInfo.getEmail(), mOrden.getID()+" calificada", version};
                    String queryLog = Globals.makeQuery(Globals.QUERY_ACCION, paramLog);
                    PreparedStatement preparedStatementLog = conn.prepareStatement(queryLog);
                    preparedStatementLog.executeUpdate();
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdTask = null;
            if (success) {
                Toast.makeText(getApplicationContext(),"Calificacion Enviada",Toast.LENGTH_SHORT).show();
                showRate = false;
                invalidateOptionsMenu();
            } else {
                Toast.makeText(getApplicationContext(),"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_orden, menu);
        MenuItem mnuRate = menu.findItem(R.id.mnu_calificar);
        MenuItem mnuFalso = menu.findItem(R.id.mnu_falso);
        MenuItem mnuShare = menu.findItem(R.id.mnu_share);

        if(!showRate){
            mnuRate.setVisible(false);
        }
        if(mOrden.getEstado() != Globals.ORDEN_ORIGEN || mUserInfo.getTipo() < Globals.CLIENTE_BASE){
            mnuFalso.setVisible(false);
        }
        if(!showShare) mnuShare.setVisible(false);

        //MenuItem mnuFotos = menu.findItem(R.id.mnu_fotos);
        //MenuItem mnuFotos2 = menu.findItem(R.id.mnu_fotosf);
        //mnuFotos.setVisible(false);
        //mnuFotos2.setVisible(false);
        //if(mOrden.getFotos() >= 4) mnuFotos.setVisible(true);
        //if(mOrden.getFotos() == 8) mnuFotos2.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                this.finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            case R.id.mnu_operador:
                showOperador();
                return true;
            case R.id.mnu_calificar:
                showCalificar();
                return true;
            case R.id.mnu_falso:
                stopSiNo();
                return true;
            case R.id.mnu_share:
                showPeople();

            case R.id.mnu_evidencias:
                Intent intenth = new Intent(MapaActivity.this, EvidenciasActivity.class);
                intenth.putExtra("tipoEvidencia",tipoEvidencia);
                intenth.putExtra("user", mUserStr);
                intenth.putExtra("orden", mOrden.getID());
                intenth.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intenth);
                MapaActivity.this.overridePendingTransition (R.anim.open_next, R.anim.close_next);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void stopSiNo(){
        AlertDialog alertDialog = new AlertDialog.Builder(MapaActivity.this).create();
        alertDialog.setTitle("Orden en Falso");
        alertDialog.setMessage("¿Esta seguro de poner en Falso la orden "+mOrden.getID()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Falso",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUpdateTask = new UpdateOrden(Globals.ORDEN_FALSO, mOrden.getID(), mOrden.getOperador(),MapaActivity.this);
                        mUpdateTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private UpdateOrden mUpdateTask;
    public class UpdateOrden extends AsyncTask<Void, Void, Boolean> {
        private final int mEstado;
        private final int mMatr;
        private final String mOrdenID;
        String z = "";
        Boolean isSuccess = false;
        private Context mContext;

        UpdateOrden(int estado, String orden, int matr, Context context) {
            mEstado = estado;
            mOrdenID = orden;
            mMatr = matr;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            //mProgress.setVisibility(VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                String field = "falso";
                String query = "UPDATE orden_status SET estado = '" + mEstado + "', " + field + " = GETDATE() " +
                        "WHERE fk_orden = '" + mOrdenID + "' ";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;

                    String query2 = "UPDATE Orden_traslados SET Traslado_en_falso = 1 " +
                            "WHERE ID_orden = '" + mOrdenID + "' AND ID_matricula = " + mMatr;
                    PreparedStatement preparedStatement2 = conn.prepareStatement(query2);
                    preparedStatement2.executeUpdate();

                    sendPushOP(mOrdenID, mMatr, mEstado);
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateTask = null;
            //mProgress.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(MapaActivity.this,"Orden En Falso",Toast.LENGTH_SHORT).show();
                ((Activity)mContext).finish();
            } else {
                Toast.makeText(MapaActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdateTask = null;
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
        if(showRate) handler.removeCallbacks(runnable);
    }

/*    final int PEOPLE_EMPRESA = 1;
    final int PEOPLE_USUARIO = 2;
    private void showPeople(){
        //mProgress.setVisibility(VISIBLE);
        mGetPeopleTask = new GetPeople(PEOPLE_EMPRESA, 0);
        mGetPeopleTask.execute();
    }

    private GetPeople mGetPeopleTask;
    public class GetPeople extends AsyncTask<Void, Void, ArrayList<People>> {
        Boolean isSuccess = false;
        ArrayList<People> people = new ArrayList<People>();
        ArrayList<String> peopleStr = new ArrayList<String>();
        int mTipo, mEmpresa;

        GetPeople(int tipo, int empresa){
            mTipo = tipo;
            mEmpresa = empresa;
        }

        @Override
        protected ArrayList<People> doInBackground(Void... params) {
            String query = "SELECT DISTINCT id_razon AS id, r_social AS nombre FROM r_socialesapp ORDER BY r_social ASC";
            if(mTipo == PEOPLE_USUARIO){
                query = "SELECT DISTINCT Usuario_tusamovil.fk_cliente AS id, Usuario_tusamovil.usuario AS nombre " +
                        "FROM  agencias_app INNER JOIN r_socialesapp ON agencias_app.id_razon = r_socialesapp.id_razon \n" +
                        "INNER JOIN Usuario_tusamovil ON agencias_app.id_permiso = Usuario_tusamovil.fk_cliente \n" +
                        "WHERE agencias_app.id_razon = "+mEmpresa+" AND Usuario_tusamovil.tipo_cliente = 1";

                //SELECT DISTINCT Usuario_tusamovil.fk_cliente AS id, Usuario_tusamovil.usuario AS nombre
                //        FROM  agencias_app INNER JOIN r_socialesapp ON agencias_app.id_razon = r_socialesapp.id_razon
                //        INNER JOIN Usuario_tusamovil ON agencias_app.id_permiso = Usuario_tusamovil.fk_cliente
                //        WHERE agencias_app.id_razon in (23,1) AND Usuario_tusamovil.tipo_cliente = 1

            }
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()){
                        People persona = new People(rs);
                        people.add(persona);
                        peopleStr.add(persona.getNombre());
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return people;
        }

        @Override
        protected void onPostExecute(final ArrayList<People> people) {
            mGetPeopleTask = null;
            String title = "Compartir con Agencia";
            if(mTipo == PEOPLE_USUARIO) title = "Compartir con Usuario";
            String[] lista = {};
            AlertDialog.Builder b = new AlertDialog.Builder(MapaActivity.this);
            b.setTitle(title);
            b.setItems(peopleStr.toArray(lista), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    personaSeleccionada(people.get(which), mTipo);
                }

            });
            b.show();
        }

        @Override
        protected void onCancelled() {
            mGetPeopleTask = null;
        }
    }

    private void personaSeleccionada(People persona, int tipo){
        if(tipo == PEOPLE_EMPRESA){
            mGetPeopleTask = new GetPeople(PEOPLE_USUARIO, persona.getId());
            mGetPeopleTask.execute();
        }else{
            compartirSiNo(persona);
        }
    }

    private void compartirSiNo(final People persona){
        AlertDialog alertDialog = new AlertDialog.Builder(MapaActivity.this).create();
        alertDialog.setTitle("Compartir Traslado");
        alertDialog.setMessage("¿Esta seguro de compartir con "+persona.getNombre()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Compartir",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("QQQQ",""+persona.getId()+" | "+mOrden.getID());
                        mCompartidaTask = new SetCompartida(persona.getId(), mOrden.getID());
                        mCompartidaTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private SetCompartida mCompartidaTask;
    public class SetCompartida extends AsyncTask<Void, Void, Boolean> {
        private final int mUsuario;
        private final String mOrdenID;
        Boolean isSuccess = false;

        SetCompartida(int usuario, String orden) {
            mUsuario = usuario;
            mOrdenID = orden;
        }

        @Override
        protected void onPreExecute() {
        //mProgress.setVisibility(VISIBLE);
        }

@Override
protected Boolean doInBackground(Void... params) {
        try {
        Connection conn = DBConnection.CONN();
        String query = "UPDATE orden_status SET fk_compartido = '" + mUsuario + "' WHERE fk_orden = '" + mOrdenID + "' ";
        if (conn == null) {
        Log.i("MSSQLERROR","Error al conectar con SQL server");
        } else {
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.executeUpdate();
        isSuccess=true;
        }
        } catch (Exception ex) {
        Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
        isSuccess = false;
        }
        return isSuccess;
        }

@Override
protected void onPostExecute(final Boolean success) {
        mCompartidaTask = null;
        //mProgress.setVisibility(View.GONE);
        if (success) {
        Toast.makeText(MapaActivity.this,"Se compartio correctamente",Toast.LENGTH_SHORT).show();
        showShare = false;
        invalidateOptionsMenu();
        } else {
        Toast.makeText(MapaActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
        }
        }

@Override
protected void onCancelled() {
        mCompartidaTask = null;
        }
        }
        }
*/

    final int PEOPLE_EMPRESA = 1;
    private void showPeople(){
        mGetPeopleTask = new GetPeople(PEOPLE_EMPRESA, 0);
        mGetPeopleTask.execute();
    }
    private GetPeople mGetPeopleTask;
    public class GetPeople extends AsyncTask<Void, Void, ArrayList<People>> {
        Boolean isSuccess = false;
        ArrayList<People> people = new ArrayList<People>();
        ArrayList<String> peopleStr = new ArrayList<String>();
        ArrayList<Integer> peopleint = new ArrayList<Integer>();
        int mTipo, mEmpresa;
        GetPeople(int tipo, int empresa){
            mTipo = tipo;
            mEmpresa = empresa;
        }

        @Override
        protected ArrayList<People> doInBackground(Void... params) {
            String query = "SELECT DISTINCT id_razon AS id, r_social AS nombre FROM r_socialesapp ORDER BY r_social ASC";
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()){
                        People persona = new People(rs);
                        people.add(persona);
                        peopleStr.add(persona.getNombre());
                        peopleint.add(persona.getId());
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return people;
        }

        @Override
        protected void onPostExecute(final ArrayList<People> people) {
            mGetPeopleTask = null;
            String title = "Compartir con Agencia";
            String[] lista = {};
            final boolean[] checksAgencias = new boolean[peopleStr.size()];

            AlertDialog.Builder b = new AlertDialog.Builder(MapaActivity.this);
            b.setTitle(title);
            b.setCancelable(false);
            b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });
            b.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    for (int x = 0; x < checksAgencias.length; x++) {
                        if(checksAgencias[x]==true){
                            checks=true;
                        }
                    }
                    if(!checks){
                        Toast.makeText(MapaActivity.this, "Debe seleccionar una opción", Toast.LENGTH_SHORT).show();
                    }else{
                        listaAgencias = new ArrayList<Integer>();
                        for (int x = 0; x < checksAgencias.length; x++) {
                            if(checksAgencias[x]==true){
                                listaAgencias.add(peopleint.get(x));
                            }
                        }
                        String list = listaAgencias.toString();
                        String lista = list.substring(1,list.length()-1);
                        Log.i("AgenciasCheck",""+lista);
                        String query2 = "SELECT DISTINCT Usuario_tusamovil.fk_cliente AS id, Usuario_tusamovil.usuario AS nombre " +
                                "FROM  agencias_app INNER JOIN r_socialesapp ON agencias_app.id_razon = r_socialesapp.id_razon \n" +
                                "INNER JOIN Usuario_tusamovil ON agencias_app.id_permiso = Usuario_tusamovil.fk_cliente \n" +
                                "WHERE agencias_app.id_razon in ("+lista+") AND Usuario_tusamovil.tipo_cliente = 1";
                        //Log.i("BBBBBBBB",""+query2);
                        listaUsuarios = new ArrayList<Integer>();
                            try {
                                Connection conn2 = DBConnection.CONN();
                                if (conn2 == null) {
                                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                                } else {
                                    Statement stmt2 = conn2.createStatement();
                                    ResultSet rs2 = stmt2.executeQuery(query2);
                                    while (rs2.next()){
                                        People persona = new People(rs2);
                                        if(!listaUsuarios.contains(persona.getId())){
                                            listaUsuarios.add(persona.getId());
                                        }
                                    }
                                    isSuccess=true;
                                }
                            } catch (Exception ex) {
                                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query2);
                                isSuccess = false;
                            }
                            String idss = listaUsuarios.toString();
                            String idsss = idss.replace("[",",");
                            String idssss = idsss.replace("]",",");
                            String ids = idssss.replace(" ","");
                            Log.i("ARREGLO1",""+ids);
                            compartirSiNo(ids);
                    }
                }
            });
            b.setMultiChoiceItems(peopleStr.toArray(lista), checksAgencias, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                    checksAgencias[which] = isChecked;
                }
            });
            if(mTipo == PEOPLE_EMPRESA){
                final AlertDialog dialog2 = b.create();
                dialog2.show();
            }
        }

        @Override
        protected void onCancelled() {
            mGetPeopleTask = null;
        }
    }

    private void compartirSiNo(final String ids) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapaActivity.this);
            alertDialog.setTitle("Compartir Traslado");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("¿Esta seguro que deseas compartir?");
            alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                    bandera2=true;
                }
            });
            alertDialog.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog2 = new ProgressDialog(MapaActivity.this);
                    progressDialog2.setMessage("Compartiendo..."); // Mensaje
                    progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    progressDialog2.show(); // Display Progress Dialog
                    progressDialog2.setCancelable(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (bandera4) {
                                try {
                                    Thread.sleep(3000);
                                } catch (Exception e) {}
                            }
                        }
                    }).start();
                        mCompartidaTask = new SetCompartida(ids, mOrden.getID());
                        mCompartidaTask.execute((Void) null);
                    dialog.dismiss();
                    bandera2=true;
                }
            });
            if(bandera2) {
                final AlertDialog dialog3 = alertDialog.create();
                dialog3.show();
                ((AlertDialog) dialog3).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MapaActivity.this.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                ((AlertDialog) dialog3).getButton(
                                        AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        });
                    }
                }, 1000);
                bandera2 = false;
            }
    }

    private SetCompartida mCompartidaTask;
    public class SetCompartida extends AsyncTask<Void, Void, Boolean> {
        private final String mUsuarios;
        private final String mOrdenID;
        Boolean isSuccess = false;

        SetCompartida(String usuarios, String orden) {
            mUsuarios = usuarios;
            mOrdenID = orden;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                String query = "UPDATE orden_status SET compartido = '"+mUsuarios+"' WHERE fk_orden = '" + mOrdenID + "' ";
                Log.i("FINAL",query);
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCompartidaTask = null;
            if (success) {
                Toast.makeText(MapaActivity.this,"Se ha compartido correctamente",Toast.LENGTH_SHORT).show();
                progressDialog2.dismiss();
                showShare = false;
                invalidateOptionsMenu();
            } else {
                Toast.makeText(MapaActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mCompartidaTask = null;
        }
    }
}
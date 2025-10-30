package net.ddsmedia.tusa.tusamovil;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Orden;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapaGeneralActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String mUserStr;
    private int mType;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private Boolean mLogSaved = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_general);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mType = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapgen);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Globals.progressDialog.dismiss();

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Ubicaciones Mapa General");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.hide();

        //ProgressDialog
        Globals.progressDialog2 = new ProgressDialog(MapaGeneralActivity.this);
        Globals.progressDialog2.setMessage("Esta operación puede tomar varios segundos, Por favor espere."); // Mensaje
        Globals.progressDialog2.setTitle("Cargando..."); // Título
        Globals.progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        Globals.progressDialog2.show(); // Display Progress Dialog
        Globals.progressDialog2.setCancelable(true);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (checkConectivity()) {
            mMap = googleMap;

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25, -100), 5));
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            int offset = 0 * 100;
            List<String> param = new ArrayList<String>(Arrays.asList(String.valueOf(mUserInfo.getCliente()), String.valueOf(mUserInfo.getTipo()), mUserInfo.getEmail(), String.valueOf(offset)));

            String accion = "Consuta_OT_activas";

            String consulta = Globals.makeQuery(5, param.toArray(new String[param.size()]));

            //String consulta = "SELECT ID_orden, fk_matricula, estado, calificacion, fk_compartido, (SELECT CASE estado WHEN 1 THEN FORMAT(asignada,'dd/MM HH:mm') WHEN 2 THEN FORMAT(iniciada,'dd/MM HH:mm') WHEN 7 THEN FORMAT(s.origen,'dd/MM HH:mm') WHEN 8 THEN FORMAT(s.falla,'dd/MM HH:mm') WHEN 9 THEN FORMAT(s.pausa,'dd/MM HH:mm') WHEN 3 THEN FORMAT(en_sitio,'dd/MM HH:mm') END) AS fecha, No_chasis AS VIN, s.tipo_o AS tipo, (SELECT Nombre FROM Directorio WHERE ID_entidad = o.Origen) AS nomOrigen, (SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, (SELECT CONCAT(Nombres,' ', Ap_paterno,' ', Ap_materno) FROM Personal WHERE ID_matricula = fk_matricula) AS nombreOperador, ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula = fk_matricula AND Adicional = 0),0) AS celOperador, ISNULL((SELECT TOP 1 latitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND latitud != '0.0' ORDER BY fecha DESC),0) AS latitud, ISNULL((SELECT TOP 1 longitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND longitud != '0.0' ORDER BY fecha DESC),0) AS longitud, ISNULL((SELECT TOP 1 velocidad FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND CAST(velocidad as float) > 1 ORDER BY fecha DESC),0) AS velocidad, ISNULL((SELECT TOP 1 CONCAT(latitud,',',longitud) FROM geo_destino WHERE fk_cliente = Destino),'0,0') AS geoDestino, (SELECT CASE estado WHEN 1 THEN asignada WHEN 2 THEN iniciada WHEN 7 THEN s.origen WHEN 3 THEN en_sitio END) AS fechaf,  ISNULL(fotos,0) AS fotos FROM orden_status s, Orden_traslados o WHERE s.fk_orden = o.ID_orden AND estado != 4 AND estado != 5 AND estado != 6 AND o.id_empresa = '1' ORDER BY estado DESC, fechaf DESC";
            //String consulta = "SELECT ID_orden, fk_matricula, estado, calificacion, fk_compartido, (SELECT CASE estado WHEN 1 THEN FORMAT(asignada,'dd/MM HH:mm') WHEN 2 THEN FORMAT(iniciada,'dd/MM HH:mm') WHEN 7 THEN FORMAT(s.origen,'dd/MM HH:mm') WHEN 8 THEN FORMAT(s.falla,'dd/MM HH:mm') WHEN 9 THEN FORMAT(s.pausa,'dd/MM HH:mm') WHEN 3 THEN FORMAT(en_sitio,'dd/MM HH:mm') END) AS fecha, No_chasis AS VIN, s.tipo_o AS tipo, (SELECT Nombre FROM Directorio WHERE ID_entidad = o.Origen) AS nomOrigen, (SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, (SELECT CONCAT(Nombres,' ', Ap_paterno,' ', Ap_materno) FROM Personal WHERE ID_matricula = fk_matricula) AS nombreOperador, ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula = fk_matricula AND Adicional = 0),0) AS celOperador, ISNULL((SELECT TOP 1 latitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND latitud != '0.0' ORDER BY fecha DESC),0) AS latitud, ISNULL((SELECT TOP 1 longitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND longitud != '0.0' ORDER BY fecha DESC),0) AS longitud, ISNULL((SELECT TOP 1 velocidad FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND CAST(velocidad as float) > 1 ORDER BY fecha DESC),0) AS velocidad, ISNULL((SELECT TOP 1 CONCAT(latitud,',',longitud) FROM geo_destino WHERE fk_cliente = Destino),'0,0') AS geoDestino, (SELECT CASE estado WHEN 1 THEN asignada WHEN 2 THEN iniciada WHEN 7 THEN s.origen WHEN 3 THEN en_sitio END) AS fechaf,  ISNULL(fotos,0) AS fotos FROM orden_status s, Orden_traslados o WHERE s.fk_orden = o.ID_orden AND estado != 4 AND estado != 5 AND estado != 6 AND (fk_cliente = '9' OR fk_compartido = '9') ORDER BY estado DESC, fechaf DESC";

            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR", "Error al conectar con SQL server");
                } else {
                    if(!mLogSaved) {
                        String version = Globals.getVersion(getApplicationContext());
                        //String[] paramLog = {String.valueOf(mUserInfo.getMatricula()), mUserInfo.getEmail(), accion, version};
                        String[] paramLog = {String.valueOf(mUserInfo.getCliente()), mUserInfo.getEmail(), accion, version};
                        String queryLog = Globals.makeQuery(Globals.QUERY_ACCION, paramLog);
                        PreparedStatement preparedStatementLog = conn.prepareStatement(queryLog);
                        preparedStatementLog.executeUpdate();
                        mLogSaved = true;
                        Log.i("LOG_CLIENTE", queryLog);
                    }

                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(consulta);
                    Log.i("QUERYYY2", consulta);
                    int a = 0, b = 0;
                    while (rs.next()) {
                        b++;
                        Log.i("Total", "" + b);
                        Orden ordenNueva = new Orden(rs);
                        Log.i("__ORDENINFO2", ordenNueva.toJSON().toString());
                        //Log.i("POSyyyyyyyyyyyyy",ordenNueva.getLongitud()+","+ordenNueva.getLatitud());

                        if (!ordenNueva.getLongitud().equals("0") && !ordenNueva.getLatitud().equals("0")) {

                            a++;
                            Log.i("COUNT_22222", "" + a);
                            String msg = "";
                            if (ordenNueva.getEstado() == 1) {
                                msg = "Asignada" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 2) {
                                msg = "En Traslado" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 3) {
                                msg = "En Destino" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 4) {
                                msg = "Finalizada" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 5) {
                                msg = "Cancelada" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 6) {
                                msg = "Falso" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 7) {
                                msg = "En origen" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 8) {
                                msg = "Falla" + " " + ordenNueva.getFecha();
                            }
                            if (ordenNueva.getEstado() == 9) {
                                msg = "Resguardo" + " " + ordenNueva.getFecha();
                            }
                            long velo = Math.round(ordenNueva.getVelocidad());
                            if (velo > 85) velo = 85;
                            if (ordenNueva.getVelocidad() > 1)
                                msg += "<br>" + velo + " km/h <small>Aprox</small>";

                            mMap.addMarker(new MarkerOptions()
                                    .title("VIN " + ordenNueva.getVIN() + " \n" + ordenNueva.getID())
                                    .snippet(msg)
                                    .position(new LatLng(Double.parseDouble(ordenNueva.getLatitud()), Double.parseDouble(ordenNueva.getLongitud())))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                @Override
                                public View getInfoWindow(Marker arg0) {
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {

                                    LinearLayout info = new LinearLayout(MapaGeneralActivity.this);
                                    info.setOrientation(LinearLayout.VERTICAL);

                                    TextView title = new TextView(MapaGeneralActivity.this);
                                    title.setTextColor(Color.BLACK);
                                    title.setGravity(Gravity.CENTER);
                                    title.setTypeface(null, Typeface.BOLD);
                                    title.setText(marker.getTitle());

                                    TextView snippet = new TextView(MapaGeneralActivity.this);
                                    snippet.setTextColor(Color.GRAY);
                                    snippet.setText(Html.fromHtml(marker.getSnippet()));
                                    snippet.setGravity(Gravity.CENTER);

                                    info.addView(title);
                                    info.addView(snippet);
                                    return info;
                                }
                            });
                        }
                    }
                    Globals.progressDialog2.dismiss();
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR", "Excepcion MSSQL " + ex.getMessage() + " " + consulta);
            }
        }else {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    //Verificar conexión a internet
    public boolean checkConectivity(){
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
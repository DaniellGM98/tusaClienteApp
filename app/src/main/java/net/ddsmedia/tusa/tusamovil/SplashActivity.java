package net.ddsmedia.tusa.tusamovil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.messaging.FirebaseMessaging;
import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.jsoup.Jsoup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RequiresApi(api = 33)
public class SplashActivity extends Activity {

    private String mUserInfo;
    private View mControlsView;
    private boolean mVisible;

    private String mStoredUser;
    private int mStoredMatr;
    private String mStoredPass;
    private UserLoginTask mAuthTask;

    SharedPreferences loginData;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
        } else {
            Toast.makeText(SplashActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
        }

        loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mStoredMatr = loginData.getInt("matricula",0);
        mStoredUser = loginData.getString("username","");
        mStoredPass = loginData.getString("password","");

        checkPermisos();


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
    }

    private void doLogin(){
        mAuthTask = new UserLoginTask(mStoredUser, mStoredPass);
        mAuthTask.execute((Void) null);
    }

    private void goMain(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("user", mUserInfo);
        startActivity(intent);
        finish();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMatricula;
        private final String mPassword;
        String z = "";
        Boolean isSuccess = false;
        private Usuario mUsuario;

        UserLoginTask(String user, String password) {
            mMatricula = user;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                /*String query = "SELECT ID_entidad, Nombre, Razon_social, Contacto, Celular, Correo_electronico, " +
                                    "(SELECT temporal FROM Usuario_tusamovil WHERE fk_cliente  = '" + mMatricula + "') AS temporal, " +
                                    "ISNULL((SELECT COUNT(*) FROM orden_status WHERE estado < " + Globals.ORDEN_FINALIZADA + " AND " +
                                        "(SELECT Propietario FROM Orden_traslados WHERE ID_orden = fk_orden) = '" + mMatricula + "'),0) AS pendientes  " +
                                    "FROM Directorio WHERE id_entidad = '" + mMatricula + "' AND " +
                                    "(SELECT password FROM Usuario_tusamovil " +
                                            "WHERE activo = 1 AND fk_cliente = '" + mMatricula + "') = '"+ Globals.cryptPassword(mPassword) +"'";*/
                String[] param = {mMatricula, mPassword};
                String query = Globals.makeQuery(Globals.QUERY_LOGIN, param);
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mUsuario = new Usuario(rs);
                        mUserInfo = mUsuario.toJSON().toString();
                        Log.i("USERINFO",mUserInfo);
                        Globals.updInfo(mUsuario,loginData);
                        isSuccess=true;
                    }else{
                        Log.i("MSSQLERROR","No hay registro ");
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL");
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                //Toast.makeText(SplashActivity.this,"Bienvenido "+mUsuario.getRazon(),Toast.LENGTH_SHORT).show();
                if(mUsuario.getTemporal() == 0){
                    goMain();
                }else{
                    Intent intent = new Intent(getBaseContext(), PasswordActivity.class);
                    try {
                        intent.putExtra("user",mUsuario.toJSON().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("init",true);
                    startActivity(intent);
                    finish();
                }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/cli"+mStoredMatr);
                Globals.deleteInfo(loginData);
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void checkUpdate(){
        final String[] latestVersion = {""};
        String currentVersion = "";

        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;

        final String finalCurrentVersion = currentVersion;
        Thread downloadThread = new Thread() {
            public void run() {
                Looper.prepare();
                try{
                    // TODO: cambiar url de app en play store
                    /*latestVersion[0] = Jsoup
                            .connect(
                                    "https://play.google.com/store/apps/details?id="
                                            //+ "net.ddsmedia.tusa.tusamoviloperador&hl=en")
                                            + getPackageName() + "&hl=en")
                            .timeout(30000)
                            .userAgent(
                                    "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com").get()
                            //.select("div[itemprop=softwareVersion]").first()
                            //.select("span[class=htlgb]:eq(3)").first()
                            .select("span.htlgb").get(3)
                            .ownText();*/
                    latestVersion[0] = Jsoup
                            .connect("http://dds.media/getAppVersion.php?app="+ getPackageName())
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://dds.media").get()
                            .select("h1").first()
                            .ownText();
                    Log.i("VERSIONES", finalCurrentVersion +"::"+ latestVersion[0]);
                    if (!finalCurrentVersion.equalsIgnoreCase(latestVersion[0])){
                        //showUpdateDialog();
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);
                        builder.setTitle("Existe una nueva versión disponible");
                        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                        ("market://details?id="+getPackageName())));
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doLogin();
                            }
                        });

                        builder.setCancelable(false).show();
                    }else{
                        doLogin();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.i("EXCEPTION", "VERIFICA TU CONEXION A INTERNET");

                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);
                    builder.setTitle("No se detectó conexión de Datos o WiFi para conectarse a internet, intentalo más tarde");
                    builder.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //downloadThread.start();
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            //dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Cerrar App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    builder.setCancelable(false).show();
                }
                Looper.loop();
            }
        };

        downloadThread.start();
    }

    private void checkPermisos(){
        if (Build.VERSION.SDK_INT >= 33) {
            if (hasPermissionsAPI33(this, PERMISSIONSAPI33)) {
                if (mStoredUser.isEmpty()) {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mUserInfo = loginData.getString("info", "");
                    Log.i("INFO SHARED", mUserInfo);
                    //checkUpdate();
                    doLogin();
                }
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONSAPI33, PERMISSION_ALL);
            }
        }else{
            if (hasPermissions(this, PERMISSIONS)) {
                if (mStoredUser.isEmpty()) {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mUserInfo = loginData.getString("info", "");
                    Log.i("INFO SHARED", mUserInfo);
                    //checkUpdate();
                    doLogin();
                }
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    private final int PERMISSION_ALL = 1;
    private static String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
    };
    private static String[] PERMISSIONSAPI33 = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
    };
    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            //for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            //}
        }
        return true;
    }

    public static boolean hasPermissionsAPI33(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            //for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            //}
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permitido
                } else {
                    // No permitido
                    Toast.makeText(this,"Debe habilitar los permisos requeridos",Toast.LENGTH_LONG).show();
                }
                checkPermisos();
                return;
            }
        }
    }
}

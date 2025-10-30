package net.ddsmedia.tusa.tusamovil.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import net.ddsmedia.tusa.tusamovil.model.Orden;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.apache.commons.net.ftp.FTP;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Created by Ivan on 25/03/2017.
 */

public class Globals {

    //public static final String DB_IP = "40.86.91.134"; //original
    //public static final String DB_IP = "4.tcp.ngrok.io:14555"; //provisional
    public static final String DB_IP = "20.15.201.237"; //provisional new
    public static final String DB_NAME = "Tusa";
    public static final String DB_USER = "tusasa";
    public static final String DB_PASSWORD = "51s73m452019";

    /*
    public static final String DB_IP = "3.22.53.161:18892";
    public static final String DB_NAME = "Tusa";
    public static final String DB_USER = "tusasa";
    public static final String DB_PASSWORD = "51s73m452019";
     */

    /*public static final String DB_NAME = "Tusa2";
    public static final String DB_USER = "acceso";
    public static final String DB_PASSWORD = "sa123";*/

    public static final String FTP_SERVER = "vmtraunsqlsrv01.centralus.cloudapp.azure.com";
    public static final String FTP_USER = "localadmin";
    public static final String FTP_PASS = "A7m7r45L4d05.local";

    public static final int AN_HOUR = 1000 * 60 * 15;
    public static final String SMS_PANICO = "7711541170";

    public static ProgressDialog progressDialog, progressDialog2;

    /** ORDEN_STATUS **/
    public static final int ORDENLIST_TYPE_PENDIENTES = 2;
    public static final int ORDENLIST_TYPE_HISTORIAL = 3;
    public static final int ORDENLIST_TYPE_MAPA_GENERAL = 5;

    public static final int ORDEN_ASIGNADA = 1;
    public static final int ORDEN_INICIADA = 2;
    public static final int ORDEN_EN_SITIO = 3;
    public static final int ORDEN_FINALIZADA = 4;
    public static final int ORDEN_CANCELADA = 5;
    public static final int ORDEN_FALSO = 6;
    public static final int ORDEN_ORIGEN = 7;
    public static final int ORDEN_FALLA = 8;
    public static final int ORDEN_RESGUARDO = 9;

    /** ORDEN_TIPO **/
    public static final int ORDEN_NACIONAL = 1;
    public static final int ORDEN_LATINO = 2;
    public static final int ORDEN_LAREDO = 3;
    public static final int ORDEN_PANICO = 4;

    /** TIPO_NOTIFICACION **/
    public static final int NOTIFICATION_TYPE_ORDEN = 1;
    public static final int NOTIFICATION_TYPE_UPDATE = 2;
    public static final int NOTIFICATION_TYPE_ORDEN_FALLA = 3;
    public static final int NOTIFICATION_TYPE_SALUD = 4;

    /** TIPO CLIENTE **/
    public static final int CLIENTE_SOLICITA = 1; // campo fk_cliente de orden de traslado
    public static final int CLIENTE_SEGUNDO = 2; // campo compartido de orden de traslado (compartido)
    public static final int CLIENTE_PROPIETARIO = 3; // campo propietario de orden de translado
    public static final int CLIENTE_BASE = 4; // sahagun mty usuarios tusa
    public static final int CLIENTE_USUARIO = 5; // usuario tusa alta orden
    public static final int CLIENTE_ADMINIS = 6; // empleados de tusa para encuesta de salud


    public static final int QUERY_LOGIN = 1;
    public static final int QUERY_PENDIENTES = 2;
    public static final int QUERY_HISTORIAL = 3;
    public static final int QUERY_ACCION = 4;
    public static final int QUERY_MAPAGENERAL = 5;
    public static String makeQuery(int type, String[] args){
        String res = "";
        switch (type){
            case QUERY_LOGIN:
                res = "SELECT fk_cliente, usuario, email, temporal, tipo_cliente, " +
                                "ISNULL((SELECT COUNT(*) FROM orden_status WHERE estado != " + ORDEN_FINALIZADA + " AND " +
                                    "estado != " + ORDEN_CANCELADA + " AND estado != " + ORDEN_FALSO + " AND " +
                                    "(SELECT fk_cliente FROM Orden_traslados WHERE ID_orden = fk_orden) = u.fk_cliente),0) AS pendientes,  " +
                                "ISNULL((SELECT TOP 1 id FROM Estado_de_Salud WHERE ID_matricula = fk_matricula " +
                                        "AND CONVERT(CHAR(10), Fecha, 120) = CONVERT(CHAR(10), GETDATE(), 120)),0) AS salud, fk_matricula " +
                            "FROM Usuario_tusamovil u " +
                                "WHERE activo = 1  AND email = '" + args[0] + "' " +
                                    "AND password = '"+ Globals.cryptPassword(args[1]) +"'";
                break;
            case QUERY_PENDIENTES:
                res = "SELECT ID_orden, fk_matricula, estado, calificacion, fk_compartido, " +
                                "(SELECT CASE estado " +
                                    "WHEN 1 THEN FORMAT(asignada,'dd/MM HH:mm') " +
                                    "WHEN 2 THEN FORMAT(iniciada,'dd/MM HH:mm') " +
                                    "WHEN 7 THEN FORMAT(s.origen,'dd/MM HH:mm') " +
                                    "WHEN 8 THEN FORMAT(s.falla,'dd/MM HH:mm') " +
                                    "WHEN 9 THEN FORMAT(s.pausa,'dd/MM HH:mm') " +
                                    "WHEN 3 THEN FORMAT(en_sitio,'dd/MM HH:mm') END) AS fecha, " +
                                "No_chasis AS VIN, s.tipo_o AS tipo, " +
                                "(SELECT Nombre FROM Directorio WHERE ID_entidad = o.Origen) AS nomOrigen, " +
                                "(SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, " +
                                "(SELECT CONCAT(Nombres,' ', Ap_paterno,' ', Ap_materno) FROM Personal WHERE ID_matricula = fk_matricula) AS nombreOperador, " +
                                "ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula = fk_matricula AND Adicional = 0),0) AS celOperador, " +
                                "ISNULL((SELECT TOP 1 latitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND latitud != '0.0' ORDER BY fecha DESC),0) AS latitud, " +
                                "ISNULL((SELECT TOP 1 longitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND longitud != '0.0' ORDER BY fecha DESC),0) AS longitud, " +
                                "ISNULL((SELECT TOP 1 velocidad FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND CAST(velocidad as float) > 1 ORDER BY fecha DESC),0) AS velocidad, " +
                                "ISNULL((SELECT TOP 1 CONCAT(latitud,',',longitud) FROM geo_destino WHERE fk_cliente = Destino),'0,0') AS geoDestino, " +
                                "(SELECT CASE estado WHEN 1 THEN asignada WHEN 2 THEN iniciada WHEN 7 THEN s.origen WHEN 3 THEN en_sitio END) AS fechaf,  " +
                                "ISNULL(fotos,0) AS fotos " +
                            "FROM orden_status s, Orden_traslados o " +
                                "WHERE s.fk_orden = o.ID_orden AND fk_matricula!=0 AND estado != " + Globals.ORDEN_FINALIZADA + " AND " +
                                //"WHERE s.fk_orden = o.ID_orden AND estado != " + Globals.ORDEN_FINALIZADA + " AND " +
                                    "estado != " + Globals.ORDEN_CANCELADA + " AND estado != " + Globals.ORDEN_FALSO + " AND ";
                if(Integer.parseInt(args[1]) == CLIENTE_SOLICITA || Integer.parseInt(args[1]) == CLIENTE_SEGUNDO) {
                    //res += "(fk_cliente = '" + args[0] + "' OR fk_compartido = '" + args[0] + "') ORDER BY estado DESC, fechaf DESC";
                    res += "(fk_cliente = '" + args[0] + "' OR CHARINDEX(',"+args[0]+",',compartido)>0) ORDER BY estado DESC, fechaf DESC";

                }else if(Integer.parseInt(args[1]) == CLIENTE_PROPIETARIO){
                    res += "Propietario = '" + args[0] + "' ORDER BY estado DESC, fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_BASE){
                    res += "o.id_empresa = '" + args[0] + "' ORDER BY estado DESC, fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_USUARIO){
                    res += "o.id_usuario = '" + args[2] + "' ORDER BY estado DESC, fechaf DESC";
                }
                res += " OFFSET " + args[3] + " ROWS FETCH NEXT 100 ROWS ONLY";
                break;
            case QUERY_HISTORIAL:
                //res = "SELECT TOP 100 ID_orden, fk_matricula, estado, calificacion, " +
                res = "SELECT ID_orden, fk_matricula, estado, calificacion, fk_compartido,  " +
                                "(SELECT CASE estado " +
                                    "WHEN 4 THEN FORMAT(finalizada,'dd/MM HH:mm') " +
                                    "WHEN 6 THEN FORMAT(falso,'dd/MM HH:mm') END) AS fecha, " +
                                "No_chasis AS VIN, s.tipo_o AS tipo, " +
                                "(SELECT Nombre FROM Directorio WHERE ID_entidad = o.Origen) AS nomOrigen, " +
                                "(SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, " +
                                "(SELECT CONCAT(Nombres,' ', Ap_paterno,' ', Ap_materno) FROM Personal WHERE ID_matricula = fk_matricula) AS nombreOperador, " +
                                "ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula = fk_matricula AND Adicional = 0),0) AS celOperador, " +
                                "ISNULL((SELECT TOP 1 latitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND latitud != '0.0' ORDER BY fecha DESC),0) AS latitud, " +
                                "ISNULL((SELECT TOP 1 longitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND longitud != '0.0' ORDER BY fecha DESC),0) AS longitud, " +
                                "ISNULL((SELECT TOP 1 velocidad FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND CAST(velocidad as float) > 1 ORDER BY fecha DESC),0) AS velocidad, " +
                                "ISNULL((SELECT TOP 1 CONCAT(latitud,',',longitud) FROM geo_destino WHERE fk_cliente = Destino),'0,0') AS geoDestino, " +
                                "(SELECT CASE estado WHEN 4 THEN finalizada WHEN 6 THEN falso END) AS fechaf ,  " +
                                "ISNULL(fotos,0) AS fotos " +
                            "FROM orden_status s, Orden_traslados o " +
                                "WHERE s.fk_orden = o.ID_orden AND " ;

                if(args[4].equals("0")){
                    res += " CONVERT(CHAR(7), (SELECT CASE estado WHEN 4 THEN finalizada WHEN 6 THEN falso END), 120) = CONVERT(CHAR(7), GETDATE(), 120) AND ";
                }else if(args[4].equals("1")){
                    res += " CONVERT(CHAR(7), (SELECT CASE estado WHEN 4 THEN finalizada WHEN 6 THEN falso END), 120) = CONVERT(CHAR(7), DATEADD(month, -1, GETDATE()), 120) AND ";
                }else if(args[4].equals("2")){
                    res += " CONVERT(CHAR(7), (SELECT CASE estado WHEN 4 THEN finalizada WHEN 6 THEN falso END), 120) = CONVERT(CHAR(7), DATEADD(month, -2, GETDATE()), 120) AND ";
                }

                if(Integer.parseInt(args[5]) == 0){
                    res += "(estado = " + Globals.ORDEN_FINALIZADA + " OR estado = " + Globals.ORDEN_FALSO + ") AND ";
                }else if(Integer.parseInt(args[5]) == 1){
                    res += "estado = " + Globals.ORDEN_FINALIZADA + " AND ";
                }else if(Integer.parseInt(args[5]) == 2){
                    res += "estado = " + Globals.ORDEN_FALSO + " AND ";
                }
                if(Integer.parseInt(args[1]) == CLIENTE_SOLICITA || Integer.parseInt(args[1]) == CLIENTE_SEGUNDO) {
                    //res += "fk_cliente = '" + args[0] + "' ORDER BY fechaf DESC";
                    //res += "(fk_cliente = '" + args[0] + "' OR fk_compartido = '" + args[0] + "') ORDER BY fechaf DESC";
                    res += "(fk_cliente = '" + args[0] + "' OR CHARINDEX(',"+args[0]+",',compartido)>0) ORDER BY fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_PROPIETARIO){
                    res += "Propietario = '" + args[0] + "' ORDER BY fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_BASE){
                    res += "o.id_empresa = '" + args[0] + "' ORDER BY fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_USUARIO){
                    res += "o.id_usuario = '" + args[2] + "' ORDER BY fechaf DESC";
                }
                //res += " OFFSET " + args[3] + " ROWS FETCH NEXT 100 ROWS ONLY";
                break;
            case QUERY_ACCION:
                res = "INSERT INTO FechaSesionCliente (fk_cliente, email, Fcha_SesionClte, Accion, app_version) " +
                        "VALUES ('"+ args[0] +"', '"+ args[1] +"', GETDATE(), '"+ args[2] +"', '"+args[3]+"') ";
                break;

            case QUERY_MAPAGENERAL:
                res = "SELECT ID_orden, fk_matricula, estado, calificacion, fk_compartido, " +
                        "(SELECT CASE estado " +
                        "WHEN 1 THEN FORMAT(asignada,'dd/MM HH:mm') " +
                        "WHEN 2 THEN FORMAT(iniciada,'dd/MM HH:mm') " +
                        "WHEN 7 THEN FORMAT(s.origen,'dd/MM HH:mm') " +
                        "WHEN 8 THEN FORMAT(s.falla,'dd/MM HH:mm') " +
                        "WHEN 9 THEN FORMAT(s.pausa,'dd/MM HH:mm') " +
                        "WHEN 3 THEN FORMAT(en_sitio,'dd/MM HH:mm') END) AS fecha, " +
                        "No_chasis AS VIN, s.tipo_o AS tipo, " +
                        "(SELECT Nombre FROM Directorio WHERE ID_entidad = o.Origen) AS nomOrigen, " +
                        "(SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, " +
                        "(SELECT CONCAT(Nombres,' ', Ap_paterno,' ', Ap_materno) FROM Personal WHERE ID_matricula = fk_matricula) AS nombreOperador, " +
                        "ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula = fk_matricula AND Adicional = 0),0) AS celOperador, " +
                        "ISNULL((SELECT TOP 1 latitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND latitud != '0.0' ORDER BY fecha DESC),0) AS latitud, " +
                        "ISNULL((SELECT TOP 1 longitud FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND longitud != '0.0' ORDER BY fecha DESC),0) AS longitud, " +
                        "ISNULL((SELECT TOP 1 velocidad FROM geo_op WHERE fk_ot = fk_orden AND fk_op = fk_matricula AND CAST(velocidad as float) > 1 ORDER BY fecha DESC),0) AS velocidad, " +
                        "ISNULL((SELECT TOP 1 CONCAT(latitud,',',longitud) FROM geo_destino WHERE fk_cliente = Destino),'0,0') AS geoDestino, " +
                        "(SELECT CASE estado WHEN 1 THEN asignada WHEN 2 THEN iniciada WHEN 7 THEN s.origen WHEN 3 THEN en_sitio END) AS fechaf,  " +
                        "ISNULL(fotos,0) AS fotos " +
                        "FROM orden_status s, Orden_traslados o " +
                        "WHERE s.fk_orden = o.ID_orden AND estado != " + Globals.ORDEN_FINALIZADA + " AND " +
                        "estado != " + Globals.ORDEN_CANCELADA + " AND estado != " + Globals.ORDEN_FALSO + " AND ";
                if(Integer.parseInt(args[1]) == CLIENTE_SOLICITA || Integer.parseInt(args[1]) == CLIENTE_SEGUNDO) {
                    //res += "(fk_cliente = '" + args[0] + "' OR fk_compartido = '" + args[0] + "') ORDER BY estado DESC, fechaf DESC";
                    res += "(fk_cliente = '" + args[0] + "' OR CHARINDEX(',"+args[0]+",',compartido)>0) ORDER BY estado DESC, fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_PROPIETARIO){
                    res += "Propietario = '" + args[0] + "' ORDER BY estado DESC, fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_BASE){
                    res += "o.id_empresa = '" + args[0] + "' ORDER BY estado DESC, fechaf DESC";
                }else if(Integer.parseInt(args[1]) == CLIENTE_USUARIO){
                    res += "o.id_usuario = '" + args[2] + "' ORDER BY estado DESC, fechaf DESC";
                }
                //res += " OFFSET " + args[3] + " ROWS FETCH NEXT 100 ROWS ONLY";
                break;

        }
        return res;
    }

    public static String getVersion(Context context){
        String currentVersion = "";

        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(context.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;
        Log.i("VERSION",currentVersion);
        return currentVersion;
    }

    public static String cryptPassword(String pwd){
        String pass = getHash(pwd,"SHA1");
        pass = getHash(pass,"MD5");
        pass = new StringBuilder(pass).reverse().toString();

        return pass;
    }

    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public static final String BASE_URL = "http://app.blinkmensajeros.com/api/";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int TEN_MINUTES = 1000 * 60 * 10;

    public static final Integer MNU_HOME = 0;
    public static final Integer MNU_PENDIENTES = 1;
    public static final Integer MNU_HISTORY = 2;
    public static final Integer MNU_PASSWORD = 3;
    public static final Integer MNU_EXIT = 4;
    public static final Integer MNU_SALUD = 5;
    public static final Integer MNU_MAPA_GENERAL = 6;

    public static JSONObject getObject(String tagName, JSONObject jsonObject) throws JSONException {
        JSONObject jObj = jsonObject.getJSONObject(tagName);
        return jObj;
    }

    public static String getString(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getString(tagName);
    }

    public static Boolean getBoolean(String tagName, JSONObject jsonObject) throws JSONException {
        return jsonObject.getBoolean(tagName);
    }


    public static final void saveInfo(Usuario user, String password, SharedPreferences prefs) throws JSONException {
        SharedPreferences.Editor editor = prefs.edit();
        //editor.putInt("matricula", user.getMatricula());
        editor.putInt("matricula", user.getCliente());
        editor.putString("password", password);
        editor.putString("username", user.getEmail());
        //editor.putInt("tipo_orden", user.getPendientes());
        editor.putString("info", user.toJSON().toString());
        editor.commit();
    }

    public static void updInfo(Usuario user, SharedPreferences prefs) throws JSONException{
        SharedPreferences.Editor editor = prefs.edit();
        /*editor.putString("orden", user.getOrden());
        if(user.getOrden().equals("")){
            editor.putInt("tipo_orden", 0);
            editor.putString("orden_info","");
        }*/
        editor.putString("info", user.toJSON().toString());
        editor.commit();
    }

    public static void updInfoOrden(Orden orden, SharedPreferences prefs) throws JSONException{
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("orden", orden.getID());
        editor.putInt("tipo_orden", orden.getTipo());
        editor.putString("orden_info", orden.toJSON().toString());
        editor.commit();
    }

    public static final void deleteInfo(SharedPreferences prefs){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("matricula", 0);
        editor.putString("password", "");
        editor.putString("info", "");
        /*editor.putString("orden", "");
        editor.putString("orden_info", "");
        editor.putInt("tipo_orden", 0);*/
        editor.commit();
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getImageFromFTP(String filename, Context ctx){
        InputStream input;
        Bitmap myBitmap;
        File file = new File(ctx.getFilesDir(),filename+".jpg");
        //try {
            /*if(file.exists()){
                myBitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                //myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                //Log.i("ARCHIVO_GUARDADO",myBitmap.getByteCount()+file.getAbsolutePath());
                return myBitmap;
            }else{*/
                FTPClient con = new FTPClient();
                try {
                    con.connect(FTP_SERVER);
                    if (con.login(FTP_USER, FTP_PASS))
                    {
                        con.enterLocalPassiveMode();
                        con.setFileType(FTP.BINARY_FILE_TYPE);
                        con.changeWorkingDirectory("/Graficos/");

                        input = con.retrieveFileStream(filename+".jpg");
                        myBitmap = BitmapFactory.decodeStream(input);

                        String res = saveToInternalStorage(myBitmap,ctx,filename);
                        Log.v("Descargada Guardada", "ruta: "+res);

                        con.logout();
                        con.disconnect();
                        return myBitmap;
                    }
                } catch (IOException e) {
                    Log.d("FTP ERROR", "Error: could not connect to host " + Globals.FTP_SERVER + "::" + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            //}
        /*} catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        return null;
    }

    public static String saveToInternalStorage(Bitmap bitmapImage, Context ctx, String matr){
       File file = new File(ctx.getFilesDir(),matr+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    public static void sendPushOP(String orden, int matr, int status){
        String url = "https://trasladosuniversales.com.mx/app/sendPush.php?o="+orden+"&m="+matr+"&s="+status;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = null;
            inputStream = httpResponse.getEntity().getContent();
            String result = convertInputStreamToString(inputStream);

            Log.i("PUSH_CLIENTE",result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}

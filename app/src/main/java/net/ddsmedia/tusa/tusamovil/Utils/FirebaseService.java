package net.ddsmedia.tusa.tusamovil.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.ddsmedia.tusa.tusamovil.MapaActivity;
import net.ddsmedia.tusa.tusamovil.OrdenesActivity;
import net.ddsmedia.tusa.tusamovil.R;
import net.ddsmedia.tusa.tusamovil.SaludActivity;
import net.ddsmedia.tusa.tusamovil.model.Orden;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import static net.ddsmedia.tusa.tusamovil.Utils.Globals.NOTIFICATION_TYPE_ORDEN;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.NOTIFICATION_TYPE_ORDEN_FALLA;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.NOTIFICATION_TYPE_SALUD;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.NOTIFICATION_TYPE_UPDATE;

public class FirebaseService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    private Context mCtx;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        mCtx = getApplicationContext();
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
// aqui
            try {
                JSONObject data = new JSONObject(remoteMessage.getData());
                int tipo = data.getInt("tipo");
                if(tipo == NOTIFICATION_TYPE_ORDEN){
                    String ordenstr = data.getString("orden");
                    Orden orden = new Orden(new JSONObject(ordenstr));
                    sendOrdenNotification(orden);
                }else if(tipo == NOTIFICATION_TYPE_ORDEN_FALLA){
                    String ordenstr = data.getString("orden");
                    Orden orden = new Orden(new JSONObject(ordenstr));
                    sendOrdenNotificationFalla(orden);
                }else if(tipo == NOTIFICATION_TYPE_UPDATE){
                    sendUpdateNotification();
                }else if(tipo == NOTIFICATION_TYPE_SALUD){
                    sendSaludNotification(data.getInt("matr"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOrdenNotification(Orden orden) throws JSONException {
        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String userStr = loginData.getString("info","");

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        String title = "Orden de Traslado";
        String msg = "El VIN "+orden.getVIN()+" se encuentra "+orden.getEstadoStr();

        int tipo = NOTIFICATION_TYPE_ORDEN;
        Intent intent = new Intent(getApplicationContext(), MapaActivity.class);
        if(orden.getEstado() == Globals.ORDEN_FINALIZADA){
            tipo = Globals.ORDEN_FINALIZADA;
            title = "Calificar Orden de Traslado";
        }else if(orden.getEstado() == Globals.ORDEN_INICIADA){
            title = "Orden de Traslado Iniciada";
            msg = "El VIN "+orden.getVIN()+" ha iniciado su traslado";
        }else if(orden.getEstado() == Globals.ORDEN_ORIGEN){
            title = "Orden de Traslado "+orden.getVIN();
            msg = "El operador está en origen para recoger el VIN "+orden.getVIN();
            intent = new Intent(getApplicationContext(), OrdenesActivity.class);
            intent.putExtra("tipo",Globals.ORDENLIST_TYPE_PENDIENTES);
        }else if(orden.getEstado() == Globals.ORDEN_FALLA){
            tipo = NOTIFICATION_TYPE_ORDEN_FALLA;
            title = "Orden de Traslado "+orden.getVIN();
            msg = "Traslado en pausa por falla o desperfecto.";
        }

        intent.putExtra("user", user.toJSON().toString());
        intent.putExtra("orden", orden.toJSON().toString());
        showNotification(title, msg, intent, tipo);
    }

    public void sendOrdenNotificationFalla(Orden orden) throws JSONException {
        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String userStr = loginData.getString("info","");

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        String title = "Orden de Traslado "+orden.getVIN();
        String msg = "";

        Intent intent = new Intent(getApplicationContext(), MapaActivity.class);
        if(orden.getEstado() == Globals.ORDEN_FINALIZADA){
            msg = "Traslado finalizado por falla o desperfecto.";
        }else if(orden.getEstado() == Globals.ORDEN_INICIADA){
            msg = "Falla o desperfecto solucionado. Se reanuda el traslado.";
        }

        intent.putExtra("user", user.toJSON().toString());
        intent.putExtra("orden", orden.toJSON().toString());

        showNotification(title, msg, intent, NOTIFICATION_TYPE_ORDEN_FALLA);
    }

    public void sendUpdateNotification(){
        String title = "TUSA Móvil";
        String msg = "Hay una actualización disponible";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse
                ("market://details?id="+getPackageName()));

        showNotification(title, msg, intent, NOTIFICATION_TYPE_UPDATE);
    }

    public void sendSaludNotification(int matr) throws JSONException {
        String title = "TUSA Móvil";
        String msg = "Llena tu encuesta de Estado de Salud";

        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String userStr = loginData.getString("info","");

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        Intent intent = new Intent(getApplicationContext(), SaludActivity.class);
        intent.putExtra("user", user.toJSON().toString());
        intent.putExtra("userId", matr);
        intent.putExtra("salud", 0);

        showNotification(title, msg, intent, NOTIFICATION_TYPE_SALUD);
    }

    public static final int ID_SMALL_NOTIFICATION = 235;
    private static final String NOTIFICATION_CHANNEL_ID = "tusa_notification_channel";

    public void showNotification(String title, String message, Intent intent, int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotificationHelper", "Permission POST_NOTIFICATIONS not granted. Cannot show notification.");
                return;
            }
        }

        PendingIntent resultPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(mCtx, type, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            resultPendingIntent = PendingIntent.getActivity(mCtx, type, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_logo2)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Establecer prioridad

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificaciones TUSA", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notificaciones Traslados Universales");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 400, 200, 400});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(type, notification);
    }

    /*public void showNotification(String title, String message, Intent intent, int type){

        PendingIntent resultPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity
                    (mCtx, type, intent, PendingIntent.FLAG_IMMUTABLE);
        }else{
            resultPendingIntent = PendingIntent.getActivity
                    (mCtx, type, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //PendingIntent resultPendingIntent =
          //      PendingIntent.getActivity(
            //            mCtx,
              //          type,
                //        intent,
                  //      PendingIntent.FLAG_UPDATE_CURRENT
                //);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx,NOTIFICATION_CHANNEL_ID);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.ic_stat_logo2).setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        //if(type >= NOTIFICATION_TYPE_ORDEN_FALLA)
        //        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificaciones TUSA", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notificaciones Traslados Universales");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 400, 200, 400});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(type, notification);
    }*/
}

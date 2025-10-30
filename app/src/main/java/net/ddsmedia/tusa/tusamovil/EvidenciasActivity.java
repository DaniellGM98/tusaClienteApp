package net.ddsmedia.tusa.tusamovil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvidenciasActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String orden;

    private int tipoEvidencia;

    private int pagina = 0;
    private Boolean flag_loading = false;
    private Boolean todas = false;

    private int mMes = 0;
    private int mStat = 0;

    private List<String> imagePaths = new ArrayList<>();
    private String ftpServer = "vmtraunsqlsrv01.centralus.cloudapp.azure.com";
    private String user = "admin.traslados";
    private String password = "dL3@cNWmT7eopUw!mw4CrZKaNkp5nB";
    private String remoteDirectory = "/EvidenciasFotografica/";
    private List<String> remoteFiles = null;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    int evidenciasValidas = 0;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidencias);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        tipoEvidencia = b.getInt("tipoEvidencia");
        orden = b.getString("orden");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            //mOrdenStr = "";//mUserInfo.getOrden();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();
        bar.setTitle("Evidencia Fotográfica " + orden);

        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        remoteDirectory=remoteDirectory+orden+"/";

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columnas en el Grid

        if(tipoEvidencia==0){
            remoteFiles = Arrays.asList("1_"+orden+".jpg", "2_"+orden+".jpg", "3_"+orden+".jpg", "4_"+orden+".jpg", "5_"+orden+".jpg", "6_"+orden+".jpg", "7_"+orden+".jpg", "8_"+orden+".jpg");
        }else{
            remoteFiles = Arrays.asList("1_"+orden+".jpg", "2_"+orden+".jpg", "3_"+orden+".jpg", "4_"+orden+".jpg", "5_"+orden+".jpg", "6_"+orden+".jpg", "7_"+orden+".jpg", "8_"+orden+".jpg", "9_"+orden+".jpg", "10_"+orden+".jpg", "11_"+orden+".jpg", "12_"+orden+".jpg", "13_"+orden+".jpg", "14_"+orden+".jpg", "15_"+orden+".jpg", "16_"+orden+".jpg");
        }
        downloadImages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_evidencias, menu);
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
            case R.id.mnu_descarga:
                new SaveImagesToGalleryTask(this, imagePaths).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    private void downloadImages() {
        File localDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FTPImages");
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        for (String remoteFile : remoteFiles) {
            String localFilePath = new File(localDir, remoteFile).getAbsolutePath();
            imagePaths.add(localFilePath);
        }

        progressBar.setVisibility(View.VISIBLE);
        new DownloadImagesTask(this, ftpServer, user, password, remoteDirectory, remoteFiles, imagePaths).execute();
    }

    public void onImagesDownloaded() {
        progressBar.setVisibility(View.GONE);
        ImageAdapter adapter = new ImageAdapter(this, imagePaths);
        recyclerView.setAdapter(adapter);
    }

    private class SaveImagesToGalleryTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<EvidenciasActivity> activityReference;
        private List<String> imagePaths;

        SaveImagesToGalleryTask(EvidenciasActivity context, List<String> imagePaths) {
            activityReference = new WeakReference<>(context);
            this.imagePaths = imagePaths;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            EvidenciasActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            for (String imagePath : imagePaths) {
                File srcFile = new File(imagePath);
                File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), srcFile.getName());

                try (FileInputStream in = new FileInputStream(srcFile);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    // Actualiza la galería
                    activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)));
                } catch (IOException e) {
                    Log.e("SaveImages", "Error saving image to gallery", e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EvidenciasActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if(evidenciasValidas==0){
                // Obtener el LayoutInflater
                LayoutInflater inflater = getLayoutInflater();

                // Inflar el layout personalizado
                View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

                // Encontrar los elementos del layout personalizado
                ImageView toastIcon = layout.findViewById(R.id.toast_icon);
                TextView toastText = layout.findViewById(R.id.toast_text);

                // Configurar el mensaje del Toast
                toastText.setText("No hay evidencias fotográficas");

                // Opcional: cambiar el icono del Toast en tiempo de ejecución
                toastIcon.setImageResource(R.drawable.logoatm);

                // Crear el Toast
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);

                // Mostrar el Toast
                toast.show();
            }else{
                // Obtener el LayoutInflater
                LayoutInflater inflater = getLayoutInflater();

                // Inflar el layout personalizado
                View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

                // Encontrar los elementos del layout personalizado
                ImageView toastIcon = layout.findViewById(R.id.toast_icon);
                TextView toastText = layout.findViewById(R.id.toast_text);

                // Configurar el mensaje del Toast
                toastText.setText("Imágenes guardadas en la galería");

                // Opcional: cambiar el icono del Toast en tiempo de ejecución
                toastIcon.setImageResource(R.drawable.logoatm);

                // Crear el Toast
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);

                // Mostrar el Toast
                toast.show();
            }
        }
    }

    public class DownloadImagesTask extends AsyncTask<Void, Integer, Void> {

        private WeakReference<EvidenciasActivity> activityReference;
        private String ftpServer;
        private String user;
        private String password;
        private String remoteDirectory;
        private List<String> remoteFiles;
        private List<String> localFiles;

        public DownloadImagesTask(EvidenciasActivity context, String ftpServer, String user, String password, String remoteDirectory, List<String> remoteFiles, List<String> localFiles) {
            activityReference = new WeakReference<>(context);
            this.ftpServer = ftpServer;
            this.user = user;
            this.password = password;
            this.remoteDirectory = remoteDirectory;
            this.remoteFiles = remoteFiles;
            this.localFiles = localFiles;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(ftpServer);
                ftpClient.login(user, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                for (int i = 0; i < remoteFiles.size(); i++) {
                    String remoteFile = remoteDirectory + remoteFiles.get(i);
                    String localFilePath = localFiles.get(i);

                    // Obtener información del archivo remoto usando listFiles
                    FTPFile[] files = ftpClient.listFiles(remoteFile);
                    if (files != null && files.length == 1 && files[0].getSize() > 0) {
                        // El archivo existe y tiene un tamaño mayor a cero
                        File localFile = new File(localFilePath);
                        try (FileOutputStream fos = new FileOutputStream(localFile)) {
                            ftpClient.retrieveFile(remoteFile, fos);
                            evidenciasValidas++;
                        }
                        // Actualiza el progreso
                        publishProgress((int) ((i / (float) remoteFiles.size()) * 100));
                    }
                    //else {
                        //Log.i("infooooo", "El archivo no existe o tiene tamaño cero: " + remoteFile);
                    //}
                }

                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ex) {
                Log.e("FTP", "Error downloading images", ex);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            EvidenciasActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // Actualiza el ProgressBar (en este ejemplo, no estamos usando valores específicos de progreso)
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EvidenciasActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            activity.onImagesDownloaded();
        }
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        private Context context;
        private List<String> imagePaths;

        public ImageAdapter(Context context, List<String> imagePaths) {
            this.context = context;
            this.imagePaths = imagePaths;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imagePath = imagePaths.get(position);
            Glide.with(context).load(new File(imagePath)).into(holder.imageView);

            holder.itemView.setOnClickListener(v -> {
                //Toast.makeText(context, imagePath, Toast.LENGTH_SHORT).show();
                if(evidenciasValidas>0) {
                    Intent intenth = new Intent(EvidenciasActivity.this, FotoEvidenciaActivity.class);
                    intenth.putExtra("user", mUserStr);
                    intenth.putExtra("orden", orden);
                    intenth.putExtra("url", imagePath);
                    intenth.putExtra("position", position + 1);
                    intenth.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intenth);
                    EvidenciasActivity.this.overridePendingTransition(R.anim.open_next, R.anim.close_next);
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagePaths.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }

}

package net.ddsmedia.tusa.tusamovil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

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

public class FotoEvidenciaActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String orden;
    private String url;
    private int position;

    private SubsamplingScaleImageView imgFotoEvidencia;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotoevidencia);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        orden = b.getString("orden");
        url = b.getString("url");
        position = b.getInt("position");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            //mOrdenStr = "";//mUserInfo.getOrden();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();

        switch (position) {

            case 2:
                bar.setTitle("Unidad Trasera inicio "+ orden);
                break;
            case 3:
                bar.setTitle("Costado Derecho inicio "+ orden);
                break;
            case 4:
                bar.setTitle("Costado Izquierdo inicio "+ orden);
                break;
            case 5:
                bar.setTitle("Tablero inicio "+ orden);
                break;
            case 6:
                bar.setTitle("Urea inicio "+ orden);
                break;
            case 7:
                bar.setTitle("Combustible inicio "+ orden);
                break;
            case 8:
                bar.setTitle("Refrigerante inicio "+ orden);
                break;
            case 9:
                bar.setTitle("Unidad Frente final "+ orden);
                break;
            case 10:
                bar.setTitle("Unidad Trasera final "+ orden);
                break;
            case 11:
                bar.setTitle("Costado Derecho final "+ orden);
                break;
            case 12:
                bar.setTitle("Costado Izquierdo final "+ orden);
                break;
            case 13:
                bar.setTitle("Tablero final "+ orden);
                break;
            case 14:
                bar.setTitle("Urea final "+ orden);
                break;
            case 15:
                bar.setTitle("Combustible final "+ orden);
                break;
            case 16:
                bar.setTitle("Refrigerante final "+ orden);
                break;

            default:
                bar.setTitle("Unidad Frente inicio "+ orden);
                break;
        }

        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        imgFotoEvidencia = findViewById(R.id.imgFotoEvidencia);

        Uri uri = Uri.parse("file://"+url);

        // Cargar la imagen desde la URI
        imgFotoEvidencia.setImage(ImageSource.uri(uri));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_fotoevidencia, menu);
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

}

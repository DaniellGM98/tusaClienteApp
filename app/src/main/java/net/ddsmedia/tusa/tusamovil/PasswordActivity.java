package net.ddsmedia.tusa.tusamovil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PasswordActivity extends Activity {

    private EditText mPwdActual;
    private EditText mPwdNueva;
    private EditText mPwdNueva2;
    private ProgressBar mProgress;
    private Button mBtnSave;

    private JSONObject mJSONUserInfo;
    public String mUserStr;
    public String mPwdStr;
    private Usuario mUserInfo;
    private UpdatePassTask mUpdTask;
    private Boolean mInit;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle(R.string.title_activity_password);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mInit = b.getBoolean("init");
        SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mPwdStr = loginData.getString("password","");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPwdActual = (EditText) findViewById(R.id.txtPwdA);
        mPwdNueva = (EditText) findViewById(R.id.txtPwdN);
        mPwdNueva2 = (EditText) findViewById(R.id.txtPwdN2);

        mProgress = (ProgressBar) findViewById(R.id.pbarP);

        mBtnSave = (Button) findViewById(R.id.btnSaveP);
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });
    }

    private void attemptSave(){
        mPwdActual.setError(null);
        mPwdNueva.setError(null);
        mPwdNueva2.setError(null);

        String actual = mPwdActual.getText().toString();
        String nueva = mPwdNueva.getText().toString();
        String nueva2 = mPwdNueva2.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(actual)) {
            mPwdActual.setError(getString(R.string.error_field_required));
            focusView = mPwdActual;
            cancel = true;
        }else if (TextUtils.isEmpty(nueva)) {
            mPwdNueva.setError(getString(R.string.error_field_required));
            focusView = mPwdNueva;
            cancel = true;
        }else if (TextUtils.isEmpty(nueva2)) {
            mPwdNueva2.setError(getString(R.string.error_field_required));
            focusView = mPwdNueva2;
            cancel = true;
        }else if(!actual.equals(mPwdStr)){
            mPwdActual.setError(getString(R.string.error_pwd_actual));
            focusView = mPwdActual;
            cancel = true;
        }else if(!nueva.equals(nueva2)){
            mPwdNueva2.setError(getString(R.string.error_pwd_diff));
            focusView = mPwdNueva2;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mProgress.setVisibility(View.VISIBLE);
            mUpdTask = new UpdatePassTask(mUserInfo.getEmail(), nueva);
            mUpdTask.execute((Void) null);
        }
    }

    public class UpdatePassTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMail;
        private final String mPassword;
        String z = "";
        Boolean isSuccess = false;

        UpdatePassTask(String mail, String password) {
            mMail = mail;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    String query = "UPDATE Usuario_tusamovil SET temporal = 0, password = '" + Globals.cryptPassword(mPassword) + "' " +
                            "WHERE email = '" + mMail + "' AND fk_cliente = " + mUserInfo.getCliente();
                            //"WHERE email = '" + mMail + "' AND fk_cliente = " + mUserInfo.getMatricula();
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
            mUpdTask = null;
            mProgress.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(PasswordActivity.this,"Contraseña modificada correctamente",Toast.LENGTH_SHORT).show();
                try {
                    Globals.saveInfo(mUserInfo,mPassword,getSharedPreferences("loginData", Context.MODE_PRIVATE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(mInit) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("user", mUserStr);
                    startActivity(intent);
                }
                finish();
            } else {
                Toast.makeText(PasswordActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdTask = null;
        }
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

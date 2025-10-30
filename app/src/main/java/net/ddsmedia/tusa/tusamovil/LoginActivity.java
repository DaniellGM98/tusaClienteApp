package net.ddsmedia.tusa.tusamovil;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static android.view.View.VISIBLE;

public class LoginActivity extends Activity  {
    //public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private UserLoginTask mAuthTask = null;
    private Usuario mUsuario;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String mUserInfo;

    private TextView mForgot;
    private Button mEmailSignInButton;
    //private TextInputLayout mInputLayout;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Set up the login form.
        //mInputLayout = (TextInputLayout) findViewById(R.id.inputLayout);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEmailSignInButton.getText().equals(getString(R.string.action_sign_in))){
                    attemptLogin();
                }else if(mEmailSignInButton.getText().equals(getString(R.string.action_generate))){
                    attemptRecovery();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mForgot = (TextView) findViewById(R.id.lblForgot);
        mForgot.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(mForgot.getText().equals(getString(R.string.prompt_forgot))){
                   Toast.makeText(LoginActivity.this,"Ingrese su email registrado para enviar una contraseña aleatoria",Toast.LENGTH_LONG).show();
                   showRecovery();
               }else if(mForgot.getText().equals(getString(R.string.action_sign_in))){
                    showLogin();
               }
           }
        });
    }

    /*private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }*/


    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMatricula;
        private final String mPassword;
        String z = "";
        Boolean isSuccess = false;

        UserLoginTask(String email, String password) {
            mMatricula = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {mMatricula, mPassword};
            String query = Globals.makeQuery(Globals.QUERY_LOGIN, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mUsuario = new Usuario(rs);
                        Log.i("USERINFO",mUsuario.toJSON().toString());
                        mUserInfo = mUsuario.toJSON().toString();
                        isSuccess=true;

                        String version = Globals.getVersion(getApplicationContext());
                        String[] paramLog = {String.valueOf(mUsuario.getMatricula()), mUsuario.getEmail(), "Inicio de sesion", version};
                        String queryLog = Globals.makeQuery(Globals.QUERY_ACCION, paramLog);
                        Log.i("LOG_CLIENTE", queryLog);
                        PreparedStatement preparedStatement = conn.prepareStatement(queryLog);
                        preparedStatement.executeUpdate();
                    }else{
                        Log.i("MSSQLERROR","No hay registro \n"+query);
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL\n"+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //Toast.makeText(LoginActivity.this,"Bienvenido "+mUsuario.getRazon(),Toast.LENGTH_SHORT).show();
                SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
                try {
                    Globals.saveInfo(mUsuario, mPassword, loginData);
                    FirebaseMessaging.getInstance().subscribeToTopic("todoscli");

                    if(mUsuario.getTipo() == Globals.CLIENTE_PROPIETARIO) {
                        //FirebaseMessaging.getInstance().subscribeToTopic("propcli"+mUsuario.getMatricula());
                        FirebaseMessaging.getInstance().subscribeToTopic("propcli" + mUsuario.getCliente());
                    }else if(mUsuario.getTipo() == Globals.CLIENTE_BASE) {
                        //FirebaseMessaging.getInstance().subscribeToTopic("basecli"+mUsuario.getMatricula());
                        FirebaseMessaging.getInstance().subscribeToTopic("basecli" + mUsuario.getCliente());
                    }else if(mUsuario.getTipo() == Globals.CLIENTE_USUARIO) {
                        FirebaseMessaging.getInstance().subscribeToTopic("usercli" + mUsuario.getEmail());
                    }else if(mUsuario.getTipo() == Globals.CLIENTE_ADMINIS || mUsuario.getTipo() == Globals.CLIENTE_BASE ||mUsuario.getTipo() == Globals.CLIENTE_USUARIO) {
                        FirebaseMessaging.getInstance().subscribeToTopic("useradmin" + mUsuario.getMatricula());
                    }else {
                        FirebaseMessaging.getInstance().subscribeToTopic("cli" + mUsuario.getCliente());
                        //FirebaseMessaging.getInstance().subscribeToTopic("cli"+mUsuario.getMatricula());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(mUsuario.getTemporal() == 0){
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("user", mUserInfo);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(getBaseContext(), PasswordActivity.class);
                    intent.putExtra("user", mUserInfo);
                    intent.putExtra("init",true);
                    startActivity(intent);
                    finish();
                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void showLogin(){
        mForgot.setText(getString(R.string.prompt_forgot));
        mPasswordView.setVisibility(VISIBLE);
        mEmailSignInButton.setText(getString(R.string.action_sign_in));
        mEmailView.setHint(getString(R.string.prompt_user));
        //mInputLayout.setHint(getString(R.string.prompt_user));
        mEmailView.setInputType(InputType.TYPE_CLASS_TEXT);
        mEmailView.setText("");
        mPasswordView.setText("");
    }

    private void showRecovery(){
        mForgot.setText(getString(R.string.action_sign_in));
        mPasswordView.setVisibility(View.GONE);
        mEmailSignInButton.setText(getString(R.string.action_generate));
        mEmailView.setHint(getString(R.string.prompt_email));
        //mInputLayout.setHint(getString(R.string.prompt_email));
        mEmailView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mEmailView.setText("");
    }

    private void attemptRecovery() {
        if (mRecoveryTask != null) {
            return;
        }

        mEmailView.setError(null);
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mRecoveryTask = new SendRecovery(email);
            mRecoveryTask.execute((Void) null);
        }
    }

    private SendRecovery mRecoveryTask;
    public class SendRecovery extends AsyncTask<Void, Void, String> {
        private final String mMail;
        String z = "";

        SendRecovery(String mail) {
            mMail = mail;
        }

        @Override
        protected void onPreExecute() { Toast.makeText(LoginActivity.this,"Verificando",Toast.LENGTH_SHORT).show(); }

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://trasladosuniversales.com.mx/app/recoveryCli.php?m="+mMail;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
                InputStream inputStream = null;
                inputStream = httpResponse.getEntity().getContent();
                z = convertInputStreamToString(inputStream);

                Log.i("RECOVERY",z);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return z;
        }

        @Override
        protected void onPostExecute(final String msg) {
            mRecoveryTask = null;
            showProgress(false);
            if(msg.equals("TRUE")){
                Toast.makeText(LoginActivity.this,"Te enviamos una nueva contraseña a tu correo. Si no encuentras el mensaje, verifica tu bandeja de correo no deseado o spam.",Toast.LENGTH_LONG).show();
                showLogin();
            }else if(msg.equals("FALSE")){
                mEmailView.setError("Email no registrado");
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mRecoveryTask = null;
            showProgress(false);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}

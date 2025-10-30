package net.ddsmedia.tusa.tusamovil.Utils;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Ivan on 17/03/2017.
 */

public class DBConnection {

    static String classs = "net.sourceforge.jtds.jdbc.Driver";

    @SuppressLint("NewApi")
    public static final Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + Globals.DB_IP + ";"
                    + "databaseName=" + Globals.DB_NAME + ";user=" + Globals.DB_USER + ";password="
                    + Globals.DB_PASSWORD + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }

}

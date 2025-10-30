package net.ddsmedia.tusa.tusamovil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamovil.Utils.DBConnection;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.model.Orden;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

import static android.view.View.VISIBLE;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.CLIENTE_BASE;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.CLIENTE_USUARIO;
import static net.ddsmedia.tusa.tusamovil.Utils.Globals.sendPushOP;

public class OrdenesActivity extends Activity implements SearchView.OnQueryTextListener {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    //private String mOrdenStr;
    private ListView listView;

    private String[] arrEstados;

    private ArrayList<Orden> arrOrdenes;
    private OrdenesAdapter adapter;

    private GetOrdenesTask mGetOrdenesTask = null;
    private int mType;
    private View mProgress;
    private TextView mNoOrdenes;

    private int pagina = 0;
    private Boolean flag_loading = false;
    private Boolean todas = false;

    private View filtros;
    private Spinner mMeses;
    private Spinner mStatus;

    private int mMes = 0;
    private int mStat = 0;

    private int tipoEvidencia = 0;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordenes);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mType = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            //mOrdenStr = "";//mUserInfo.getOrden();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        arrEstados = getResources().getStringArray(R.array.orden_status);

        listView = (ListView) findViewById(R.id.lstOrdenes);

        filtros = findViewById(R.id.filtros);
        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_pend);
        if(mType == Globals.ORDENLIST_TYPE_HISTORIAL){
            bar.setTitle(R.string.activity_hist);
            tipoEvidencia=1;
            filtros.setVisibility(VISIBLE);

            Resources r = getApplicationContext().getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    38,
                    r.getDisplayMetrics()
            );
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) listView
                    .getLayoutParams();
            mlp.setMargins(0, px, 0, 0);

            mMeses = (Spinner) findViewById(R.id.spMes);
            ArrayAdapter<CharSequence> adapterMes = ArrayAdapter.createFromResource(this,
                    R.array.hist_meses, android.R.layout.simple_spinner_item);
            adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mMeses.setAdapter(adapterMes);
            mMeses.setSelection(0,false);
            mMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView,
                                           View selectedItemView, int position, long id) {
                    mMes = position;
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    //mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula());
                    mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getCliente());
                    Log.i("GET_ORDENES","Select Meses seleccionado");
                    mGetOrdenesTask.execute();
                }

                public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                }
            });

            mStatus = (Spinner) findViewById(R.id.spEdo);
            ArrayAdapter<CharSequence> adapterEdo = ArrayAdapter.createFromResource(this,
                    R.array.hist_status, android.R.layout.simple_spinner_item);
            adapterEdo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mStatus.setAdapter(adapterEdo);
            //mStatus.setSelection(0,false);
            mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView,
                                           View selectedItemView, int position, long id) {
                    mStat = position;
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    //mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula());
                    mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getCliente());
                    Log.i("GET_ORDENES","Select Status seleccionado");
                    mGetOrdenesTask.execute();
                }

                public void onNothingSelected(AdapterView<?> arg0) {// do nothing
                }
            });
        }

        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mProgress = findViewById(R.id.pbOrdenes);
        mNoOrdenes = (TextView) findViewById(R.id.lblNoOrdenes);

        arrOrdenes = new ArrayList<Orden>();
        adapter = new OrdenesAdapter(this,arrOrdenes);

        //listView = (ListView) findViewById(R.id.lstOrdenes);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Orden orden = (Orden) parent.getItemAtPosition(position);

                if(orden.getEstado() > Globals.ORDEN_ASIGNADA &&
                        Double.parseDouble(orden.getLatitud()) != 0 && Double.parseDouble(orden.getLongitud()) != 0 &&
                        orden.getEstado() != Globals.ORDEN_FALSO){
                    try {
                        Intent intent = new Intent(getApplicationContext(), MapaActivity.class);
                        intent.putExtra("user", mUserStr);
                        intent.putExtra("orden", orden.toJSON().toString());
                        intent.putExtra("tipoEvidencia", tipoEvidencia);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        //getApplicationContext().overridePendingTransition (R.anim.open_next, R.anim.close_next);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    showInfo(orden, position);
                }
            }
        });

        if(mType == Globals.ORDENLIST_TYPE_PENDIENTES){
            //mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula());
            mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getCliente());
            mGetOrdenesTask.execute();
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0){
                    if(flag_loading == false && totalItemCount > 99 && !todas){
                        flag_loading = true;
                        pagina++;
                        //mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula());
                        mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getCliente());
                        mGetOrdenesTask.execute();
                    }
                }
            }
        });
    }

    private void showInfo(final Orden orden, final int position){
        AlertDialog alertDialog = new AlertDialog.Builder(OrdenesActivity.this).create();

        String msg = "Operador\n" + orden.getNombreOperador()+"\n"+orden.getCelOperador()+"\n\n";
        msg += orden.getEstadoStr() + " "+orden.getFecha();

        alertDialog.setTitle("VIN "+orden.getVIN());
        alertDialog.setMessage(msg);

        if(orden.getEstado() == Globals.ORDEN_ASIGNADA && mUserInfo.getTipo() > Globals.CLIENTE_PROPIETARIO){
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancelar Orden",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            stopSiNo(orden, position);
                        }
                    });
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cerrar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void stopSiNo(final Orden orden, final int ordenIndex){
        AlertDialog alertDialog = new AlertDialog.Builder(OrdenesActivity.this).create();
        alertDialog.setTitle("Cancelar Orden");
        alertDialog.setMessage("¿Esta seguro de Cancelar la orden "+orden.getID()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUpdTask = new UpdateOrden(Globals.ORDEN_CANCELADA, orden.getID(), orden.getOperador(), orden);
                        mUpdTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private UpdateOrden mUpdTask;
    public class UpdateOrden extends AsyncTask<Void, Void, Boolean> {
        private final int mEstado;
        private final int mMatr;
        private final Orden mIndice;
        private final String mOrdenID;
        String z = "";
        Boolean isSuccess = false;

        UpdateOrden(int estado, String orden, int matr, Orden pos) {
            mEstado = estado;
            mOrdenID = orden;
            mMatr = matr;
            mIndice = pos;
        }

        @Override
        protected void onPreExecute() { mProgress.setVisibility(VISIBLE); }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                String field = "cancelada";
                String query = "UPDATE orden_status SET estado = '" + mEstado + "', " + field + " = GETDATE() " +
                        "WHERE fk_orden = '" + mOrdenID + "' ";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Log.i("QUERYy",""+query);
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;

                    String query2 = "UPDATE Orden_traslados SET Traslado_cancelado = 1, Fecha_cancelacion = GetDate() " +
                            "WHERE ID_orden = '" + mOrdenID + "' AND ID_matricula = " + mMatr;
                    Log.i("QUERYy2",""+query2);
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
            mUpdTask = null;
            mProgress.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(OrdenesActivity.this,"Orden Cancelada",Toast.LENGTH_SHORT).show();
                adapter.remove(mIndice);
                adapter.notifyDataSetChanged();
                searchView.setQuery("", false);
                searchView.clearFocus();
                //searchView.setIconified(true);
            } else {
                Toast.makeText(OrdenesActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdTask = null;
        }
    }



    public static class OrdenesAdapter extends ArrayAdapter<Orden> implements Filterable{

        private static class ViewHolder {
            TextView lblEstado;
            TextView lblVin;
            TextView lblOrigen;
            TextView lblDestino;
            TextView lblOt;
            TextView lblFecha;
        }

        public ArrayList<Orden> ordenes;
        public ArrayList<Orden> filteredOrdenes;
        private OrdenFilter ordenFilter;

        public OrdenesAdapter(Context context, ArrayList<Orden> ordenes){
            super(context,R.layout.orden_item,ordenes);
            this.ordenes = ordenes;
            this.filteredOrdenes = ordenes;

            getFilter();
        }

        @Override
        public int getCount() {
            return filteredOrdenes.size();
        }

        @Override
        public Orden getItem(int i) {
            return filteredOrdenes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Orden orden = getItem(position);

            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.orden_item, parent, false);

                viewHolder.lblVin = (TextView) convertView.findViewById(R.id.lblVini);
                viewHolder.lblEstado = (TextView) convertView.findViewById(R.id.lblEdoi);
                viewHolder.lblOrigen = (TextView) convertView.findViewById(R.id.lblOrigeni);
                viewHolder.lblDestino = (TextView) convertView.findViewById(R.id.lblDestinoi);
                viewHolder.lblOt = (TextView) convertView.findViewById(R.id.lblOti);
                viewHolder.lblFecha = (TextView) convertView.findViewById(R.id.lblFei);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.lblVin.setText(orden.getVIN());
            viewHolder.lblEstado.setText(orden.getEstadoStr());
            viewHolder.lblOt.setText(orden.getID());
            viewHolder.lblFecha.setText(orden.getFecha());
            viewHolder.lblEstado.setTextColor(getContext().getResources().getColor(android.R.color.primary_text_light));
            viewHolder.lblFecha.setTextColor(getContext().getResources().getColor(android.R.color.primary_text_light));
            if(orden.getEstado() == Globals.ORDEN_EN_SITIO || orden.getEstado() == Globals.ORDEN_FINALIZADA) {
                viewHolder.lblEstado.setTextColor(getContext().getResources().getColor(R.color.success));
                viewHolder.lblFecha.setTextColor(getContext().getResources().getColor(R.color.success));
            }else if(orden.getEstado() == Globals.ORDEN_FALSO || orden.getEstado() == Globals.ORDEN_FALLA) {
                viewHolder.lblEstado.setTextColor(Color.RED);
                viewHolder.lblFecha.setTextColor(Color.RED);
            }
            viewHolder.lblOrigen.setText(orden.getOrigen());
            viewHolder.lblDestino.setText(orden.getDestino());

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if(ordenFilter == null){
                ordenFilter = new OrdenFilter();
            }
            return ordenFilter;
        }

        private class OrdenFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Orden> tempList = new ArrayList<Orden>();
                if(constraint != null && ordenes!=null) {

                    for(Orden orden : ordenes){
                        if(orden.getVIN().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                                orden.getID().toLowerCase().contains(constraint.toString().toLowerCase())){
                            tempList.add(orden);
                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }else {
                    filterResults.count = ordenes.size();
                    filterResults.values = ordenes;
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                filteredOrdenes = (ArrayList<Orden>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    private Boolean mLogSaved = false;
    public class GetOrdenesTask extends AsyncTask<Void, Void, ArrayList<Orden>> {
        private final int mMatricula;
        String z = "";
        Boolean isSuccess = false;
        ArrayList<Orden> ordenes = new ArrayList<Orden>();

        GetOrdenesTask(int matricula) {
            mMatricula = matricula;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            mNoOrdenes.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<Orden> doInBackground(Void... params) {
            int offset = pagina * 100;
            List<String> param = new ArrayList<String>(Arrays.asList(String.valueOf(mMatricula), String.valueOf(mUserInfo.getTipo()), mUserInfo.getEmail(), String.valueOf(offset)));
            //String[] param = {String.valueOf(mMatricula), String.valueOf(mUserInfo.getTipo()), mUserInfo.getEmail(), String.valueOf(offset)};
            String accion = "Consuta_OT_activas";
            if(mType == Globals.ORDENLIST_TYPE_HISTORIAL){
                param.add(String.valueOf(mMes));
                param.add(String.valueOf(mStatus.getSelectedItemPosition()));
                accion = "Consuta_OT_Finalizadas";
            }
            String query = Globals.makeQuery(mType, param.toArray(new String[param.size()]));
            Log.i("query activos", query);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
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
                    ResultSet rs = stmt.executeQuery(query);
                    Log.i("QUERYYY",query);
                    int a=0;
                    while (rs.next()){
                        Orden ordenNueva = new Orden(rs);
                        ordenNueva.setEstadoStr(arrEstados[ordenNueva.getEstado()]);
                        ordenes.add(ordenNueva);
                        Log.i("ORDENINFO",ordenNueva.toJSON().toString());
                        if(!ordenNueva.getLongitud().equals("0") && !ordenNueva.getLatitud().equals("0")) {
                            a++;
                            Log.i("COUNTCONCOORDENADAS",""+a);
                        }
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return ordenes;
        }

        @Override
        protected void onPostExecute(ArrayList<Orden> ordens) {
            super.onPostExecute(ordens);
            flag_loading = false;
            Log.i("ORDENES RES",ordens.size()+" ordenes encontradas. PAGINA: "+pagina);
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
            if(ordens.size() > 0){
                //adapter.clear();
                adapter.addAll(ordens);
                adapter.notifyDataSetChanged();
                if(ordens.size() < 100 || mType == Globals.ORDENLIST_TYPE_HISTORIAL) todas = true;
            }else{
                flag_loading = true;
                Toast.makeText(OrdenesActivity.this,"No hay ordenes",Toast.LENGTH_SHORT).show();
                mNoOrdenes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            flag_loading = false;
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
        }
    }

    private SearchView searchView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mnu_ordenes, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.orden_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

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

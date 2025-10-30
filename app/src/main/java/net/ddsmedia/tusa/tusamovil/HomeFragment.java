package net.ddsmedia.tusa.tusamovil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamovil.Utils.AdapaterGridView;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.Utils.GridViewItem;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnItemClickListener {
    GridView gridview;
    AdapaterGridView gridviewAdapter;
    ArrayList<GridViewItem> data = new ArrayList<GridViewItem>();

    private JSONObject mJSONUserInfo;
    public String mUserStr;
    private Usuario mUserInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        Bundle b = getArguments();
        mUserStr = b.getString("user");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        gridview = (GridView) v.findViewById(R.id.gridView1);
        gridview.setOnItemClickListener(this);

        if(mUserInfo.getTipo() != Globals.CLIENTE_ADMINIS){
            data.add(new GridViewItem(getResources().getString(R.string.title_pendientes), getResources().getDrawable(R.drawable.ic_pendientes), Globals.MNU_PENDIENTES));
            data.add(new GridViewItem(getResources().getString(R.string.title_historial), getResources().getDrawable(R.drawable.ic_historial), Globals.MNU_HISTORY));
            data.add(new GridViewItem(getResources().getString(R.string.title_mapa_general), getResources().getDrawable(R.drawable.ic_mapageneral), Globals.MNU_MAPA_GENERAL));
        }
        //if(mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS){
        if(mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS || mUserInfo.getTipo() == Globals.CLIENTE_BASE || mUserInfo.getTipo() == Globals.CLIENTE_USUARIO) {
            data.add(new GridViewItem(getResources().getString(R.string.activity_salud), getResources().getDrawable(R.drawable.ic_salud), Globals.MNU_SALUD));
        }

        setDataAdapter();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
    }

    // Set the Data Adapter
    private void setDataAdapter() {
        gridviewAdapter = new AdapaterGridView(getActivity(), R.layout.fragment_list_item, data);
        gridview.setAdapter(gridviewAdapter);
    }

    @Override
    public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long id) {
        Integer action = data.get(+position).getMnu_option();
        if (action == Globals.MNU_PENDIENTES){
            //startActivity(new Intent(getActivity(), ActivityCategoryList.class));
            Intent intentp = new Intent(getActivity(), OrdenesActivity.class);
            intentp.putExtra("tipo",Globals.ORDENLIST_TYPE_PENDIENTES);
            intentp.putExtra("user", mUserStr);
            intentp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intentp);
            getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
        }
        else if (action == Globals.MNU_HISTORY){
            //startActivity(new Intent(getActivity(), ActivityCart.class));
            Intent intenth = new Intent(getActivity(), OrdenesActivity.class);
            intenth.putExtra("tipo",Globals.ORDENLIST_TYPE_HISTORIAL);
            intenth.putExtra("user", mUserStr);
            intenth.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intenth);
            getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
        }
        else if (action == Globals.MNU_MAPA_GENERAL){
                //ProgressDialog
                Globals.progressDialog = new ProgressDialog(getActivity());
                Globals.progressDialog.setMessage("Esta operación puede tomar varios segundos, Por favor espere."); // Mensaje
                Globals.progressDialog.setTitle("Cargando..."); // Título
                Globals.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                Globals.progressDialog.show(); // Display Progress Dialog
                Globals.progressDialog.setCancelable(true);
            if (checkConectivity()) {

                Intent intentmg = new Intent(getActivity(), MapaGeneralActivity.class);
                intentmg.putExtra("tipo",Globals.ORDENLIST_TYPE_HISTORIAL);
                intentmg.putExtra("user", mUserStr);
                intentmg.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentmg);
                getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }else{
                Globals.progressDialog.dismiss();
                Toast.makeText(getActivity(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
        else if (action == Globals.MNU_SALUD){
            Intent intentsa = new Intent(getActivity(), SaludActivity.class);
            intentsa.putExtra("user", mUserStr);
            intentsa.putExtra("userId", mUserInfo.getMatricula());
            intentsa.putExtra("salud", mUserInfo.getSalud());
            intentsa.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intentsa);
            getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
        }
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

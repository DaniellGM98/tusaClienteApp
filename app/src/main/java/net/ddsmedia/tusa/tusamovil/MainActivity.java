package net.ddsmedia.tusa.tusamovil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import net.ddsmedia.tusa.tusamovil.Utils.AdapterNavDrawerList;
import net.ddsmedia.tusa.tusamovil.Utils.Globals;
import net.ddsmedia.tusa.tusamovil.Utils.NavDrawerItem;
import net.ddsmedia.tusa.tusamovil.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private AdapterNavDrawerList adapter;

	// declare dbhelper and adapter object
	//static DBHelper dbhelper;
	//AdapterMainMenu mma;

	private String userData;
	private JSONObject mJSONUserInfo;
	public String mUserStr;
	private Usuario mUserInfo;
	private Bundle bundle;
	private String mNombreUsuario;

	private boolean showFotos = false;

	//private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

	@SuppressLint("SourceLockedOrientationActivity")
	@SuppressWarnings("ResourceType")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_drawer_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Bundle b = getIntent().getExtras();
		mUserStr = b.getString("user");

		if (mUserStr == null) {
			SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
			mUserStr = loginData.getString("info", "");
		}

		bundle = new Bundle();
		bundle.putString("user", mUserStr);

		mTitle = getTitle();
		try {
			mJSONUserInfo = new JSONObject(mUserStr);
			mUserInfo = new Usuario(mJSONUserInfo);
			mTitle = mDrawerTitle = mUserInfo.getNombre();

			Log.i("USER_ID", mUserInfo.getMatricula() + " :: salud: "+mUserInfo.getSalud()+" tipo "+mUserInfo.getTipo());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (mUserInfo.getTipo() == Globals.CLIENTE_BASE || mUserInfo.getTipo() == Globals.CLIENTE_USUARIO)
			showFotos = true;

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons3);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		mDrawerLayout.setDrawerShadow(R.drawable.navigation_drawer_shadow, GravityCompat.START);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), Globals.MNU_HOME));
		if(mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS || mUserInfo.getTipo() == Globals.CLIENTE_BASE || mUserInfo.getTipo() == Globals.CLIENTE_USUARIO) {
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), Globals.MNU_SALUD));
		}
		if (mUserInfo.getTipo() != Globals.CLIENTE_ADMINIS){
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), Globals.MNU_PENDIENTES));
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), Globals.MNU_HISTORY));
			navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1), Globals.MNU_MAPA_GENERAL));
		}
		//if(mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS) {
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), Globals.MNU_PASSWORD));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1), Globals.MNU_EXIT));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new AdapterNavDrawerList(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		ActionBar bar = getActionBar();
		//bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));

		// get screen device width and height
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// checking internet connection
		if (!Globals.isNetworkAvailable(MainActivity.this)) {
			Toast.makeText(MainActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
		}

		//mma = new AdapterMainMenu(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav
																								// menu
																								// toggle
																								// icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

		//checkNotificationPermission();

		////
		/*if((mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS || mUserInfo.getTipo() == Globals.CLIENTE_BASE
				|| mUserInfo.getTipo() == Globals.CLIENTE_USUARIO) && mUserInfo.getSalud() == 0)
			displayView(1);*/

		/*FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							//Log.w(TAG, "Fetching FCM registration token failed", task.getException());
							return;
						}
						// Get new FCM registration token
						String token = task.getResult();
						Log.i("tokenFIRE",""+token);
					}
				});*/
	}

	/*private void checkNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// El permiso fue concedido, puedes proceder con la funcionalidad de notificaciones
			} else {
				// El permiso fue denegado, informar al usuario
				Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
			}
		}
	}*/

	@Override
	protected void onResume() {
		super.onResume();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("D");
		String formattedDate = df.format(calendar.getTime());
		int a = Integer.parseInt(formattedDate);

		SharedPreferences preferences;
		preferences = getSharedPreferences("EncuestaSalud", Context.MODE_PRIVATE);
		int b = preferences.getInt("diaEncuesta", 0);

		if((mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS || mUserInfo.getTipo() == Globals.CLIENTE_BASE
				|| mUserInfo.getTipo() == Globals.CLIENTE_USUARIO) && mUserInfo.getSalud() == 0){
			if(a!=b){
				displayView(1);
			}
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//dbhelper.deleteAllData();
		//dbhelper.close();
		finish();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);
	}

	/**
	 * Slide menu item click listener
	 */
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mnu_fotos, menu);

		MenuItem mnuFotos = menu.findItem(R.id.mnuFotos);
		mnuFotos.setVisible(showFotos);

		MenuItem mnuSubir = menu.findItem(R.id.mnuSubir);
		mnuSubir.setVisible(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
			case R.id.mnuFotos:
				Intent intent = new Intent(this, FotosActivity.class);
				intent.putExtra("user", mUserStr);
				//intent.putExtra("orden", mOrden.getID());
				//intent.putExtra("tipo", tipo);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.ic_menu).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		Integer action = navDrawerItems.get(+position).getMnu_option();
		//Toast.makeText(MainActivity.this, ""+action, Toast.LENGTH_SHORT).show();
		switch (action) {
		case 0:
			//fragment = new ActivityHome();
				fragment = new HomeFragment();
				fragment.setArguments(bundle);
			break;
		case 1:
			Intent intentp = new Intent(getApplicationContext(), OrdenesActivity.class);
			intentp.putExtra("tipo",Globals.ORDENLIST_TYPE_PENDIENTES);
			intentp.putExtra("user", mUserStr);
			intentp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentp);
			overridePendingTransition (R.anim.open_next, R.anim.close_next);
			break;
		case 2:
			Intent intenth = new Intent(getApplicationContext(), OrdenesActivity.class);
			intenth.putExtra("tipo",Globals.ORDENLIST_TYPE_HISTORIAL);
			intenth.putExtra("user", mUserStr);
			intenth.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intenth);
			overridePendingTransition (R.anim.open_next, R.anim.close_next);
			break;
		case 3:
			Intent intents = new Intent(getApplicationContext(), PasswordActivity.class);
			intents.putExtra("user", mUserStr);
			intents.putExtra("init",false);
			intents.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intents);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case 4:
			SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
			Globals.deleteInfo(loginData);
			//FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/cli"+mUserInfo.getMatricula());
			FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/cli"+mUserInfo.getCliente());
			Intent i= new Intent(MainActivity.this, LoginActivity.class);
			startActivity(i);
			finish();
			//mDrawerLayout.closeDrawer(mDrawerList);
			//moveTaskToBack (true);
			break;
		case 5:
			Intent intentsa = new Intent(getApplicationContext(), SaludActivity.class);
			intentsa.putExtra("user", mUserStr);
			intentsa.putExtra("userId", mUserInfo.getMatricula());
			intentsa.putExtra("salud", mUserInfo.getSalud());
			intentsa.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentsa);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
			case 6:
				//ProgressDialog
				Globals.progressDialog = new ProgressDialog(MainActivity.this);
				Globals.progressDialog.setMessage("Esta operación puede tomar varios segundos, Por favor espere."); // Mensaje
				Globals.progressDialog.setTitle("Cargando..."); // Título
				Globals.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
				Globals.progressDialog.show(); // Display Progress Dialog
				Globals.progressDialog.setCancelable(true);

				Intent intentmg = new Intent(MainActivity.this, MapaGeneralActivity.class);
				intentmg.putExtra("tipo",Globals.ORDENLIST_TYPE_HISTORIAL);
				intentmg.putExtra("user", mUserStr);
				intentmg.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intentmg);
				overridePendingTransition(R.anim.open_next, R.anim.close_next);
				break;

		default:
			break;
		}

		if(mUserInfo.getTipo() == Globals.CLIENTE_ADMINIS){
			fragment = new HomeFragment();
			fragment.setArguments(bundle);
		}

		if (fragment != null) {
			android.app.FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}

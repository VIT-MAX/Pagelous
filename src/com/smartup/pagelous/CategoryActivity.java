package com.smartup.pagelous;

import java.util.ArrayList;
import java.util.List;



import org.json.JSONArray;
import org.json.JSONObject;

import net.simonvt.menudrawer.MenuDrawer;

import android.R.string;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.smartup.pagelous.models.Category;

public class CategoryActivity extends SherlockFragmentActivity implements OnItemClickListener, OnNavigationListener {
    private static final String STATE_MENUDRAWER = MainActivity.class.getName() + ".menuDrawer";
    
    private MenuDrawer mMenuDrawer;

	private JSONObject jsonObject;
	private final Handler myHandler = new Handler();
	private List<Category> categoryList = new ArrayList<Category>();;	
	private CategoriesListFragment list;

	private GoogleMap googleMap;

	private Location myLocation;

	private LocationManager locationManager;
	private String url = "http://www.pagelous.com/api/";
	
	@Override
	public void onCreate(Bundle savedInstancesState ){
		super.onCreate(savedInstancesState);
		int categoryKey = getIntent().getExtras().getInt("category");
		String url = null;
		switch (categoryKey) {
		case 1:
			url = "http://www.pagelous.com/api/eat";
			break;
		case 2:
			url = "http://www.pagelous.com/api/shop";
			break;
		case 3:
			url = "http://www.pagelous.com/api/Action";
			break;
		case 4:
			url = "http://www.pagelous.com/api/Sport";
			break;
		case 5:
			url = "http://www.pagelous.com/api/eat";
			break;
		case 6:
			url = "http://www.pagelous.com/api/Travel";
			break;
		case 7:
			url = "http://www.pagelous.com/api/Business";
			break;
		case 8:
			url = "http://www.pagelous.com/api/Society";
			break;
		case 9:
			url = "http://www.pagelous.com/api/lifestyle";
			break;
		}
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setContentView(R.layout.category);
		mMenuDrawer.setMenuView(R.layout.side_menu_list);
		list = (CategoriesListFragment)getSupportFragmentManager().findFragmentById(R.id.categories_f);
		if (url != null)
			
			getCategoryItemsFromUrl(url);
		SideMenuListFragment menu = (SideMenuListFragment)getSupportFragmentManager().findFragmentById(R.id.f_menu);
		menu.getListView().setOnItemClickListener(this);
		ActionBar actionBar = getSupportActionBar();
		
		//Nazvanie ActionBar     !!!!! to chto zakomentino
		
		//actionBar.setTitle(url);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		//chtob pri vibore v vipodajuswem menu otobrazalasj vibrannaja kategorija(esli srazu zaswol v Action to i otobrazaetsja Action
		//actionBar.setSelectedNavigationItem(categoryKey-1);
		
		// sozdajom spisok dlja vipodajuswego menu
		
		ArrayAdapter<String> dropListMenu = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item);
	
		//sozdajotsja spisok
		dropListMenu.add("Eat");
		dropListMenu.add("Shop");
		dropListMenu.add("Action");
		dropListMenu.add("Sport");
		dropListMenu.add("Feel good");
		dropListMenu.add("Travel");
		dropListMenu.add("Business");
		dropListMenu.add("Society");
		dropListMenu.add("Lifestyle");
		
		//peredajotsja v action bar!
		
		actionBar.setListNavigationCallbacks(dropListMenu, this);
		actionBar.setSelectedNavigationItem(categoryKey-1);
		//list = (CategoriesListFragment)getSupportFragmentManager().findFragmentById(R.id.categories_f);
	
		initMap();
	}

	private void initMap() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		 
        // Showing status
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        }else { // Google Play Services are available
 
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
 
            // Getting GoogleMap object from the fragment
            googleMap = fm.getMap();
 
            // Enabling MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);
          
            //Getting Current Location
            myLocation = getLastBestLocation();       
         
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            
            if(myLocation!=null){
            	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
            googleMap.getUiSettings().setZoomControlsEnabled(false);
        }
	}
	
	 private Location getLastBestLocation() {
	 	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    
	    long GPSLocationTime = 0;
	    if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

	    long NetLocationTime = 0;

	    if (null != locationNet) {
	        NetLocationTime = locationNet.getTime();
	    }

	    if ( 0 < GPSLocationTime - NetLocationTime ) {
	        return locationGPS;
	    }
	    else{
	        return locationNet;
	    }

	 }		
	
	
	public void getCategoryItemsFromUrl(final String url) {
        Thread thread = new Thread()
        {
			@Override
            public void run() {
                try {
            		JSONParser jsonParser = new JSONParser();
            		jsonObject = jsonParser.getJSONFromUrl(url);
        			JSONArray dataArray = jsonObject.getJSONArray("pages");
        			categoryList.clear();
        			for (int i = 0; i < dataArray.length(); i++) {
        				categoryList.add(new Category((JSONObject) dataArray.get(i))) ;    				
        			}                		
                	myHandler.post(updateRunnable);               	
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };                      
        thread.start();           
	}
	
    final Runnable updateRunnable = new Runnable() {
        public void run() {
    		list.setCategoryList(categoryList);
        }
    };
    
	
	@Override
	public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
		
		super.onBackPressed();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mMenuDrawer.setActiveView(view);
		mMenuDrawer.closeMenu();
	}
	
    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}


	public List<Category> getCategoryList() {
		return categoryList;
	}


	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		//vizivaem pri nazatie na spisok menu to chto vibrali nuznuju kategoruju MENU
		switch (itemPosition +1) {
		case 1:
			url = "http://www.pagelous.com/api/eat";
			break;
		case 2:
			url = "http://www.pagelous.com/api/shop";
			break;
		case 3:
			url = "http://www.pagelous.com/api/Action";
			break;
		case 4:
			url = "http://www.pagelous.com/api/Sport";
			break;
		case 5:
			url = "http://www.pagelous.com/api/eat";
			break;
		case 6:
			url = "http://www.pagelous.com/api/Travel";
			break;
		case 7:
			url = "http://www.pagelous.com/api/Business";
			break;
		case 8:
			url = "http://www.pagelous.com/api/Society";
			break;
		case 9:
			url = "http://www.pagelous.com/api/lifestyle";
			break;
		}
		getCategoryItemsFromUrl(url);
				
		return false;
	}

	

		
	}
		
	
    
    
    
    
    



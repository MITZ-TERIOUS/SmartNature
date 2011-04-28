package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FindGarden extends ListActivity implements AdapterView.OnItemClickListener {
	
	static GardenAdapter adapter;
	ArrayList<Garden> gardens = new ArrayList<Garden>();
	Gson gson = new Gson();
	
	@Override @SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Window.FEATURE_PROGRESS
		Object previousData = getLastNonConfigurationInstance(); 
		if (previousData != null)
			gardens = (ArrayList<Garden>) previousData;
		setContentView(R.layout.find_garden);
		adapter = new GardenAdapter(this, R.layout.findgarden_list_item, gardens);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		if (previousData == null) {
			setProgressBarIndeterminateVisibility(true);
			new Thread(getGardens).start();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return gardens.isEmpty() ? null : gardens;
	}
	
	Runnable getGardens = new Runnable() {
		@Override
		public void run() {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://gardengnome.heroku.com/gardens.json");
			int[] gardenIDs = {};
			boolean success = false;
			try {
				HttpResponse response = httpclient.execute(httpget);
				
				System.out.println("status code=" + response.getStatusLine().getStatusCode());
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				gardenIDs = gson.fromJson(result, int[].class);
				success = true;
			} catch (Exception e) { e.printStackTrace(); }
			
			if (success) {
				for (int i = 0; i < gardenIDs.length; i++) {
					gardens.add(getGarden(gardenIDs[i]));
					runOnUiThread(new Runnable() {
						@Override public void run() { adapter.notifyDataSetChanged(); }
					});
				}
			} else {
				runOnUiThread(new Runnable() {
					@Override public void run() {
						findViewById(R.id.find_garden_msg).setVisibility(View.VISIBLE);
					}
				});
			}
			
			runOnUiThread(new Runnable() {
				@Override public void run() { setProgressBarIndeterminateVisibility(false); }
			});
		}
	};
	
	public Garden getGarden(int serverId) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://gardengnome.heroku.com/gardens/" + serverId + ".json");
		String result = "";
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
				result = EntityUtils.toString(entity);
		} catch (Exception e) { e.printStackTrace(); }
		
		return gson.fromJson(result, Garden.class);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}
	
	class GardenAdapter extends ArrayAdapter<Garden> {
		
		private ArrayList<Garden> gardens;
		private LayoutInflater li;
		
		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			gardens = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null)
				view = li.inflate(R.layout.findgarden_list_item, null);
			Garden garden = gardens.get(position);
			((TextView) view.findViewById(R.id.garden_name)).setText(garden.getName());
			((TextView) view.findViewById(R.id.garden_info)).setText(garden.getCity() + ", " + garden.getState());
			return view;
		}
	}
}
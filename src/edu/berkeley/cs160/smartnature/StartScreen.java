package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

class GardenGnome extends Application {
	static ArrayList<Garden> gardens = new ArrayList<Garden>();
}

public class StartScreen extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
	
	static GardenAdapter adapter;
	static ArrayList<Garden> gardens;
	/** for prototype this has the position of the clicked garden */
	static int id;
	AlertDialog dialog;
	private DatabaseHelper dh;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.dh = new DatabaseHelper(this);	
		initMockData();
		getListView().setOnItemClickListener(this);
		((Button) findViewById(R.id.new_garden)).setOnClickListener(this);
		((Button) findViewById(R.id.search_encyclopedia)).setOnClickListener(this);
		
	}
	
	public void initMockData() {
		gardens = GardenGnome.gardens;
		if (gardens.isEmpty()) {
			dh.insert_garden("Berkeley Youth Alternatives", R.drawable.preview1, "40,60,90,200");
			dh.insert_garden("Karl Linn", R.drawable.preview2, "140,120,210,190");
			
			Garden g1 = dh.select_garden("Berkeley Youth Alternatives");
			Garden g2 = dh.select_garden("Karl Linn");
			
			dh.insert_plot("Jerry", "40,60,90,200," + Color.BLACK, Plot.RECT, Color.BLACK, "", 10, 1);
			dh.insert_plot("Amy", "140,120,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 0, 2);
			dh.insert_plot("Shared", "270,120,360,220," + Color.BLACK, Plot.POLY, Color.BLACK, "0,0,50,10,90,100", 0, 3);
			dh.insert_plot("Cyndi", "40,200,90,300," + Color.BLACK, Plot.RECT, Color.BLACK, "", 0, 4);
			dh.insert_plot("Alex", "140,50,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 10, 5);
			dh.insert_plot("Flowers", "270,120,360,260," + Color.BLACK, Plot.POLY, Color.BLACK, "0,0,50,10,90,100,70,140,60,120", 0, 6);
			
			Plot p1 = dh.select_plot("Jerry");
			Plot p2 = dh.select_plot("Amy");
			Plot p3 = dh.select_plot("Shared");
			Plot p4 = dh.select_plot("Cyndi");
			Plot p5 = dh.select_plot("Alex");
			Plot p6 = dh.select_plot("Flowers");
			
			g1.addPlot(p1);
			g1.addPlot(p2);
			g1.addPlot(p3);
			g2.addPlot(p4);
			g2.addPlot(p5);
			g2.addPlot(p6);
			
			gardens.add(g1);
			gardens.add(g2);
		}
		adapter = new GardenAdapter(this, R.layout.garden_list_item, gardens);
		setListAdapter(adapter);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.new_garden)
			startActivity(new Intent(this, GardenScreen.class));
		else
			startActivity(new Intent(this, Encyclopedia.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, GardenScreen.class);
		Bundle bundle = new Bundle();
		StartScreen.id = position;
		bundle.putInt("id", position);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_contact:
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setData(Uri.parse("mailto:" + getString(R.string.dev_email)));
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GardenGnome feedback");
				startActivity(intent); //startActivity(Intent.createChooser(intent, "Send mail..."));
				break;
			case R.id.m_globaloptions:
				startActivity(new Intent(this, GlobalSettings.class));
				break;
			case R.id.m_help:
		}
		return super.onOptionsItemSelected(item);
	}
	
	class GardenAdapter extends ArrayAdapter<Garden> {
		private ArrayList<Garden> items;
		private LayoutInflater li;
		
		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
			super(context, textViewResourceId, items);
			li = ((ListActivity) context).getLayoutInflater();
			this.items = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = li.inflate(R.layout.garden_list_item, null);
			Garden g = items.get(position);
			((TextView) v.findViewById(R.id.garden_name)).setText(g.getName());
			((ImageView) v.findViewById(R.id.preview_img)).setImageResource(g.getPreviewId());
			
			return v;
		}
	}
}

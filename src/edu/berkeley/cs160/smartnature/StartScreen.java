package edu.berkeley.cs160.smartnature;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

class GardenGnome extends Application {
	static ArrayList<Garden> gardens = new ArrayList<Garden>();
}

public class StartScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
	
	static GardenAdapter adapter;
	ArrayList<Garden> gardens;
	AlertDialog dialog;
	View textEntryView;
	private DatabaseHelper dh;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.dh = new DatabaseHelper(this);	
		initMockData();
		getListView().setOnItemClickListener(this);
		findViewById(R.id.new_garden).setOnClickListener(this);
		findViewById(R.id.search_encyclopedia).setOnClickListener(this);
		findViewById(R.id.find_garden).setOnClickListener(this);		
	}
	
	public void initMockData() {
		gardens = GardenGnome.gardens;
		if (gardens.isEmpty()) {
			Garden g1 = new Garden(R.drawable.preview1, "Berkeley Youth Alternatives");	
			Garden g2 = new Garden(R.drawable.preview2, "Karl Linn");
			g1.setCity("Berkeley"); g1.setState("CA");
			g2.setCity("Berkeley"); g2.setState("CA");
			
			Rect bounds1 = new Rect(40, 60, 90, 200);
			Rect bounds2 = new Rect(140, 120, 210, 190);
			Rect bounds3 = new Rect(270, 120, 270 + 90, 120 + 100);
			Rect bounds4 = new Rect(40, 200, 90, 300);
			Rect bounds5 = new Rect(140, 50, 210, 190);
			Rect bounds6 = new Rect(270, 120, 270 + 90, 120 + 140);
			
			float[] pts = { 0, 0, 50, 10, 90, 100 };
			float[] pts2 = { 0, 0, 50, 10, 90, 100, 70, 140, 60, 120 };
			
			g1.addPlot("Jerry's Plot", bounds1, 10, Plot.RECT);
			g1.addPlot("Amy's Plot", bounds2, 0, Plot.OVAL);
			g1.addPlot("Shared Plot", bounds3, 0, pts);
			g2.addPlot("Cyndi's Plot", bounds4, 0, Plot.RECT);
			g2.addPlot("Alex's Plot", bounds5, 10, Plot.OVAL);
			g2.addPlot("Flowers", bounds6, 0, pts2);
			
			gardens.add(g1);
			gardens.add(g2);
		}
		adapter = new GardenAdapter(this, R.layout.garden_list_item, gardens);
		setListAdapter(adapter);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.new_garden:
				showDialog(0);
				break;
			case R.id.search_encyclopedia:
				startActivity(new Intent(this, Encyclopedia.class));
				break;
			case R.id.find_garden:
				startActivity(new Intent(this, FindGarden.class));
				break;
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);
		
		dialog = builder.setTitle(R.string.new_garden_prompt)
				.setPositiveButton(R.string.alert_dialog_ok, this)
				.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
				.create();
		
		// automatically show soft keyboard
		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
		
		return dialog;
	}
	
	public void onClick(DialogInterface dialog, int whichButton) {
		Intent intent = new Intent(this, GardenScreen.class);
		intent.putExtra("garden_id", gardens.size());
		EditText gardenName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
		gardens.add(new Garden(gardenName.getText().toString()));
		adapter.notifyDataSetChanged();
		startActivity(intent);
		removeDialog(0);
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
		intent.putExtra("garden_id", position);
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
				view = li.inflate(R.layout.garden_list_item, null);
			Garden garden = gardens.get(position);
			((TextView) view.findViewById(R.id.garden_name)).setText(garden.getName());
			((ImageView) view.findViewById(R.id.preview_img)).setImageResource(garden.getPreviewId());
			
			return view;
		}
	}
}

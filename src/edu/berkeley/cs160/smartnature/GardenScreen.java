package edu.berkeley.cs160.smartnature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

public class GardenScreen extends Activity {
	
	final int ZOOM_DURATION = 3000;
	Garden mockGarden;
	AlertDialog dialog;
	ZoomControls zoom;
	GardenView gardenView;
	Handler mHandler = new Handler();
	boolean showLabels = true, showFullScreen, zoomAutoHidden;
	int zoomLevel;
	
	int gardenID;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
		if (showFullScreen)
			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("id")) {
			mockGarden = StartScreen.gardens.get(extras.getInt("id"));
			gardenID = extras.getInt("id");
			setTitle(mockGarden.getName());
		} else {
			mockGarden = new Garden(R.drawable.preview, "");
			showDialog(0);
		}
		setContentView(R.layout.garden);
		gardenView = (GardenView) findViewById(R.id.garden_view);
		zoom = (ZoomControls) findViewById(R.id.zoom_controls);
		zoomAutoHidden = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("zoom_autohide", false);
		if (zoomAutoHidden)
			zoom.setVisibility(View.GONE);
		zoom.setOnZoomInClickListener(zoomIn);
		zoom.setOnZoomOutClickListener(zoomOut);
		
		/*(gardenView.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		setContentView(R.layout.plot);
		settingListeners();
		Bundle bundle = new Bundle();
		//bundle.putString("name", ((TextView)view.findViewById(R.id.garden_name)).getText().toString());
		//intent.putExtras(bundle);
		//startActivity(intent);
		}
		});*/
		
		/*
		gardenView.setOnClickListener(new OnClickListener() {

	    public void onClick(View v) {
	
      	Intent intent = new Intent(GardenScreen.this, PlotScreen.class);
				Bundle bundle = new Bundle(3);
				bundle.putString("name", gardenView.focusedPlot.getName());
				bundle.putInt("gardenID", gardenID);
				bundle.putInt("plotID", gardenView.focusedPlot.getID());
				intent.putExtras(bundle);      	
				startActivity(intent);

	    }
	  });
		*/
		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
		if (hintsOn) {
			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
		}
	}
	
	private void settingListeners(){
	   TextView plotTitle = (TextView) findViewById(R.id.plotTextView);
	   plotTitle.setText(gardenView.focusedPlot.getName());
	  
	   Button addPlantButton = (Button) findViewById(R.id.addPlantButton);
	   addPlantButton.setOnClickListener(new OnClickListener() {
	   @Override
	          public void onClick(View v) {
	           Intent intent = new Intent(GardenScreen.this, PlantScreen.class);
	     //Bundle bundle = new Bundle();
	     //bundle.putString("name", ((TextView) v.findViewById(R.id.garden_name)).getText().toString());
	     //intent.putExtras(bundle);
	     startActivity(intent);
	     //showDialog(0);
	          }
	      });

	   Button backButton = (Button) findViewById(R.id.backButton);
	   backButton.setOnClickListener(new OnClickListener() {
	          public void onClick(View v) {
	           setContentView(R.layout.garden);
	          }
	      });

	  }
  
	@Override
	public Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText gardenName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
				setTitle(gardenName.getText().toString());
				mockGarden.setName(gardenName.getText().toString());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						StartScreen.gardens.add(mockGarden);
						StartScreen.adapter.notifyDataSetChanged();
					}
				});
			}
		};
		DialogInterface.OnClickListener canceled = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		};
		DialogInterface.OnCancelListener exited = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		};
		dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.new_garden_prompt)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
			.setNegativeButton(R.string.alert_dialog_cancel, canceled) // this means the dialog's cancel button was pressed
			.setOnCancelListener(exited) // this means the back button was pressed
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.garden_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_addregion:
				Intent intent = new Intent(this, AddPlot.class);
				Bundle bundle = new Bundle();
				bundle.putInt("id", StartScreen.gardens.indexOf(mockGarden));
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			case R.id.m_home:
				finish();
				break;
			case R.id.m_resetzoom:
				zoomLevel = 0;
				gardenView.reset();
				break;
			case R.id.m_share:
				startActivity(new Intent(this, ShareGarden.class));
				break;
			case R.id.m_showlabels:
				showLabels = !showLabels;
				item.setTitle(showLabels ? "Hide labels" : "Show labels");
				gardenView.invalidate();				
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	View.OnClickListener zoomIn = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			ScaleAnimation anim = new ScaleAnimation(1, 1.5f, 1, 1.5f, gardenView.getWidth() / 2.0f, gardenView.getHeight() / 2.0f);
			anim.setDuration(400);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation anim) { }
				@Override
				public void onAnimationRepeat(Animation anim) { }
				@Override
				public void onAnimationEnd(Animation anim) { zoomLevel++; }
			});
			gardenView.startAnimation(anim);
		}
	};
	
	View.OnClickListener zoomOut = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			handleZoom();
			ScaleAnimation anim = new ScaleAnimation(1, 1/1.5f, 1, 1/1.5f, gardenView.getWidth() / 2.0f, gardenView.getHeight() / 2.0f); 
			anim.setDuration(400);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation anim) { }				
				@Override
				public void onAnimationRepeat(Animation anim) { }
				@Override
				public void onAnimationEnd(Animation anim) { zoomLevel--; }
			});
			gardenView.startAnimation(anim);
		}
	};
	
	public void handleZoom() {
		if (zoomAutoHidden) {
			mHandler.removeCallbacks(autoHide);
			if (!zoom.isShown())
				zoom.show(); //zoom.setVisibility(View.VISIBLE);
			mHandler.postDelayed(autoHide, ZOOM_DURATION);
		}
	}
	
	Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			if (zoom.isShown()) {
				mHandler.removeCallbacks(autoHide);
				zoom.hide(); //zoom.setVisibility(View.GONE);
			}
		}
	};
}

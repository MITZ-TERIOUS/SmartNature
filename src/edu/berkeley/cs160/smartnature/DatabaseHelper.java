package edu.berkeley.cs160.smartnature;
//http://www.screaming-penguin.com/node/7742

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Rect;
import android.graphics.RectF;

public class DatabaseHelper {
	private static final String DATABASE_NAME = "garden_gnome.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME_GARDEN = "garden";
	private static final String TABLE_NAME_PLOT = "plot";
	private static final String TABLE_NAME_PLANT = "plant";
	private static final String TABLE_NAME_ENTRY = "entry";
	private static final String TABLE_NAME_MAP_GP = "map_garden_plot";
	private static final String TABLE_NAME_MAP_PP = "map_plot_plant";
	private static final String TABLE_NAME_MAP_PE = "map_plant_entry";
	private static final String INSERT_GARDEN = "insert into " + TABLE_NAME_GARDEN + " (g_pk, name, previewId, bounds, city, state, serverId, is_public, images) values (NULL, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_PLOT = "insert into " + TABLE_NAME_PLOT + " (po_pk, name, shape, type, color, polyPoints, rotation, id) values (NULL, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_PLANT = "insert into " + TABLE_NAME_PLANT + " (pa_pk, name, id) values (NULL, ?, ?)";
	private static final String INSERT_ENTRY = "insert into " + TABLE_NAME_ENTRY + " (e_pk, name, date) values (NULL, ?, ?)";
	private static final String INSERT_MAP_GP = "insert into " + TABLE_NAME_MAP_GP + " (g_map, po_map) values (?, ?)";
	private static final String INSERT_MAP_PP = "insert into " + TABLE_NAME_MAP_PP + " (po_map, pa_map) values (?, ?)";
	private static final String INSERT_MAP_PE = "insert into " + TABLE_NAME_MAP_PE + " (pa_map, e_map) values (?, ?)";

	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt_garden, insertStmt_plot, insertStmt_plant, insertStmt_entry, insertStmt_map_gp, insertStmt_map_pp, insertStmt_map_pe;

	public DatabaseHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt_garden = this.db.compileStatement(INSERT_GARDEN);
		this.insertStmt_plot = this.db.compileStatement(INSERT_PLOT);
		this.insertStmt_plant = this.db.compileStatement(INSERT_PLANT);
		this.insertStmt_entry = this.db.compileStatement(INSERT_ENTRY);
		this.insertStmt_map_gp = this.db.compileStatement(INSERT_MAP_GP);
		this.insertStmt_map_pp = this.db.compileStatement(INSERT_MAP_PP);
		this.insertStmt_map_pe = this.db.compileStatement(INSERT_MAP_PE);
	}

	public long insert_garden(String name, int previewId, String bounds, String city, String state, int serverId, int is_public, String images) {
		this.insertStmt_garden.clearBindings();
		this.insertStmt_garden.bindString(1, name);
		this.insertStmt_garden.bindLong(2, (long) previewId);
		this.insertStmt_garden.bindString(3, bounds);
		this.insertStmt_garden.bindString(4, city);
		this.insertStmt_garden.bindString(5, state);
		this.insertStmt_garden.bindLong(6, (long) serverId);
		this.insertStmt_garden.bindLong(7, (long) is_public);
		this.insertStmt_garden.bindString(8, images);
		return this.insertStmt_garden.executeInsert();
	}

	public long update_garden(int g_pk, String name, int previewId, String bounds, String city, String state, int serverId, int is_public, String images) {
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("previewId", previewId);
		cv.put("bounds", bounds);
		cv.put("city", city);
		cv.put("state", state);
		cv.put("serverId", serverId);
		cv.put("is_public", is_public);
		cv.put("images", images);
		String selection = "g_pk = ?";
		return db.update(TABLE_NAME_GARDEN, cv, selection, new String[] {Integer.toString(g_pk)});
	}

	public long update_garden(int g_pk, String bounds) {
		ContentValues cv = new ContentValues();
		cv.put("bounds", bounds);
		String selection = "g_pk = ?";
		return db.update(TABLE_NAME_GARDEN, cv, selection, new String[] {Integer.toString(g_pk)});
	}

	public long update_garden(int g_pk, String name, String city, String state) {
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("city", city);
		cv.put("state", state);

		String selection = "g_pk = ?";
		return db.update(TABLE_NAME_GARDEN, cv, selection, new String[] {Integer.toString(g_pk)});
	}

	public void delete_garden(int g_pk) {
		String selection = "g_pk = ?";
		this.db.delete(TABLE_NAME_GARDEN, selection, new String[] {Integer.toString(g_pk)});
	}

	public void delete_all_garden() {
		this.db.delete(TABLE_NAME_GARDEN, null, null);
	}

	public Garden select_garden(int g_pk) {
		String selection = "g_pk = " + g_pk;
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, null, selection, null, null, null, null);
		Garden temp = null;
		if (cursor.moveToFirst()) {
			do {
				temp = new Garden(Integer.parseInt(cursor.getString(cursor.getColumnIndex("previewId"))), cursor.getString(cursor.getColumnIndex("name")));
				String[] bound_s = cursor.getString(cursor.getColumnIndex("bounds")).split(",");
				RectF bound_rf = new RectF(Float.parseFloat(bound_s[0].trim()), Float.parseFloat(bound_s[1].trim()), Float.parseFloat(bound_s[2].trim()), Float.parseFloat(bound_s[3].trim()));
				temp.setRawBounds(bound_rf);
				temp.setCity(cursor.getString(cursor.getColumnIndex("city")));
				temp.setState(cursor.getString(cursor.getColumnIndex("state")));
				temp.setServerId(cursor.getInt(cursor.getColumnIndex("serverId")));
				if(cursor.getInt(cursor.getColumnIndex("is_public")) == 1)
					temp.setPublic(true);
				else
					temp.setPublic(false);
				ArrayList<String> images_al = new ArrayList<String>();
				if(!cursor.getString(cursor.getColumnIndex("images")).equalsIgnoreCase("")) {
					String[] images_s = cursor.getString(cursor.getColumnIndex("images")).split(",");
					for(int i = 0; i < images_s.length; i++)
						images_al.add(images_s[i]);
				}
				temp.setImages(images_al);
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public ArrayList<Garden> select_all_garden() {
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, null, null, null, null, null, null);
		ArrayList<Garden> temp_l = new ArrayList<Garden>();
		if (cursor.moveToFirst()) {
			do {
				Garden temp_g = new Garden(Integer.parseInt(cursor.getString(cursor.getColumnIndex("previewId"))), cursor.getString(cursor.getColumnIndex("name")));
				String[] bound_s = cursor.getString(cursor.getColumnIndex("bounds")).split(",");
				RectF bound_rf = new RectF(Float.parseFloat(bound_s[0].trim()), Float.parseFloat(bound_s[1].trim()), Float.parseFloat(bound_s[2].trim()), Float.parseFloat(bound_s[3].trim()));
				temp_g.setRawBounds(bound_rf);
				temp_g.setCity(cursor.getString(cursor.getColumnIndex("city")));
				temp_g.setState(cursor.getString(cursor.getColumnIndex("state")));
				temp_g.setServerId(cursor.getInt(cursor.getColumnIndex("serverId")));
				if(cursor.getInt(cursor.getColumnIndex("is_public")) == 1)
					temp_g.setPublic(true);
				else
					temp_g.setPublic(false);
				String[] images_s = cursor.getString(cursor.getColumnIndex("images")).split(",");
				ArrayList<String> images_al = new ArrayList<String>();
				for(int i = 0; i < images_s.length; i++)
					images_al.add(images_s[i]);
				temp_g.setImages(images_al);
				temp_l.add(temp_g);
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp_l;
	}

	public List<Integer> select_all_garden_pk() {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, new String[] {"g_pk"}, null, null, null, null, "g_pk asc");
		if (cursor.moveToFirst()) {
			do 
				list.add(cursor.getInt(cursor.getColumnIndex("g_pk")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed()) 
			cursor.close();
		return list;
	}
	
	public int select_garden_pk(String name) {
		Cursor cursor = this.db.query(TABLE_NAME_GARDEN, null, "name = ?", new String[] {name}, null, null, "g_pk asc");
		int temp = -1;
		if (cursor.moveToFirst()) {
			do
				temp = cursor.getInt(cursor.getColumnIndex("g_pk"));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public int count_garden() {
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + TABLE_NAME_GARDEN, null);
		int temp = 1;
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			temp = cursor.getInt(0);
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public long insert_plot(String name, String shape, int type, int color, String polyPoints, float rotation, int id) {
		this.insertStmt_plot.clearBindings();
		this.insertStmt_plot.bindString(1, name);
		this.insertStmt_plot.bindString(2, shape);
		this.insertStmt_plot.bindLong(3, (long) type);
		this.insertStmt_plot.bindLong(4, (long) color);
		this.insertStmt_plot.bindString(5, polyPoints);
		this.insertStmt_plot.bindDouble(6, (double) rotation);
		this.insertStmt_plot.bindLong(7, (long) id);
		return this.insertStmt_plot.executeInsert();
	}

	public long update_plot(int po_pk, String shape, int color, String polyPoints, float rotation) {
		ContentValues cv = new ContentValues();
		cv.put("shape", shape);
		cv.put("color", color);
		cv.put("polyPoints", polyPoints);
		cv.put("rotation", rotation);
		String selection = "po_pk = ?";
		return db.update(TABLE_NAME_PLOT, cv, selection, new String[] {Integer.toString(po_pk)});
	}

	public Plot select_plot(int po_pk) {
		String selection = "po_pk = " + po_pk;
		Cursor cursor = this.db.query(TABLE_NAME_PLOT, null, selection, null, null, null, null);
		Plot temp = null;
		if (cursor.moveToFirst()) {
			do {
				String[] shape_s = cursor.getString(cursor.getColumnIndex("shape")).split(",");
				Rect bound_r = new Rect(Integer.parseInt(shape_s[0]), Integer.parseInt(shape_s[1]), Integer.parseInt(shape_s[2]), Integer.parseInt(shape_s[3]));
				int color = Integer.parseInt(shape_s[4]);
				if(cursor.getInt(cursor.getColumnIndex("type")) == Plot.RECT || cursor.getInt(cursor.getColumnIndex("type")) == Plot.OVAL)
					temp = new Plot(cursor.getString(cursor.getColumnIndex("name")), bound_r, cursor.getFloat(cursor.getColumnIndex("rotation")), cursor.getInt(cursor.getColumnIndex("type")));
				else {
					if(!cursor.getString(cursor.getColumnIndex("polyPoints")).equalsIgnoreCase("")) {
						String[] polyPoints_s = cursor.getString(cursor.getColumnIndex("polyPoints")).split(",");
						float[] polyPoints_f = new float[polyPoints_s.length];
						for(int i = 0; i < polyPoints_f.length; i++)
							polyPoints_f[i] = Float.valueOf(polyPoints_s[i].trim());
						temp = new Plot(cursor.getString(cursor.getColumnIndex("name")), bound_r, cursor.getFloat(cursor.getColumnIndex("rotation")), polyPoints_f);
					}
					else
						temp = new Plot(cursor.getString(cursor.getColumnIndex("name")), bound_r, cursor.getFloat(cursor.getColumnIndex("rotation")), new float[] {0,0});
				}
				temp.setColor(cursor.getInt(cursor.getColumnIndex("color")));
				temp.getPaint().setColor(color);
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		if (temp == null)
			System.err.println("select_plot fail");
		return temp;
	}

	public String select_plot_name(int po_pk) {
		String selection = "po_pk = " + po_pk;
		Cursor cursor = this.db.query(TABLE_NAME_PLOT, null, selection, null, null, null, null);
		String temp = "";
		if (cursor.moveToFirst()) {
			do
				temp = cursor.getString(cursor.getColumnIndex("name"));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public int count_plot() {
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + TABLE_NAME_PLOT, null);
		int temp = 1;
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			temp = cursor.getInt(0);
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public void delete_plot(int po_pk) {
		String selection = "po_pk = ?";
		this.db.delete(TABLE_NAME_PLOT, selection, new String[] {Integer.toString(po_pk)});
	}

	public long insert_plant(String name, int id) {
		this.insertStmt_plant.clearBindings();
		this.insertStmt_plant.bindString(1, name);
		this.insertStmt_plant.bindLong(2, (long)id);
		return this.insertStmt_plant.executeInsert();
	}

	public Plant select_plant(int pa_pk) {
		String selection = "pa_pk = " + pa_pk + " AND name IS NOT NULL";
		Cursor cursor = this.db.query(TABLE_NAME_PLANT, null, selection, null, null, null, null);
		Plant temp = null;
		if (cursor.moveToFirst()) {
			do {
				temp = new Plant(cursor.getString(cursor.getColumnIndex("name")));
				temp.setServerId(cursor.getInt(cursor.getColumnIndex("id")));
			} while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public String select_plant_name(int pa_pk) {
		String selection = "pa_pk = " + pa_pk;
		Cursor cursor = this.db.query(TABLE_NAME_PLANT, null, selection, null, null, null, null);
		String temp = "";
		if (cursor.moveToFirst()) {
			do
				temp = cursor.getString(cursor.getColumnIndex("name"));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public int count_plant() {
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + TABLE_NAME_PLANT, null);
		int temp = 1;
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			temp = cursor.getInt(0);
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public void delete_plant(int pa_pk) {
		String selection = "pa_pk = ?";
		this.db.delete(TABLE_NAME_PLANT, selection, new String[] {Integer.toString(pa_pk)});
	}

	public long insert_entry(String name, String date) {
		this.insertStmt_entry.clearBindings();
		this.insertStmt_entry.bindString(1, name);
		this.insertStmt_entry.bindString(2, date);
		return this.insertStmt_entry.executeInsert();
	}

	public Entry select_entry(int e_pk) {
		String selection = "e_pk = " + e_pk + " AND name IS NOT NULL";
		Cursor cursor = this.db.query(TABLE_NAME_ENTRY, null, selection, null, null, null, null);
		Entry temp = null;
		if (cursor.moveToFirst()) {
			do
				temp = new Entry(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("date")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public String select_entry_name(int e_pk) {
		String selection = "e_pk = " + e_pk;
		Cursor cursor = this.db.query(TABLE_NAME_ENTRY, null, selection, null, null, null, null);
		String temp = "";
		if (cursor.moveToFirst()) {
			do
				temp = cursor.getString(cursor.getColumnIndex("name"));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public int count_entry() {
		Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + TABLE_NAME_ENTRY, null);
		int temp = 1;
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			temp = cursor.getInt(0);
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return temp;
	}

	public void delete_entry(int e_pk) {
		String selection = "e_pk = ?";
		this.db.delete(TABLE_NAME_ENTRY, selection, new String[] {Integer.toString(e_pk)});
	}

	public long insert_map_gp(int g_map, int po_map) {
		this.insertStmt_map_gp.clearBindings();
		this.insertStmt_map_gp.bindLong(1, (long)g_map);
		this.insertStmt_map_gp.bindLong(2, (long)po_map);
		return this.insertStmt_map_gp.executeInsert();
	}

	public List<Integer> select_map_gp_po(int g_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "g_map" + " = '" + g_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_GP, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("po_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return list;
	}
	
	public List<Integer> select_map_gp_g() {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor = this.db.rawQuery("SELECT DISTINCT g_map FROM " + TABLE_NAME_MAP_GP, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("g_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return list;
	}
	
	public void delete_map_gp_g(int g_map) {
		String selection = "g_map = ?";
		this.db.delete(TABLE_NAME_MAP_GP, selection, new String[] {Integer.toString(g_map)});
	}
	
	public void delete_map_gp_p(int po_map) {
		String selection = "po_map = ?";
		this.db.delete(TABLE_NAME_MAP_GP, selection, new String[] {Integer.toString(po_map)});
	}

	public long insert_map_pp(int po_map, int pa_map) {
		this.insertStmt_map_pp.clearBindings();
		this.insertStmt_map_pp.bindLong(1, (long)po_map);
		this.insertStmt_map_pp.bindLong(2, (long)pa_map);
		return this.insertStmt_map_pp.executeInsert();
	}

	public List<Integer> select_map_pp_pa(int po_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "po_map" + " = '" + po_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_PP, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("pa_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return list;
	}

	public void delete_map_pp(int pa_map) {
		String selection = "pa_map = ?";
		this.db.delete(TABLE_NAME_MAP_PP, selection, new String[] {Integer.toString(pa_map)});
	}

	public long insert_map_pe(int pa_map, int e_map) {
		this.insertStmt_map_pe.clearBindings();
		this.insertStmt_map_pe.bindLong(1, (long)pa_map);
		this.insertStmt_map_pe.bindLong(2, (long)e_map);
		return this.insertStmt_map_pe.executeInsert();
	}

	public List<Integer> select_map_pe_e(int pa_map) {
		List<Integer> list = new ArrayList<Integer>();
		String selection = "pa_map" + " = '" + pa_map + "'";
		Cursor cursor = this.db.query(TABLE_NAME_MAP_PE, null, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			do
				list.add(cursor.getInt(cursor.getColumnIndex("e_map")));
			while (cursor.moveToNext());
		}
		if (cursor != null || !cursor.isClosed())
			cursor.close();
		return list;
	}

	public void delete_map_pe(int e_map) {
		String selection = "e_map = ?";
		this.db.delete(TABLE_NAME_MAP_PE, selection, new String[] {Integer.toString(e_map)});
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME_GARDEN + " (g_pk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, name TEXT, previewId INTEGER, bounds TEXT, city TEXT, state TEXT, serverId INTEGER, is_public INTEGER, images TEXT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_PLOT + " (po_pk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, name TEXT, shape TEXT, type INTEGER, color INTEGER, polyPoints TEXT, rotation REAL, id INTEGER)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_PLANT + " (pa_pk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, name TEXT, id INTEGER)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_ENTRY + " (e_pk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, name TEXT, date TEXT)");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_GP + " (g_map INTEGER, po_map INTEGER, FOREIGN KEY(g_map) REFERENCES garden(g_pk), FOREIGN KEY(po_map) REFERENCES plot(po_pk))");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_PP + " (po_map INTEGER, pa_map INTEGER, FOREIGN KEY(po_map) REFERENCES plot(po_pk), FOREIGN KEY(pa_map) REFERENCES plant(pa_pk))");
			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_PE + " (pa_map INTEGER, e_map INTEGER, FOREIGN KEY(pa_map) REFERENCES plant(pa_pk), FOREIGN KEY(e_map) REFERENCES entry(e_pk))");		
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GARDEN);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PLOT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PLANT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRY);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_GP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_PP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_PE);
			onCreate(db);
		}
	}
}

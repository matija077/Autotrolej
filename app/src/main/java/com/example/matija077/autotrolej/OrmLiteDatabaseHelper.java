package com.example.matija077.autotrolej;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Matija077 on 4/17/2017.
 */

public class OrmLiteDatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "autotorlej.db";
	private static final int DATABASE_VERSION = 2;

	//JAVA interface for acessing Database objects
	private Dao<Station, Integer> stationDao;
	private Dao<Route, Integer> routeDao;

	public OrmLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {

		//checking if database exists on null return from method below.
		//SQLiteDatabase databaseExists = getWritableDatabase();

		try {
			TableUtils.createTableIfNotExists(connectionSource, Station.class);
			TableUtils.createTableIfNotExists(connectionSource, Route.class);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(OrmLiteDatabaseHelper.class.getName(), "Error creating database");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
		try {
			//dropping tables if they exists, last parametar ignores errors in case table doesn't exist.
			TableUtils.dropTable(connectionSource, Route.class, true);
			TableUtils.dropTable(connectionSource, Station.class, true);
			onCreate(sqLiteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e("onUpgrade()", "Unable to upgrade database from version " + String.valueOf(Integer.valueOf(DATABASE_VERSION) - 1) + " to new "
					+ DATABASE_VERSION, e);
		}
	}

	//Close the database connections and clear any cached DAOs.
	@Override
	public void close() {
		super.close();
		stationDao = null;
		routeDao = null;
	}

	public Dao<Station, Integer> getStationDao() throws SQLException {
		if (stationDao == null) {
			stationDao = getDao(Station.class);
		}
		return stationDao;
	}

	public Dao<Route, Integer> getRouteDao() throws SQLException {
		if (routeDao == null) {
			routeDao = getDao(Route.class);
		}
		return routeDao;
	}

	//station
	public void insertStation(Station st) {

		Station station = new Station();
		station = st;

		try {
			stationDao = getStationDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			stationDao.create(station);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertStaton()", "Error inserting station in database");
		}
	}

	public void deleteStation(Station st) {
		Station station = new Station();
		station = st;
		try {
			stationDao.delete(station);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Station> getAllStations() {
		List<Station> stations = new ArrayList<Station>();
		try {
			stations = stationDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("GetAllStations()", "Error fatching all stations");
		}
		return stations;
	}

	//route
	public void insertRoute(Route rt) {
		Route route = rt;

		try {
			routeDao = getRouteDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			routeDao.create(route);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertRoute()", "Error inserting route in database");
		}
	}

	public List<Route> getAllRoutes() {
		List<Route> routes = new ArrayList<Route>();

		try {
			routes = routeDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getAllRoutes()", "Error fatching all routes");
		}
		return routes;
	}
}

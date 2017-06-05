package com.example.matija077.autotrolej;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Created by Matija077 on 4/17/2017.
 */

public class OrmLiteDatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "autotorlej.db";
	private static final int DATABASE_VERSION = 9;

	//JAVA interface for acessing Database objects
	private Dao<Station, Integer> stationDao;
	private Dao<Route, Integer> routeDao;
	private Dao<Schedule, Integer> scheduleDao;
	private Dao<Station_route, Integer> station_routeDao;
	private static final String TAG = "OrmLiteDatabaseHelper";

	public OrmLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		initialize();
	}

	private void initialize() {
		try {
			stationDao = getStationDao();
			routeDao = getRouteDao();
			station_routeDao = getStation_routeDao();
			scheduleDao = getScheduleDao();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {

		//checking if database exists on null return from method below.
		//SQLiteDatabase databaseExists = getWritableDatabase();

		try {
			TableUtils.createTableIfNotExists(connectionSource, Station.class);
			TableUtils.createTableIfNotExists(connectionSource, Route.class);
			TableUtils.createTableIfNotExists(connectionSource, Schedule.class);
			TableUtils.createTableIfNotExists(connectionSource, Station_route.class);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(OrmLiteDatabaseHelper.class.getName(), "Error creating database");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
		//dropping tables if they exists, last parametar ignores errors in case table doesn't exist.
		try {
			TableUtils.dropTable(connectionSource, Station_route.class, true);
			TableUtils.dropTable(connectionSource, Schedule.class, true);
			TableUtils.dropTable(connectionSource, Route.class, true);
			TableUtils.dropTable(connectionSource, Station.class, true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		onCreate(sqLiteDatabase, connectionSource);
	}

	//development porpoise is to drop data once inserted for now

	private void clearAllData() {
		try {
			TableUtils.clearTable(connectionSource, Station.class);
			TableUtils.clearTable(connectionSource, Route.class);
			TableUtils.clearTable(connectionSource, Schedule.class);
			TableUtils.clearTable(connectionSource, Station_route.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//Close the database connections and clear any cached DAOs.
	@Override
	public void close() {
		stationDao = null;
		routeDao = null;
		scheduleDao = null;
		station_routeDao = null;
		//clearAllData();
		super.close();
	}

	//wrapper for private clearAllData()
	public void clear() {
		clearAllData();
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

	public Dao<Schedule, Integer> getScheduleDao() throws SQLException {
		if (scheduleDao == null) {
			scheduleDao = getDao(Schedule.class);
		}
		return scheduleDao;
	}

	public Dao<Station_route, Integer> getStation_routeDao() throws SQLException {
		if (station_routeDao == null) {
			station_routeDao = getDao(Station_route.class);
		}
		return station_routeDao;
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

	public void insertStation(List<Station> st) {

		List<Station> stations = new ArrayList<Station>();
		stations = st;

		try {
			stationDao = getStationDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			for (Station station : stations) {
				stationDao.create(station);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertStaton()", "Error inserting station in database");
		}
	}

	public void insertStation(HashMap<String, Station> st) {

		HashMap<String, Station> stations = new HashMap<>();
		stations = st;

		try {
			stationDao = getStationDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
		for(Station station : stations.values()) {
			stationDao.create(station);
		}
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

	public Station getStationById(Integer id) {
		Station station = null;
		try {
			station = stationDao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getStationById()", "Error getting station with id" + id);
		}
		return station;
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

	public Station queryStation_specific1(Integer id) {
		Station station = null;
		try {
			station = stationDao.queryBuilder()
								.where().eq("id", id).queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return station;
	}

	/*
		query for all stations inside a visible rectangle representing visible map part given bye
		visibleRegion.latLngBounds method. we must query all unfortunately and we check for each
		station if it is inside of rectangle.
	*/
	public List<Station> queryStation_specific2(VisibleRegion visibleRegion) {
		List<Station> stations = new ArrayList<Station>();
		List<Station> returnedStations = new ArrayList<Station>();
		try {
			LatLngBounds latLngBounds = visibleRegion.latLngBounds;
			stations = stationDao.queryForAll();
			for (Station station : stations) {
				LatLng latLng = new LatLng(station.getGpsy(), station.getGpsx());
				if (latLngBounds.contains(latLng)) {
					returnedStations.add(station);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		stations = null;
		return returnedStations;
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

	public void insertRoute(HashMap<String, Route> rt) {

		HashMap<String, Route> routes = new HashMap<>();
		routes = rt;

		try {
			routeDao = getRouteDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			for (Route route : routes.values()) {
				routeDao.create(route);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertStaton()", "Error inserting route in database");
		}
	}

	public Route getRouteById(Integer id) {
		Route route = null;
		try {
			route = routeDao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getRouteById()", "Error getting route with id" + id);
		}
		return route;
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

	public Route queryRoute(String[] columnNames, String[] params, String[] connectors) {
		Route route = null;
		try {
			//route = routeDao.queryBuilder()
			//					.where().eq(columnName, param).queryForFirst();
			QueryBuilder<Route, Integer> queryBuilder = routeDao.queryBuilder();
			Where where = queryBuilder.where();
			for (int i = 0; i < columnNames.length; i++) {
				where.eq(columnNames[i], params[i]);
				if (connectors[i].equals("and")) {
					where.and();
				} else if (connectors[i].equals("or")) {
					where.or();
				}
			}
			PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return route;
	}

	/*
		Optimization is pain in the ass so this is for returning route if such exists in
		station_route part of parsing.
	*/
	public Route queryRoot_specific1(String routeMarkValue, String directionValue) {
		Route route = null;
		try {
			QueryBuilder<Route, Integer> queryBuilder = routeDao.queryBuilder();
			Where<Route, Integer> where = queryBuilder.where();
			where.and(
				where.eq("routeMark", routeMarkValue),
				where.or(
					where.eq("directionA", directionValue),
					where.eq("directionB", directionValue)
				)
			);
			//PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
			route = queryBuilder.queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return route;
	}

	public Route queryRoot_specific1(String routeMarkValue) {
		Route route = null;
		try {
			QueryBuilder<Route, Integer> queryBuilder = routeDao.queryBuilder();
			queryBuilder.where().eq("routeMark", routeMarkValue);
			route = queryBuilder.queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return route;
	}

	//schedule
	public void insertSchedule(Schedule s) {
		Schedule schedule = s;

		try {
			scheduleDao = getScheduleDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			scheduleDao.create(schedule);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertRoute()", "Error inserting route in database");
		}
	}

	public void insertSchedule(HashSet<Schedule> s) {
		HashSet<Schedule> schedules = s;

		try {
			scheduleDao = getScheduleDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			for (Schedule schedule : schedules) {
				scheduleDao.create(schedule);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertRoute()", "Error inserting schedule in database");
		}
	}


	public List<Schedule> getAllSchedules() {
		List<Schedule> schedules = new ArrayList<Schedule>();

		try {
			schedules = scheduleDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getAllRoutes()", "Error fatching all routes");
		}

		try {
			for (int i= 0; i < schedules.size(); i++) {
				station_routeDao.refresh(schedules.get(i).getStation_route());
				Station_route station_route = schedules.get(i).getStation_route();
				stationDao.refresh(station_route.getStation());
				routeDao.refresh(station_route.getRoute());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getAllStation_routes()", "Error filling stations or routes");
		}

		return schedules;
	}

	//station_route
	public void insertStation_route(Station_route srt) {
		Station_route station_route = srt;

		try {
			station_routeDao = getStation_routeDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			station_routeDao.create(station_route);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertRoute()", "Error inserting route in database");
		}
	}

	public void insertStation_route(HashSet<Station_route> srt) {
		HashSet<Station_route> station_routes = srt;

		try {
			station_routeDao = getStation_routeDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			for(Station_route station_route : station_routes) {
				station_routeDao.create(station_route);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("insertRoute()", "Error inserting route in database");
		}
	}

	public Station_route getStation_routeById(Integer id) {
		Station_route station_route = null;
		try {
			station_route = station_routeDao.queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getStation_routeById()", "Error getting station_route with id" + id);
		}
		return station_route;
	}

	//our station_route will only have id set in foreign object.s If we want them filled we
	//ned to call dao.refresh method with station_route.getClass() paramete	r.
	public List<Station_route> getAllStation_routes() {
		List<Station_route> station_routes = new ArrayList<Station_route>();

		try {
			station_routes = station_routeDao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getAllStation_routes()", "Error fatching all station_routes");
		}

		try {
			for (int i= 0; i < station_routes.size(); i++) {
				stationDao.refresh(station_routes.get(i).getStation());
				routeDao.refresh(station_routes.get(i).getRoute());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e("getAllStation_routes()", "Error filling stations or routes");
		}

		return station_routes;
	}

	public Station_route queryStation_route_specific1(String stationId, String routeMarkValue,
													  Character direction, Boolean fill) {
		Station_route station_route = null;
		try {
			QueryBuilder<Station_route, Integer> queryBuilderStation_route = station_routeDao.
					queryBuilder();
			queryBuilderStation_route.where().eq("direction", direction);
			QueryBuilder<Route, Integer> queryBuilderRoute = routeDao.queryBuilder();
			queryBuilderRoute.where().eq("routeMark", routeMarkValue);
			QueryBuilder<Station, Integer> queryBuilderStation = stationDao.queryBuilder();
			queryBuilderStation.where().eq("id", stationId);
			station_route = queryBuilderStation_route.join(queryBuilderRoute).
					join(queryBuilderStation).queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (fill == Boolean.TRUE && station_route != null) {
			try {
				routeDao.refresh(station_route.getRoute());
				stationDao.refresh(station_route.getStation());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return station_route;
	}

	public List<Station_route> queryStation_route_specific2(String routeMarkValue) {
		List<Station_route> station_routes = null;
		try {
			QueryBuilder<Station_route, Integer> queryBuilderStation_route = station_routeDao.queryBuilder();
			QueryBuilder<Route, Integer> queryBuilderRoute = routeDao.queryBuilder();
			queryBuilderRoute.where().eq("routeMark", routeMarkValue);
			station_routes = queryBuilderStation_route.join(queryBuilderRoute).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (Station_route station_route : station_routes) {
			try {
				routeDao.refresh(station_route.getRoute());
				stationDao.refresh(station_route.getStation());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return station_routes;
	}

	public List<Station_route> queryStation_route_specific3(Station station) throws SQLException {
		if (station == null) {
			return null;
		}

		List<Station_route> station_routes = new ArrayList<Station_route>();
		station_routes = station_routeDao.queryForEq("station_id", station.getId());

		for (Station_route station_route : station_routes) {
			routeDao.refresh(station_route.getRoute());
			stationDao.refresh(station_route.getStation());
		}

		return station_routes;
	}

	/*public List<Station_route> queryStation_route_specific3(double gpsx, double gpsy) {

	}*/

}

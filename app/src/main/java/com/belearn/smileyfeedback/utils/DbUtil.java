package com.belearn.smileyfeedback.utils;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import com.belearn.smileyfeedback.model.Location;
import com.belearn.smileyfeedback.model.Question;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class DbUtil {
    private static final String IP = "192.168.0.22:1433";
    //private static final String IP = "127.0.0.1:1433";
    private static final String DATABASE = "EmoticonFeedback";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "SQL";

    private static final String CREATE_FEEDBACK = "{call CreateFeedback(?, ?, ?)}";
    private static final String CREATE_QUESTION = "{call CreateQuestion(?)}";
    private static final String ARCHIVE_QUESTION = "{call ArchiveQuestion(?)}";
    private static final String SELECT_ACTIVE_QUESTIONS = "{call SelectActiveQuestions}";
    private static final String CREATE_LOCATION = "{call CreateLocation(?)}";
    private static final String ARCHIVE_LOCATION = "{call ArchiveLocation(?)}";
    private static final String SELECT_ACTIVE_LOCATIONS = "{call SelectActiveLocations}";

    private static final String ID_QUESTION = "IDQuestion";
    private static final String ID_LOCATION = "IDLocation";
    private static final String TEXT= "Text";
    private static final String ACTIVE = "Active";

    private DbUtil() { }

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    private static Connection getConnection()
    {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(getConnectionURL());
        } catch (SQLException se) {
            Log.e(DbUtil.class.getName(), se.getMessage());
        } catch (Exception e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        }
        return conn;
    }

    private static String getConnectionURL() {
        return String.format("jdbc:jtds:sqlserver://%s;databaseName=%s;user=%s;password=%s;", IP, DATABASE, USERNAME, PASSWORD);
    }

    public static int createFeedback(int questionId, int locationId, int grade) {
        try (Connection con = getConnection()){
            CallableStatement callableStatement = con.prepareCall(CREATE_FEEDBACK);
            callableStatement.setInt(1, questionId);
            callableStatement.setInt(2, locationId);
            callableStatement.setInt(3, grade);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        } catch (Exception e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        }
        return 0;
    }

    public static int createQuestion(String text) {
        try (Connection con = getConnection()){
            CallableStatement callableStatement = con.prepareCall(CREATE_QUESTION);
            callableStatement.setString(1, text);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
            return 0;
        }
    }

    public static int archiveQuestion(int id) {
        try (Connection con = getConnection()){
            CallableStatement callableStatement = con.prepareCall(ARCHIVE_QUESTION);
            callableStatement.setInt(1, id);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
            return 0;
        }
    }

    public static List<Question> selectActiveQuestions() {
        List<Question> list = new ArrayList<>();
        try {
            CallableStatement callableStatement = getConnection().prepareCall(SELECT_ACTIVE_QUESTIONS);
            ResultSet result = callableStatement.executeQuery();
            while(result.next()) {
                list.add(new Question(
                        result.getInt(ID_QUESTION),
                        result.getString(TEXT),
                        result.getInt(ACTIVE)
                ));
            }
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        }
        return list;
    }

    public static int createLocation(String text) {
        try (Connection con = getConnection()){
            CallableStatement callableStatement = con.prepareCall(CREATE_LOCATION);
            callableStatement.setString(1, text);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
            return 0;
        }
    }

    public static int archiveLocation(int id) {
        try (Connection con = getConnection()){
            CallableStatement callableStatement = con.prepareCall(ARCHIVE_LOCATION);
            callableStatement.setInt(1, id);
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
            return 0;
        }
    }

    public static List<Location> selectActiveLocations() {
        List<Location> list = new ArrayList<>();
        try {
            CallableStatement callableStatement = getConnection().prepareCall(SELECT_ACTIVE_LOCATIONS);
            ResultSet result = callableStatement.executeQuery();
            while(result.next()) {
                list.add(new Location(
                        result.getInt(ID_LOCATION),
                        result.getString(TEXT),
                        result.getInt(ACTIVE)
                ));
            }
        } catch (SQLException e) {
            Log.e(DbUtil.class.getName(), e.getMessage());
        }
        return list;
    }
}

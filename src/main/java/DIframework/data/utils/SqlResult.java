package DIframework.data.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author yjt
 */
public class SqlResult {
    private final ResultSet r;

    public SqlResult(ResultSet r){
        this.r = r;
    }

    public int getInt(int columnIndex){
        try {
            return r.getInt(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getLong(int columnIndex){
        try {
            return r.getLong(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getDouble(int columnIndex){
        try {
            return r.getDouble(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getString(int columnIndex){
        try {
            return r.getString(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean next(){
        try {
            return r.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean getBoolean(int columnIndex){
        try {
            return r.getBoolean(columnIndex);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isLast(){
        try {
            return r.isLast();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

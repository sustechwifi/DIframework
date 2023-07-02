package DIframework.data.utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class SqlFactory {

    private static final DataAdapter adapter = new DataAdapter();

    private static <O> O loadCondition(String sql, Object[] conditions,
                                       BiFunction<Connection, PreparedStatement, O> f)
            throws SQLException {
        Connection connection = JdbcUtil.getConnection();
        var p = connection.prepareStatement(adapter.adaptSql(sql));
        if (conditions != null) {
            for (int i = 0; i < conditions.length; i++) {
                adapter.write(p, conditions[i], i + 1);
            }
        }
        return f.apply(connection, p);
    }

    public static SqlResult handleQuery(String sql, Object... conditions) throws SQLException {
        return loadCondition(sql, conditions, (con, p) -> {
            try {
                return new SqlResult(p.executeQuery());
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static boolean handleUpdate(String sql, Object... conditions) {
        try {
            return loadCondition(sql, conditions, (con, p) -> {
                try {
                    p.executeUpdate();
                    con.commit();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
       以集合返回
     */
    static <I> List<I> handleMultipleResult(SqlResult resultSet,
                                                   Function<SqlResult, I> map) {
        var tmp = new ArrayList<I>();
        try {
            if (resultSet.next()) {
                do {
                    tmp.add(map.apply(resultSet));
                } while (resultSet.next());
            }
        } catch (Exception e) {
            var msg = "无法从 SqlResult 映射到指定返回值";
            throw new RuntimeException(msg);
        }
        return tmp;
    }

    /*
       自定义返回值处理函数
     */
    static <I, O> O handleMultipleResult(SqlResult resultSet,
                                                Function<SqlResult, I> map,
                                         Function<Collection<I>, O> transform) {
        var tmp = new ArrayList<I>();
        try {
            if (resultSet.next()) {
                do {
                    tmp.add(map.apply(resultSet));
                } while (resultSet.next());
            }
            return transform.apply(tmp);
        } catch (Exception e) {
            var msg = "无法从 SqlResult 映射到指定返回值";
            throw new RuntimeException(msg);
        }
    }

    static <O> O handleSingleResult(SqlResult resultSet, Function<SqlResult, O> map) {
        try {
            if (resultSet.next()) {
                return map.apply(resultSet);
            } else {
                return null;
            }
        } catch (Exception e) {
            var msg = "无法从 SqlResult 映射到指定返回值";
            throw new RuntimeException(msg);
        }
    }


    // 以下是手动查询

    public static <O> O singleQuery(String sql, Function<SqlResult, O> map, Object... args) {
        try {
            return handleSingleResult(handleQuery(sql, args), map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <I, O> O multipleQuery(String sql,
                                         Function<SqlResult, I> map,
                                         Function<Collection<I>, O> transform,
                                         Object... args) {
        try {
            return handleMultipleResult(handleQuery(sql, args), map, transform);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <I> List<I> multipleQuery(String sql,
                                            Function<SqlResult, I> map,
                                            Object... args) {
        try {
            return handleMultipleResult(handleQuery(sql, args), map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Function<SqlResult,?> getMappingFunction(Class<?> clazz){
        if (adapter.supportReturnType(clazz)){
            return adapter.getMappingFunction(clazz);
        }else {
            throw new IllegalArgumentException("不支持推断的类型");
        }
    }
}

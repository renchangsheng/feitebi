/**
 * 
 */
package com.bdh.db.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;

/**
 * 
 * 
 * 
 * @author Mangoo
 * 
 */
public class DBUtil {

    public static String DB_URI_DEV = "jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf-8";

    public static String DB_URI_DIST = "jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf-8";

    public static String DB_USER = "root";

    public static String DB_PWD = "123456";

    public static PooledDataSource pooledDataSource = null;

    public static PooledDataSource getPooledDataSource() {
        return pooledDataSource;
    }

    public static DBUtil instance = null;

    private DBUtil() {
    }

    public static DBUtil getInstance() {
        if (SysUtil.SYS_MODE_OF_PRODUCT) {
            return DBUtil.getInstance(DB_URI_DIST, DB_USER, DB_PWD);
        } else {
            return DBUtil.getInstance(DB_URI_DEV, DB_USER, DB_PWD);
        }
    }

    public static DBUtil getInstance(boolean bigPool) {
        if (SysUtil.SYS_MODE_OF_PRODUCT) {
            return DBUtil.getInstance(DB_URI_DIST, DB_USER, DB_PWD, bigPool);
        } else {
            return DBUtil.getInstance(DB_URI_DEV, DB_USER, DB_PWD, bigPool);
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        DBUtil util = null;
        if (SysUtil.SYS_MODE_OF_PRODUCT) {
            util = DBUtil.getInstance(DB_URI_DIST, DB_USER, DB_PWD);
        } else {
            util = DBUtil.getInstance(DB_URI_DEV, DB_USER, DB_PWD);
        }

        for (int i = 0; i < 150; i++) {
            Connection conn = util.getConnection();
            Thread.sleep(100);
            System.out.println(conn.toString());
            util.close(conn);
        }
    }

    public static DBUtil getInstance(String uri, String uid, String pwd) {
        if (instance == null) {
            instance = new DBUtil();
            try {
                instance.createPooledDataSource(uri, uid, pwd, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public <T> List<T> convert(ResultSet rs, Class<T> type) {
        NoNullBeanProcessor bp = new NoNullBeanProcessor();
        try {
            return bp.toBeanList(rs, type);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<T>(0);
    }

    public static DBUtil getInstance(String uri, String uid, String pwd, boolean bigPool) {
        if (instance == null) {
            instance = new DBUtil();
            try {
                instance.createPooledDataSource(uri, uid, pwd, bigPool);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private void createPooledDataSource(String uri, String uid, String pwd, boolean bigPool)
            throws PropertyVetoException, SQLException {
        if (pooledDataSource == null) {

            // Acquire the DataSource... this is the only c3p0 specific code
            // here
            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass("com.mysql.jdbc.Driver");
            cpds.setJdbcUrl(uri);
            cpds.setUser(uid);
            cpds.setPassword(pwd);
            if (bigPool) {
                cpds.setAcquireIncrement(1);
                cpds.setMaxPoolSize(1);
                cpds.setInitialPoolSize(1);
                cpds.setNumHelperThreads(1);
            } else {
                cpds.setAcquireIncrement(1);
                cpds.setMaxPoolSize(1);
                cpds.setInitialPoolSize(1);
                cpds.setNumHelperThreads(1);
            }

            cpds.setBreakAfterAcquireFailure(true);
            cpds.setTestConnectionOnCheckin(true);
            cpds.setTestConnectionOnCheckout(true);

            cpds.setAutoCommitOnClose(true);
            pooledDataSource = cpds;
        } else {
            // pooledDataSource.hardReset();
        }
    }

    public Connection getConnection() throws SQLException {
        if (pooledDataSource == null) {
            try {
                if (SysUtil.SYS_MODE_OF_PRODUCT) {
                    instance.createPooledDataSource(DB_URI_DIST, DB_USER, DB_PWD, true);
                } else {
                    instance.createPooledDataSource(DB_URI_DEV, DB_USER, DB_PWD, true);
                }
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        }
        return pooledDataSource.getConnection();
    }

    /**
     * close().
     */
    public void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * executeQuery().
     * 
     * @throws DisconnectionException
     */
    public static ResultSet executeQuery(Connection conn, String query) throws SQLException {
        ResultSet result = null;
        Statement stmt = null;

        // Check conn
        if (conn == null) {
            throw (new SQLException("conn is null"));
        }

        try {
            // Statement
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Ok, save
            result = rs;

            // Release resources immediately
        } catch (SQLException ex) {
            throw new SQLException(ex);
        }
        return result;
    }

    /**
     * executeUpdate().
     * 
     * @throws DisconnectionException
     */
    public boolean executeUpdate(Connection conn, String query) throws SQLException {
        boolean bResult = false;
        Statement stmt = null;

        // Check conn
        if (conn == null) {
            throw (new SQLException("conn is null"));
        }

        try {
            // Statement
            stmt = conn.createStatement();
            stmt.executeUpdate(query);

            // OK
            bResult = true;

            // Release resources immediately
            stmt.close();
            stmt = null;
        } catch (SQLException ex) {
            throw new SQLException(ex);
        } finally {
            close(stmt);
        }

        return bResult;
    }

    /**
     * beginTransaction().
     * 
     * @throws DisconnectionException
     */
    public void beginTransaction(Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("conn is null");
        }
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    public void begin(Connection conn) throws SQLException {
        beginTransaction(conn);
    }

    /**
     * rollback().
     * 
     * @throws DisconnectionException
     */
    public void rollback(Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("conn is null");
        }
        conn.rollback();
        if (!conn.getAutoCommit()) {
            conn.setAutoCommit(true);
        }
    }

    /**
     * commit().
     * 
     * @throws DisconnectionException
     */
    public void commit(Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("conn is null");
        }
        if (!conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void end(Connection conn) throws SQLException {
        commit(conn);
    }

    /**
     * close().
     */
    public void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
                statement = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}

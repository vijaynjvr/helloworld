//
//  DISCLAIMER OF WARRANTIES.
//  The following [enclosed] code is sample code created by IBM
//  Corporation. This sample code is not part of any standard or IBM
//  product and is provided to you solely for the purpose of assisting
//  you in the development of your applications.  The code is provided
//  "AS IS". IBM MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT
//  NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE, REGARDING THE FUNCTION OR PERFORMANCE OF
//  THIS CODE.  THIS CODE MAY CONTAIN ERRORS.  IBM shall not be liable
//  for any damages arising out of your use of the sample code, even
//  if it has been advised of the possibility of such damages.
//
import java.sql.*;

import javax.sql.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class jdbctest {

	public String driverclassname;
	public String DBURL;
	public String dbprefix = "jdbc:";
	public Connection con;
	public Statement stmt;
	public BufferedReader instream;
	public long operationTimer;

	public String response = "0";

	public String globaluid = null;
	public String globalpwd = null;

	public String dbhostname;
	public String dbport;
	public String  dbname;

	public	ClassLoader loader=null;
	public Class dsClass=null;
	ConnectionPoolDataSource ds=null;
	
	public static final int DATASOURCECONNECTIONS=10;


	// Capture window events...
	static private class FrameCloser extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			System.out.println("windowClosing..." + we);
			we.getWindow().dispose();
			System.exit(0);
		}
	}

	// Capture key events...
	static private class KeyInterceptor implements KeyListener {
		public void keyTyped(java.awt.event.KeyEvent ke) {
		}
		public void keyPressed(java.awt.event.KeyEvent ke) {
		}
		public void keyReleased(java.awt.event.KeyEvent ke) {
			// System.out.println("keyReleased..." + ke);
			if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
				TextField tf = (TextField) ke.getComponent();
				synchronized (tf) {
					tf.notify();
				}
			} // if enter key is pressed
		}
	}

	public static void main(String args[]) {
		System.out.println("JDBC Test starting...");

		jdbctest ct = new jdbctest();
		ct.init(args); // Loads jdbc driver
		ct.connect(); // sets connection
		if (ct.con == null) {
			System.out.println("Connection to " + ct.DBURL + " failed!");
			System.exit(33);
		}

		System.out.println("Connection Successful: " + ct.con);
		System.out.println(
		"Connection took " + ct.operationTimer + " milliseconds to complete");
		try {
			System.out.println("AutoCommit is: " + ct.con.getAutoCommit());
		} catch (SQLException s) {
			s.printStackTrace();
			System.out.println("Error code is: " + s.getErrorCode());
			System.out.println("SQLState is: " + s.getSQLState());
			System.exit(33);
		}
		do {
			try {
				System.out.println("Create new statement...");
				if (!(ct.response.equals("11")))
				ct.stmt = ct.con.createStatement();
			} catch (SQLException se) {
				se.printStackTrace();
				System.out.println("Error code is: " + se.getErrorCode());
				System.out.println("SQLState is: " + se.getSQLState());
				System.exit(33);
			}
		} while (ct.process());
		try {
			if (ct.stmt != null) ct.stmt.close();
			ct.con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.exit ???
		System.exit(33);

	}

	public void init(String args[]) {
		instream = new BufferedReader((new InputStreamReader(System.in)));

		while (driverclassname == null) {
			System.out.println(
			"Please respond:\n\t1 - For DB2\n\t2 - For DB2/Pure Network Client\n\t3 - For Oracle (thin) \n\t4 - For Oracle (oci8)\n\t5 - For Informix\n\t6 - For Sybase\n\t7 - For Cloudscape - (Local)\n\t8 - For Cloudscape - (RMI)\n\t9 - For SQLServer (Microsoft Driver)\n\t10 - For SQLServer (SequeLink driver)\n\t11 - For SQLServer (WebSphere driver)\n");
			response = readLine();
			if (response.equals("1")) {
				driverclassname = "COM.ibm.db2.jdbc.app.DB2Driver";
				dbprefix += "db2:";
			} else
			if (response.equals("2")) {
				driverclassname = "COM.ibm.db2.jdbc.net.DB2Driver";
				dbprefix += "db2:";
			} else
			if (response.equals("3")) { //Thin Driver
				driverclassname = "oracle.jdbc.driver.OracleDriver";
				dbprefix += "oracle:thin:";
			} else
			if (response.equals("4")) { //Thick Driver
				driverclassname = "oracle.jdbc.driver.OracleDriver";
				dbprefix += "oracle:oci8:";
			}  else
			if (response.equals("5")) {
				driverclassname = "com.informix.jdbc.IfxDriver";
				dbprefix += "informix-sqli:";
			} else
			if (response.equals("6")) {
				driverclassname = "com.sybase.jdbc2.jdbc.SybDriver";
				dbprefix += "sybase:";
			} else
			if (response.equals("7")) {
				driverclassname = "COM.cloudscape.core.JDBCDriver";
				dbprefix += "cloudscape:";
			} else
			if (response.equals("8")) {
				driverclassname = "COM.cloudscape.core.RmiJdbcDriver";
				dbprefix += "cloudscape:rmi:";
			} else
			if (response.equals("9")) {
				driverclassname = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
				dbprefix += "microsoft:sqlserver:";
			} else
			if (response.equals("10")) {
				driverclassname = "com.merant.sequelink.jdbcx.datasource.SequeLinkDataSource";
				dbprefix += "sequelink:";
			} else
			if (response.equals("11")) {
				driverclassname = "com.ibm.websphere.jdbcx.sqlserver.SQLServerDataSource";
				dbprefix += "sequelink:";
			} else
			System.out.println("Invalid Selection, specify an option 1-11.");
		}


		System.out.println("Loading jdbc driver: " + driverclassname);
		try {
			if (Integer.parseInt(response) < DATASOURCECONNECTIONS)
			{
				Class.forName(driverclassname);
				System.out.println(driverclassname + " was loaded successfully");
			}
			else
			{loader = Thread.currentThread().getContextClassLoader();
			if (loader == null)
			loader = ClassLoader.getSystemClassLoader();

			dsClass=loader.loadClass(driverclassname);
		}

	} catch (Exception e) {
		e.printStackTrace();
		System.out.println(
		"Please modify you classpath to include the class: " + driverclassname);

		System.out.println(
		"To be sure that you have this class in your classpath, issue:\n javap "
		+ driverclassname);
		System.exit(33);
	}

}

public void connect() {
	while ((DBURL == null) && (Integer.parseInt(response) < DATASOURCECONNECTIONS)) {
		System.out.println("Please enter connection URL info, e.g:");

		if (driverclassname.endsWith(".app.DB2Driver")) {
			System.out.println("jdbc:db2:dbname or dbname");
		} else
		if (driverclassname.endsWith(".net.DB2Driver")) {
			System.out.println(
			"jdbc:db2://<serverName>:<port>/<dbname> or //<serverName>:<port>/<dbName>");
		} else
		if (driverclassname.endsWith(".OracleDriver")) {

			if (response.equals("4")) {
				System.out.println(
				"jdbc:oracle:oci8:@<hostname>:<port>:<dbname> or @<hostname>:<port>:<dbname>");
			} else {
				System.out.println(
				"jdbc:oracle:thin:@<hostname>:<port>:<dbname> or @<hostname>:<port>:<dbname>");
			}

		} else
		if (driverclassname.endsWith(".IfxDriver"))
		System.out.println(
		"jdbc:informix-sqli://<hostname>:<port>/<dbname>:INFORMIXSERVER=<ifxservername>");
		else
		if (driverclassname.endsWith(".SybDriver"))
		System.out.println("jdbc:sybase:<dbProtocol>:<hostname>:<port>/<dbname>");

		else
		if (driverclassname.endsWith(".SQLServerDriver"))
		System.out.println("jdbc:microsoft:sqlserver://<hostname>:<port>;DatabaseName=<dbname>");
		else
		if (driverclassname.endsWith(".JDBCDriver"))
		System.out.println("jdbc:cloudscape:<dbname>;create=<true|false>");
		else
		if (driverclassname.endsWith(".RmiJdbcDriver"))
		System.out.println("jdbc:cloudscape:rmi://<hostname>:<rmi port>/<dbname> or //<hostname>:<rmi port>/<dbname");

		if (!(driverclassname.endsWith("SequeLinkDriver")))
		DBURL = readLine();
		else
		DBURL = "";
	}

	if (DBURL==null) DBURL="";

	if (!DBURL.startsWith(dbprefix))
	DBURL = dbprefix + DBURL;


	String userid = null;
	{
		System.out.println("Please enter userid for connection to " + DBURL);
		userid = readLine();
		System.out.println("userid is: '" + userid + "'");
	}
	String password = null;
	if (userid != null && !userid.equals("")) {
		System.out.println(
		"Please enter password   "
		+ " =====>  WARNING: PASSWORD NOT HIDDEN   <====== ");
		System.out.println(
		"enter 'gui' instead of you password for a secure GUI prompt)");
		password = readLine();
		if (password.equalsIgnoreCase("gui"))
		password = getPassword(userid);
	}

	globaluid = userid;
	globalpwd = password;

	try {

		//per L3, DataSource approach is used with WS 4.x, so let's do it the same way
		if (Integer.parseInt(response) >= DATASOURCECONNECTIONS) {
			con = this.getDataSourceConnection(userid, password);
		} else { //non-SQLServer

			if (userid == null || userid.equals("")) {
				long start = System.currentTimeMillis();
				con = DriverManager.getConnection(DBURL);
				operationTimer = System.currentTimeMillis() - start;
			} else {
				long start = System.currentTimeMillis();
				con = DriverManager.getConnection(DBURL, userid, password);
				operationTimer = System.currentTimeMillis() - start;
			}

		}
	} catch (SQLException se) {
		se.printStackTrace();
		System.out.println("Error code is: " + se.getErrorCode());
		System.out.println("SQLState is: " + se.getSQLState());
		// See if we can help the user with diagnosing the problem:
		boolean network_connect_problem = false;

		if (driverclassname.equals("oracle.jdbc.driver.OracleDriver")) {
			if ((se.getSQLState() == null) && (se.getErrorCode() == 17002)) {
				System.out.println("Connection to the Oracle server failed.\n");
				System.out.println(
				"Unable to connect to @hostname:port specified in the DB URL.");
				network_connect_problem = true;
			}
		}

		if (driverclassname.equals("COM.ibm.db2.jdbc.net.DB2Driver")) {
			if ((se.getSQLState() == "08S01") && (se.getErrorCode() == -99999)) {
				System.out.println("Connection to the DB2 server failed.\n");
				System.out.println(
				"Unable to connect to //hostname:port specified in the DB URL.");
				System.out.println(
				"Verify that db2jstrt is running on the target host with the specified port as a parameter...");
				network_connect_problem = true;
			}
		}

		if (network_connect_problem) {
			System.out.println(
			"Verify that the host and port that you are connecting to are correct.");
			System.out.println(
			"Also verify that the host you are connecting to is listening on the specified port.");
			System.out.println("Hint: use telnet hostname port.");
			System.out.println(
			"\tIf you get connection refused then the host is not listening on the specified port.");
			System.out.println(
			"\tIf telnet simply hangs then the host is listening on the port.");
			System.out.println();
		}

	} // End catch connect exception

}

private Connection getDataSourceConnection(String userid, String password) {
	PooledConnection pooledConn = null;
	Connection conn = null;


	try {
		/*loader = Thread.currentThread().getContextClassLoader();
		if (loader == null)
		loader = ClassLoader.getSystemClassLoader();

		if (response.equals("10"))
		dsClass =
		loader.loadClass("com.merant.sequelink.jdbcx.datasource.SequeLinkDataSource");

		else if (response.equals("11"))
		dsClass =
		loader.loadClass("com.ibm.websphere.jdbcx.sqlserver.SQLServerDataSource");

		dsClass=loader.loadClass(driverclassname);

		*/

		ds = (ConnectionPoolDataSource) (dsClass.newInstance());

		System.out.println("enter database name:");
		dbname = readLine();

		dsClass.getMethod("setDatabaseName", new Class[] { String.class }).invoke(
		ds,
		new Object[] { dbname });

		System.out.println("enter port number:");
		dbport = readLine();

		dsClass.getMethod("setPortNumber", new Class[] { int.class }).invoke(
		ds,
		new Object[] { new Integer(dbport)});

		System.out.println("enter server name:");
		dbhostname = readLine();

		dsClass.getMethod("setServerName", new Class[] { String.class }).invoke(
		ds,
		new Object[] { dbhostname });

		dsClass.getMethod("setUser", new Class[] { String.class }).invoke(
		ds,
		new Object[] { userid });

		dsClass.getMethod("setPassword", new Class[] { String.class }).invoke(
		ds,
		new Object[] { password });

		long start = System.currentTimeMillis();
		pooledConn = ds.getPooledConnection();
		conn = pooledConn.getConnection();
		operationTimer = System.currentTimeMillis() - start;

		// Any use of the Connection must be done AFTER the unlock, or else it will fail
		// with a license verification error.

	} catch (SQLException se) {
		se.printStackTrace();
		System.out.println("Error code is: " + se.getErrorCode());
		System.out.println("SQLState is: " + se.getSQLState());
		// See if we can help the user with diagnosing the problem:

		System.out.println(
		"Verify that the host and port that you are connecting to are correct.");
		System.out.println(
		"Also verify that the host you are connecting to is listening on the specified port.");
		System.out.println("Hint: use telnet hostname port.");
		System.out.println(
		"\tIf you get connection refused then the host is not listening on the specified port.");
		System.out.println(
		"\tIf telnet simply hangs then the host is listening on the port.");
		//			System.out.println();

		return (null);

	} // End catch connect exception

	catch (Throwable th) {
		th.printStackTrace();
		return (null);
	} finally {
		//  if (conn != null)       try { conn.close(); }       catch (Throwable th) {}
		//  if (pooledConn != null) try { pooledConn.close(); } catch (Throwable th) {}

	}

	return (conn);

}


private Connection getDataSourceConnection() {
	PooledConnection pooledConn = null;
	Connection conn = null;

    System.out.println("getting data source connection...");


	try {

		long start = System.currentTimeMillis();
		pooledConn = ds.getPooledConnection();
		conn = pooledConn.getConnection();
		operationTimer = System.currentTimeMillis() - start;

		// Any use of the Connection must be done AFTER the unlock, or else it will fail
		// with a license verification error.

	} catch (SQLException se) {
		se.printStackTrace();
		System.out.println("Error code is: " + se.getErrorCode());
		System.out.println("SQLState is: " + se.getSQLState());
		// See if we can help the user with diagnosing the problem:

		System.out.println(
		"Verify that the host and port that you are connecting to are correct.");
		System.out.println(
		"Also verify that the host you are connecting to is listening on the specified port.");
		System.out.println("Hint: use telnet hostname port.");
		System.out.println(
		"\tIf you get connection refused then the host is not listening on the specified port.");
		System.out.println(
		"\tIf telnet simply hangs then the host is listening on the port.");
		//			System.out.println();

		return (null);

	} // End catch connect exception

	catch (Throwable th) {
		th.printStackTrace();
		return (null);
	} finally {
		//  if (conn != null)       try { conn.close(); }       catch (Throwable th) {}
		//  if (pooledConn != null) try { pooledConn.close(); } catch (Throwable th) {}

	}

	return (conn);

}



public String readLine() {
	String response = null;
	try {
		response = instream.readLine();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return response;
}

public boolean process() {

	String instring = null;
	if (!(response.equals("11")))
	{
		while (instring == null) {
			System.out.println(
			"Please enter sql statement to execute...(q to quit)\n(or maxconn to test maximum connections possible to this database)");
			instring = readLine();
			if (instring.equals("q")) {
				System.out.println("Ok, quitting!");
				return false;
			} else
			if (instring.equals("maxconn")) {
				System.out.println("testing for maximun connections...");
				maxconn();
				return true;
			}

		}

	}

	else //special case with DataDirect driver, test connect only

	{
		while ((instring == null) || (!instring.equals("q")) && (!instring.equals("maxconn"))) {
			System.out.println(
			"Please enter q to quit\n(or maxconn to test maximum connections possible to this database)");
			instring = readLine();

		}

		if (instring.equals("maxconn")) {
			System.out.println("testing for maximun connections...");
			maxconn();
			return true;
		}

		else
		if (instring.equals("q")) {
			System.out.println("Ok, quitting!");
			return false;			}

			else System.out.println("Jdbctest only supports connection testing for this driver.");


		}

		boolean rc = false;
		try {
			long start = System.currentTimeMillis();
			rc = stmt.execute(instring);
			operationTimer = System.currentTimeMillis() - start;
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(
		"Operation took " + operationTimer + " milliseconds to complete");
		ResultSet rs = null;

		System.out.println(
		"Just tried " + rc + " = stmt.execute(\"" + instring + "\");");

		if (rc) {
			try {
				System.out.println("Getting result set...");
				rs = stmt.getResultSet();
				ResultSetMetaData rsm = rs.getMetaData();
				// Display names of columns fetched
				int colcount = rsm.getColumnCount();
				System.out.println(colcount + " column(s) in result");
				int[] coltype = new int[colcount + 1]; // Do not slot 0
				for (int i = 1; i < colcount + 1; i++) {
					System.out.print(rsm.getColumnName(i) + "   ");
					coltype[i] = rsm.getColumnType(i);
				}

				System.out.println();
				System.out.println("-----------------------------------");

				while (rs.next()) {
					for (int j = 1; j < colcount + 1; j++) {
						if (j != 1)
						System.out.print(",");
						switch (coltype[j]) {
							case Types.TINYINT :
							System.out.print("" + rs.getShort(j));
							break;

							case Types.SMALLINT :
							System.out.print("" + rs.getShort(j));
							break;

							case Types.INTEGER :
							System.out.print("" + rs.getInt(j));
							break;

							case Types.BIGINT :
							System.out.print("" + rs.getLong(j));
							break;

							case Types.FLOAT :
							System.out.print("" + rs.getFloat(j));
							break;

							case Types.REAL :
							System.out.print("" + rs.getDouble(j));
							break;

							case Types.DOUBLE :
							System.out.print("" + rs.getDouble(j));
							break;

							case Types.NUMERIC :
							System.out.print("" + rs.getInt(j));
							break;

							case Types.DECIMAL :
							System.out.print("" + rs.getInt(j));
							break;

							case Types.CHAR :
							//              System.out.print(""+rs.getByte(j));
							System.out.print("" + rs.getString(j));
							break;

							case Types.VARCHAR :
							System.out.print("" + rs.getString(j));
							break;

							case Types.LONGVARCHAR :
							System.out.print("" + rs.getString(j));
							break;

							case Types.DATE :
							System.out.print("" + rs.getDate(j));
							break;

							case Types.TIME :
							System.out.print("" + rs.getTime(j));
							break;

							case Types.TIMESTAMP :
							System.out.print("" + rs.getTimestamp(j));
							break;

							case Types.BINARY :
							case Types.BIT :
							case Types.VARBINARY :
							case Types.LONGVARBINARY :
							byte b[] = rs.getBytes(j);
							for (int n = 0; n < b.length; n++)
							System.out.print("" + b[n] + "|");
							break;

							case Types.NULL :
							System.out.print("-");
							break;

							case Types.OTHER :
							System.out.print("OTHER");
							break;

							default :
							System.out.print("UNKNOWN-TYPE");
						}
					}
					System.out.println();

				}
				System.out.println();

				rs.close();
			} catch (SQLException se) {
				se.printStackTrace();
				System.out.println("Error code is: " + se.getErrorCode());
				System.out.println("SQLState is: " + se.getSQLState());
			}
		}

		return true;

	}

	String getPassword(String userid) {
		Frame f;
		Panel p;
		Label l;
		TextField tf;

		// Create the frame
		f = new Frame("Password Prompt for JDBC tester");
		f.setLocation(400, 400);
		f.setSize(350, 100);
		// Reister ourselves for 'Close' events
		f.addWindowListener(new FrameCloser());

		// Create the panel
		p = new Panel();
		f.add(p, "Center");
		l = new Label("Please enter password for Data Base user: " + userid);
		tf = new TextField(20);
		tf.setEchoChar('*');
		p.add(l, "Center");
		p.add(tf, "Center");
		try {
			f.show();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(
			"Your ===> DISPLAY <=== Environment Variable is not exported properly.  Please correct and retry.");
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
		}
		f.show();
		tf.requestFocus();
		tf.addKeyListener(new KeyInterceptor());

		//   System.out.println("Waiting for password to be entered...");

		synchronized (tf) {
			try {
				tf.wait();
			} catch (InterruptedException e) {
			}
		}
		String password = tf.getText();
		f.transferFocus();
		f.dispose();
		return password;
	}

	public void maxconn() {

		Connection c[] = new Connection[125];

		System.out.println("1 connection assmued...looping...");

		int i;

		for (i = 0; i < 125; i++)
		try {
			if (globaluid == null || globaluid.equals("")) {

				if (Integer.parseInt(response) < DATASOURCECONNECTIONS)
				{
					long start = System.currentTimeMillis();
					c[i] = DriverManager.getConnection(DBURL);
					operationTimer = System.currentTimeMillis() - start;
				}
				else c[i]= getDataSourceConnection();
			} else {
				if (Integer.parseInt(response) < DATASOURCECONNECTIONS)
				{

					long start = System.currentTimeMillis();
					c[i] = DriverManager.getConnection(DBURL, globaluid, globalpwd);
					operationTimer = System.currentTimeMillis() - start;
				}

				else c[i]= getDataSourceConnection();

			}

			System.out.println("Connection Successful: " + c[i]);
			System.out.println(
			"Connection "
			+ (i + 2)
			+ " took "
			+ operationTimer
			+ " milliseconds to complete");
		} catch (SQLException se) {
			se.printStackTrace();
			System.out.println("Error code is: " + se.getErrorCode());
			System.out.println("SQLState is: " + se.getSQLState());
			i--;
			break;
		}

		System.out.println(
		"Maximum connections to DB:" + DBURL + " is " + (i + 2) + ".");
		System.out.println("closing all connections except initial connection...");

		for (i = 0; i < 125; i++)
		if (c[i] != null)
		try {
			c[i].close();
			System.out.print(".");
		} catch (SQLException se2) {
			se2.printStackTrace();
			System.out.println("Error code is: " + se2.getErrorCode());
			System.out.println("SQLState is: " + se2.getSQLState());
		}
		System.out.println("Done!");

	}

}

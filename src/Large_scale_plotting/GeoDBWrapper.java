package Large_scale_plotting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.sql.Statement;

//import com.mysql.jdbc.Driver; /* for MySQL */

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
//import snaq.db.ConnectionPool; /* To use DBPool */
import org.postgresql.ds.PGPoolingDataSource;

public class GeoDBWrapper
{
	Connection myConnection = null;
	Statement stmt = null;

	static String countPrefix = "count(FIT_ID) from fit_installations WHERE installation_type = 'Domestic' ";
	static String capPrefix = "sum(declared_net_capacity_kw) from fit_installations WHERE installation_type = 'Domestic' ";
	static String instDate = " AND commissioned_date ";
	static DateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
	static String LLSOA_ID_FIELD = "llsoa_code";
	static String Pcode_d_FIELD = "post_code_district";

	private String ID_field;

	private HashMap<String, Integer> thisCount;
	private HashMap<String, Double> thisCap;
	private HashMap<String, Integer> households;
	private HashMap<String, Integer> population;
	//private HashMap<String, Integer> priorCount;
	//private HashMap<String, Double> priorCap;

	private GregorianCalendar myCal;
	private String dbURL;
	private String user;
	private String pword;
	private HashMap<String, Double> area;

	@ScheduledMethod(start = 0, interval = 1, shuffle = true, priority = 200)
	public void updateStats()
	{
		//this.priorCount = this.thisCount;
		//this.priorCap = this.thisCap;

		Date thisDay = getMyCal().getTime();

		String q1 = "SELECT " + this.ID_field + ", " + GeoDBWrapper.countPrefix + GeoDBWrapper.instDate + " <= DATE('" + myFormat.format(thisDay) + "') GROUP BY " + this.ID_field;
		String q2 = "SELECT " + this.ID_field + ", " + GeoDBWrapper.capPrefix + GeoDBWrapper.instDate + " <= DATE('" + myFormat.format(thisDay) + "') GROUP BY " + this.ID_field;

		
		try
		{
			if (this.myConnection.isClosed())
			{
				openConnection();
			}
		} catch (SQLException e1)
		{
			System.err.println("Exception thrown from testing isClosed() - attempt to reopen connection");
			openConnection();
		}
		
		try
		{
			this.stmt = this.myConnection.createStatement();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.thisCount = countsToHashmap(q1);
		this.thisCap = resultsToHashmap(q2);
		
		//System.out.println("countMap : " + this.thisCount.size() + ":" + this.thisCount.toString());
		//System.out.println("capMap : " + this.thisCap.size() + ":" + this.thisCount.toString());
	}

	private HashMap<String, Integer> countsToHashmap(String q)
	{
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		if (this.thisCount != null) ret.putAll(this.thisCount); // retain all values in case none in this step.
		try
		{
			ResultSet rs = this.stmt.executeQuery(q);
			int n = 0;
			if (rs.getWarnings() != null)
			{
				System.err.println(rs.getWarnings().getMessage());
			}
			while (rs.next())
			{
				ret.put(rs.getString(1), rs.getInt(2)); //relies on well formed results - note indices 1 based not 0 based.
				n++;
			}
			

		} catch (SQLException e)
		{
			System.err.println("Count query failed");
			System.err.println(q);
			e.printStackTrace();
		}
		

		return ret;
	}

	private HashMap<String, Double> resultsToHashmap(String q)
	{
		HashMap<String, Double> ret = new HashMap<String, Double>();
		if (this.thisCap != null) ret.putAll(this.thisCap);
		try
		{
			ResultSet rs = this.stmt.executeQuery(q);
			int n = 0;
			while (rs.next())
			{
				ret.put(rs.getString(1), rs.getDouble(2)); //relies on well formed results - note indices 1 based not 0 based.
				n++;
				//System.out.print(rs.getString(1)+ rs.getDouble(2)+",");
			}


		} catch (SQLException e)
		{
			System.err.println("sum query failed");
			System.err.println(q);
			e.printStackTrace();
		}
		
		return ret;
	}

	public int getCount(String UID)
	{
		Integer ret = this.thisCount.get(UID);		
		return ret == null ? 0 : ret.intValue();
	}

	public double getCapacity(String UID)
	{
		Double ret = this.thisCap.get(UID);		
		return ret == null ? 0 : ret.doubleValue();
	}

/*	public int getCountThisStep(String UID)
	{
		if ( this.priorCount == null)
		{
			return this.getCount(UID);
		}
		Integer ret = this.priorCount.get(UID);		
		return ret == null ? 0 : this.getCount(UID) - ret.intValue();
	}

	public double getCapacityThisStep(String UID)
	{
		if ( this.priorCap == null)
		{
			return this.getCapacity(UID);
		}
		Double ret = this.priorCap.get(UID);		
		return ret == null ? 0 : this.getCapacity(UID) - ret.doubleValue();

	}*/

	public GeoDBWrapper(String DBaddress, String server, String dbname, String user, String pword)
	{
		this.dbURL = DBaddress;
		this.user = user;
		this.pword = pword;

		// DataSource = null;

		PGPoolingDataSource source = new PGPoolingDataSource();
		source.setDataSourceName("PV data source");
		source.setServerName(server);
		source.setDatabaseName(dbname);
		source.setUser(user);
		source.setPassword(pword);
		source.setMaxConnections(10);		
		
		this.myConnection = null;
		try {
			this.myConnection = source.getConnection();
		} catch (SQLException e) {
			System.err.println("Got an SQL excecption initialising connection");
			System.err.println(e.getMessage());
			source.close();
			source = null;
		
			if (this.myConnection != null) {
		        try { this.myConnection.close(); } catch (SQLException f) {}
		} 
		}
		
		/* For MySQL
		try
		{
			DriverManager.registerDriver(new Driver());
		} catch (SQLException e)
		{
			System.err.println("Couldn't register driver with manager");
			e.printStackTrace();
		}
		*/

		/*
		 * DBPoolDataSource ds = new DBPoolDataSource(); ds.setName("pool-ds");
		 * ds.setDescription("Pooling DataSource");
		 * ds.setDriverClassName("com.mysql.jdbc.Driver");
		 * ds.setUrl("jdbc:mysql://146.227.24.38/jsnape_FiT");
		 * ds.setUser("jsnape"); ds.setPassword("sherpa12"); ds.setMinPool(5);
		 * ds.setMaxPool(100); ds.setMaxSize(0); ds.setExpiryTime(3600); //
		 * Specified in seconds.
		 * //ds.setValidationQuery("SELECT COUNT(*) FROM Replicants");
		 */

		// openConnection(); /* for mysql */

		// ConnectionPool cp = new ConnectionPool("local", 5, 100, 0, 100000,
		// "jdbc:mysql://146.227.24.38/jsnape_FiT", "jsnape", "sherpa12");
		// cp.init(50);

		try
		{
			this.stmt = this.myConnection.createStatement();
			this.stmt.setFetchSize(30000);
		} catch (SQLException e)
		{
			System.err.println("Couldn't create a statement object for the GeoDBWrapper to use");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void openConnection()
	{
		try
		{
			this.myConnection = DriverManager.getConnection(this.dbURL, this.user, this.pword);
		} catch (SQLException e1)
		{
			System.err.println("Getting connection to database failed");
			e1.printStackTrace();
		}
		
	}

	public void setID_field(String fieldName)
	{
		this.ID_field = fieldName;

	}

	/**
	 * @param myCal the myCal to set
	 */
	public void setMyCal(GregorianCalendar myCal)
	{
		this.myCal = myCal;
	}

	/**
	 * @return the myCal
	 */
	public GregorianCalendar getMyCal()
	{
		return myCal;
	}

	public void initialise()
	{
		String demographicsTable = "";
		String field = "";
		if (this.ID_field.equals(LLSOA_ID_FIELD))
		{
			demographicsTable = "LSOA_2001_demographics";
			field = "LSOA01CODE";
		}
		else
		{
			demographicsTable = "PCD_demographics";
			field = "Postcode_district";

		}		
		
		queryToStaticMaps(field, demographicsTable);
		
		this.updateStats();		
	}
	
	private void queryToStaticMaps(String field, String demographicsTable)
	{
		this.population = new HashMap<String, Integer>();
		this.households = new HashMap<String, Integer>();
		this.area = new HashMap<String, Double>();
	
		try
		{
			if (this.myConnection.isClosed())
			{
				openConnection();
			}
		} catch (SQLException e1)
		{
			System.err.println("Exception thrown from testing isClosed() - attempt to reopen connection");
			openConnection();
		}
		
		try
		{
			this.stmt = this.myConnection.createStatement();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		String q = "SELECT All_people,Area_Ha,All_households," + field + " from " + demographicsTable;
	
		try
		{
			ResultSet rs = this.stmt.executeQuery(q);
			int n = 0;
			while (rs.next())
			{
				this.population.put(rs.getString(4), Integer.valueOf(rs.getString(1))); //relies on well formed results - note indices 1 based not 0 based.
				this.area.put(rs.getString(4), Double.valueOf(rs.getString(2)));
				this.households.put(rs.getString(4), Integer.valueOf(rs.getString(3)));
				n++;
				//System.out.print(rs.getString(1)+ rs.getDouble(2)+",");
			}


		} catch (SQLException e)
		{
			System.err.println("sum query failed");
			System.err.println(q);
			e.printStackTrace();
		}
	}
	
	protected void finalize()
	{
		try
		{
			this.stmt.close();
			this.stmt = null;

			this.myConnection.close();
			this.myConnection = null;
		} catch (SQLException e)
		{
			System.err.println("Error while closing statement and connection");
			e.printStackTrace();
		}
	}
	
	/******************
	 * This method steps the model's internal gregorian calendar on each model
	 * tick
	 * 
	 * Input variables: none
	 * 
	 ******************/
	@ScheduledMethod(start = 0, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void calendarStep()
	{
		this.myCal.add(GregorianCalendar.DATE, 7);
	}

	public int getPopulation(String uid)
	{
			Integer ret = this.population.get(uid);		
			return ret == null ? 0 : ret.intValue();
	}
	public double getArea(String uid)
	{
			Double ret = this.area.get(uid);		
			return ret == null ? 0 : ret.doubleValue();
	}
	public int getHouseholds(String uid)
	{
			Integer ret = this.households.get(uid);		
			return ret == null ? 0 : ret.intValue();
	}

}

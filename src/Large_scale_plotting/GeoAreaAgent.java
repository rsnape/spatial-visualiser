package Large_scale_plotting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
//import snaq.db.ConnectionPool; /* from old MySQL implementation */
//import snaq.db.DBPoolDataSource; /* from old MySQL implementation */
import sun.jdbc.odbc.JdbcOdbcConnection;

public class GeoAreaAgent
{

	private String UID;
	private String ID_field;
	private String LSOA04CD;
	private String PCODEDIST;
	private GregorianCalendar myCal;
	private DataSource datasource;    //Old DB model
	private Connection dbConnection;	//Old DB model
	private Statement stmt;				//Old DB model

	
	private GeoDBWrapper dbWrapper;
	private int num_adoptions = 0;
	private double sum_capacity = 0;
	
	private int pop;
	private double area;
	private int HHcount;
	private double popDens;
	private double HHDens;
	private double capPerThousandPerson;
	private double numPerThousandPerson;
	private double capPerHousePerM2;
	
	/**
	 * @return the pop
	 */
	public int getPop()
	{
		return this.pop;
	}

	/**
	 * @return the hHcount
	 */
	public int getHHcount()
	{
		return this.HHcount;
	}

	/**
	 * @return the popDens
	 */
	public double getPopDens()
	{
		return this.popDens;
	}

	/**
	 * @return the hHDens
	 */
	public double getHHDens()
	{
		return this.HHDens;
	}

	/**
	 * @return the capPerThousandPerson
	 */
	public double getCapPerThousandPerson()
	{
		return this.capPerThousandPerson;
	}

	/**
	 * @return the numPerThousandPerson
	 */
	public double getNumPerThousandPerson()
	{
		return this.numPerThousandPerson;
	}



	/**
	 * @return the uID
	 */
	public String getUID()
	{
		return UID;
	}

	/**
	 * @param uID
	 *        the uID to set
	 */
	public void setUID(String uID)
	{
		UID = uID;
	}

	/**
	 * @return the iD_field
	 */
	public String getID_field()
	{
		return ID_field;
	}

	/**
	 * @param iD_field
	 *            the iD_field to set
	 */
	public void setID_field(String iD_field)
	{
		ID_field = iD_field;
	}

	/**
	 * @return the lLSOA_ID
	 */
	public String getLSOA04CD()
	{
		return LSOA04CD;
	}

	/**
	 * @param lLSOA_ID
	 *            the lLSOA_ID to set
	 */
	public void setLSOA04CD(String lLSOA_ID)
	{
		LSOA04CD = lLSOA_ID;
	}

	/**
	 * @return the pcode_district
	 */
	public String getPCODEDIST()
	{
		return PCODEDIST;
	}

	/**
	 * @param pcode_district
	 *            the pcode_district to set
	 */
	public void setPCODEDIST(String pcode_district)
	{
		PCODEDIST = pcode_district;
	}




	@ScheduledMethod(start = 0, interval = 1, shuffle = true, priority = 100)
	public void updateStats()
	{
		Date thisDay = myCal.getTime();


		this.num_adoptions = this.dbWrapper.getCount(this.UID);
		this.sum_capacity = this.dbWrapper.getCapacity(this.UID);
		
		this.capPerThousandPerson = this.sum_capacity * 1000.0 / this.pop;
		this.numPerThousandPerson = this.num_adoptions * 1000.0 / this.pop;
		this.capPerHousePerM2 = 10* this.sum_capacity / this.HHDens;
		//System.out.println(this.UID + " : Step complete - " + this.num_adoptions  + " panels capacity " + this.sum_capacity);
	}

	/**
	 * @return the num_adoptions
	 */
	public int getNum_adoptions()
	{
		return num_adoptions;
	}

	/**
	 * @return the sum_capacity
	 */
	public double getSum_capacity()
	{
		return sum_capacity;
	}

	public void initialise(GeoDBWrapper thisDB, GregorianCalendar simTime)
	//public void initialise(GregorianCalendar simTime)
	{
		this.dbWrapper = thisDB;
		this.myCal = simTime;

		if (this.getLSOA04CD() == null)
		{
			if (this.getPCODEDIST() == null)
			{
				System.err.println("Geo agent with neither Pcode or LLSOA ID");
			}

			this.setID_field(GeoDBWrapper.Pcode_d_FIELD);
			this.setUID(PCODEDIST);
		} else
		{
			// If it has an LSOA04CD, it must be an LLSOA shape, even if it has
			// a postcode too
			this.setID_field(GeoDBWrapper.LLSOA_ID_FIELD);
			this.setUID(LSOA04CD);

		}
		

		/*try
		{
			// Tried version with single connection in context, but became
			// serious bottle neck - easier one connection per agent.
			// Factor of hundreds or thousands times quicker.
			 this.dbConnection = DriverManager.getConnection("jdbc:mysql://146.227.24.38/jsnape_FiT","jsnape","sherpa12");
			//dbConnection = cp.getConnection();
			stmt = dbConnection.createStatement();
		} catch (SQLException e)
		{
			System.err.println("Couldn't create a statement object for agent " + this.getUID());
			e.printStackTrace();
		}

		Date thisDay = myCal.getTime();

		String q1 = countPrefix + ID_field + "= '" + UID + "' " + instDate + " < DATE('" + myFormat.format(thisDay) + "')";
		String q2 = capPrefix + ID_field + "= '" + UID + "' " + instDate + " < DATE('" + myFormat.format(thisDay) + "')";

		ResultSet rs;
		try
		{
			rs = stmt.executeQuery(q1);
			while (rs.next())
			{
				this.num_adoptions += rs.getInt(1);
			}
		} catch (SQLException e)
		{
			System.err.println("Statement was : " + q1);
			e.printStackTrace();
		}

		try
		{
			rs = stmt.executeQuery(q2);
			while (rs.next())
			{
				this.sum_capacity += rs.getDouble(1);
			}
		} catch (SQLException e)
		{
			System.err.println("Statement was : " + q2);
			e.printStackTrace();
		}*/

/*		try
		{
			stmt.close();

			stmt = null;

			dbConnection.close();
			dbConnection = null;
		} catch (SQLException e)
		{
			System.err.println("Error while closing statement and connection");
			e.printStackTrace();
		}*/

		this.num_adoptions = this.dbWrapper.getCount(this.UID);
		this.sum_capacity = this.dbWrapper.getCapacity(this.UID);
		this.pop = this.dbWrapper.getPopulation(this.UID);
		this.area = this.dbWrapper.getArea(this.UID);
		this.HHcount = this.dbWrapper.getHouseholds(this.UID);
		this.popDens = this.pop*1.0/this.area;
		this.HHDens = this.HHcount*1.0/this.area;
		this.capPerThousandPerson = sum_capacity * 1000.0 / this.pop;
		this.numPerThousandPerson = this.num_adoptions * 1000.0 / this.pop;
		this.capPerHousePerM2 = 10* this.sum_capacity / this.HHDens;
		
		//System.out.println("Agent " + UID + " initialised with " + num_adoptions + " domestic PV at capacity " + sum_capacity);

	}

	public GeoAreaAgent()
	{
	}

	/**
	 * @param capPerHousePerM2 the capPerHousePerM2 to set
	 */
	public void setCapPerHousePerM2(double capPerHousePerHectare)
	{
		this.capPerHousePerM2 = capPerHousePerHectare;
	}

	/**
	 * @return the capPerHousePerM2
	 */
	public double getCapPerHousePerM2()
	{
		return capPerHousePerM2;
	}

}

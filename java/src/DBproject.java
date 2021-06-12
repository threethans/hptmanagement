/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		
		//Assume that a doctor would not input their own information, this is all entered in by someone on the database team in the hospital so all needed attributes are known

		String a;
		String q = "SELECT *\nFROM Doctor;";
		int row;
		
		while (true) {
			try {
				System.out.print("Display current Doctor table (y/n)? ");
				a = in.readLine();
		
				if (a.equals("y")) {
					row = esql.executeQueryAndPrintResult(q);
				}
				break;
			}
	
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		int docID;
		String name;
		String specialty;
		int depID;
		String query;

		// docID
		while (true) {
			System.out.print("Please enter Doctor ID: ");
			try {
				docID = Integer.parseInt(in.readLine());
				break;
			}	

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;	
			}
		}

		// name
		while (true) {
			System.out.print("Please enter Doctor's full name: ");
			try {
				name = in.readLine();
				if (name.length() <= 0 || name.length() > 128) {
					throw new RuntimeException("Doctor's full name must be between 0 and 128 characters");
				}
				break;
			}
		
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// specialty
		while (true) {
			System.out.print("Please enter Doctor's specialty: ");
			try {
				specialty = in.readLine();
				if (specialty.length() <= 0 || specialty.length() > 24) {
					throw new RuntimeException("Doctor's specialty must be between 0 and 24 characters");
				}
				break;
			}
	
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// depID
		while (true) {
			System.out.print("Please enter Doctor's Department ID: ");
			try {
				depID = Integer.parseInt(in.readLine());
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//add to query
		try {
			query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (" + docID + ", \'" + name + "\', \'" + specialty + "\', " + depID + ");";
			esql.executeUpdate(query);

			System.out.print("Display updated Doctor table (y/n)? ");
			a = in.readLine();

			if (a.equals("y")) {
				row = esql.executeQueryAndPrintResult(q);
			}
		}

		catch (Exception e) {
			System.err.println("Query failed: " + e.getMessage());
		}

	}

	public static void AddPatient(DBproject esql) {//2

		//Assume that a patient would not input their own information, this is all entered in by someone on the database team in the hospital so all needed attributes are known
		int pID;
		String name;
		String gender;
		int age;
		String address;
		int numAppts;
		String query;

		String a;
		String q = "SELECT *\nFROM Patient;";
		int row;
	
		while (true) {
			try {
				System.out.print("Display current Patient table (y/n)? ");
				a = in.readLine();
			
				if (a.equals("y")) {
					row = esql.executeQueryAndPrintResult(q);

				}
				break;
			}
			
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// pID
		while (true) {
			System.out.print("Please enter Patient ID: ");
			try {
				pID = Integer.parseInt(in.readLine());
				break;
			}

			catch (Exception e) {
				System.out.println( "Invalid input! " + e.getMessage());
				continue;
			}
		}

		// name
		while (true) {
			System.out.print("Please enter Patient's full name: ");
			try {
				name = in.readLine();
				if (name.length() <= 0 || name.length() > 128) {
					throw new RuntimeException("Patient's full name must be between 0 and 128 characters");
				}
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// gender
		while (true) {
			System.out.print("Please enter Patient's gender: ");
			try {
				gender = in.readLine();
				if (gender.length() != 1) {
					throw new RuntimeException("Please enter either M or F for Patient's gender");
				}
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}
	
		// age
		while (true) {
			System.out.print("Please enter Patient's age: ");
			try {
				age = Integer.parseInt(in.readLine());
				if (age <= 0) {
					throw new RuntimeException(" Patient's age must be greater than 0");
				}
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// address
		while (true) {
			System.out.print("Please enter Patient's address: ");
			try {
				address = in.readLine();
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// number of appointments
		while (true) {
			System.out.print("Please enter number of appointments under Patient's name: ");
			try {
				numAppts = Integer.parseInt(in.readLine());
				break;
			}
		
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// put together query
		try {
			query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (" + pID + ", \'" + name + "\', \'" + gender + "\', " + age + ", \'" + address + "\', " + numAppts + ");";
			esql.executeUpdate(query);

			System.out.print("Display updated Patient table (y/n)? ");
			a = in.readLine();
	
			if (a.equals("y")) {
				row = esql.executeQueryAndPrintResult(q);
			}
		}

		catch (Exception e) {
			System.err.println("Query failed! " + e.getMessage());
		}
	}

	public static void AddAppointment(DBproject esql) {//3
		
		//Assume this is all entered in by someone on the database team in the hospital so all needed attributes are known
		int apptID;
		String date;
		String time;
		String status;
		String query;

		String a;
		String q = "SELECT *\nFROM Appointment";
		int row;

		while (true) {
			System.out.print("Display current Appointment table (y/n)? ");
			try {
				a = in.readLine();
				if (a.equals("y")) {
					row = esql.executeQueryAndPrintResult(q);
				}
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}	

		// apptID
		while (true) {
			System.out.print("Please enter Appointment ID: ");
			try {
				apptID = Integer.parseInt(in.readLine());
				break;
			}

			catch (Exception e) {
				System.out.println("Invalud input! " + e.getMessage());
				continue;
			}
		}

		// date
		while (true) {
			System.out.print("Please enter date of appointment, using the formation MM/DD/YYYY: ");
			try {
				date = in.readLine();
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// time
		while (true) {
			System.out.print("Please enter start and end time of appointment, using the format HH:MM-HH:MM (Note, 17:13 = 5:13 PM): ");
			try {
				time = in.readLine();
				break;
			}
			
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}
	
		// status
		while (true) {
			System.out.print("Please enter Appointment Status: ");
			try {
				status = in.readLine();
				if (status.length() != 2) {
					throw new RuntimeException("Please enter appointment status using codes PA, AC, AV, or WL.");
				}
				break;
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// query	
		try {
			query = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status) VALUES (" + apptID + ", \'" + date + "\', \'" + time + "\', \'" + status + "\');";
			esql.executeUpdate(query);

			System.out.print("Display updated Appointment table (y/n)? ");
			a = in.readLine();
			if (a.equals("y")) {
				row = esql.executeQueryAndPrintResult(q);
			}
		}

		catch (Exception e) {
			System.err.println("Query failed! " + e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		
		int pID;
		int docID;
		int apptID;
		String query;
		String hosp;
	
		List<List<String>> temp;
		String apptStatus;
		List<List<String>> tmp;
		int hospID;
	
		int test;
		String outAppt;
		String outPtnt;
	
		String name;
		String gender;
		int age;
		String address;
	
		String numAppt;
		int appt;
		
		// pID
		while (true) {
			System.out.print("Please enter Patient ID: ");
			try {
				pID = Integer.parseInt(in.readLine());
				break;
			}
		
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// docID
		while (true) {
			System.out.print("Please enter Doctor ID: ");
			try {
				docID = Integer.parseInt(in.readLine());
				break;			
			}

			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		// apptID
		while (true) {
			System.out.print("Please enter Appointment ID: ");
			try{ 
				apptID = Integer.parseInt(in.readLine());
				break;
			}
	
			catch (Exception e) {
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		/*
 		SELECT status
		FROM Appointment A, Doctor D, has_appointment H
		WHERE D.docID = H.docID AND H.apptID = A.apptID
		*/

		try {
			query = "SELECT A.status\nFROM Appointment A, has_appointment H\nWHERE " + docID + " = H.doctor_id AND " + apptID + " = H.appt_id AND H.appt_id = A.appnt_ID;";
			outAppt = "SELECT *\nFROM Appointment\nWHERE appnt_ID = " + apptID + ";";
			outPtnt = "SELECT *\nFROM Patient\nWHERE patient_ID = " + pID + ";";
			hosp = "SELECT H.hospital_ID\nFROM Hospital H, Appointment A, Doctor D, has_appointment HA, Department DT\nWHERE " + apptID + " = HA.appt_id AND HA.doctor_id = " + docID + " AND HA.doctor_id = D.doctor_ID AND D.did = DT.dept_ID AND DT.hid = H.hospital_ID;";
		
			// get status
			temp = esql.executeQueryAndReturnResult(query);
			apptStatus = temp.get(0).get(0);
		
			// get hospital ID
			tmp = esql.executeQueryAndReturnResult(hosp);
			hospID = Integer.parseInt(tmp.get(0).get(0));
		
			// output Appointment information
			test = esql.executeQueryAndPrintResult(outAppt);
		
			if (apptStatus.equals("WL")) {
				System.out.print("\nAppointment currently has a waitlist. Adding patient to waitlist.\n");	
			}
		
			else if (apptStatus.equals("AC")) {
				System.out.print("\nAppointment is already active. Changing appointment status to WL and adding patient to waitlist.\n");
				query = "UPDATE Appointment SET status = \'WL\' WHERE appnt_ID = " + apptID + ";";
				esql.executeUpdate(query);
				test = esql.executeQueryAndPrintResult(outAppt);
			}
		
			else if (apptStatus.equals("AV")) {
				System.out.print("\nAppointment is available. Changing appointment status to AC and adding patient to this appointment.\n");
				query = "UPDATE Appointment SET status = \'AC\' WHERE appnt_ID = " + apptID + ";";
				esql.executeUpdate(query);
				test = esql.executeQueryAndPrintResult(outAppt);
			}
		
			else { // appointment has already passed --> no longer available	
				throw new RuntimeException("Appointment is unavailable.");
			}

			//check if patient exists
			numAppt = "SELECT number_of_appts\nFROM Patient\nWHERE " + pID + " = patient_ID;";
			appt = esql.executeQuery(numAppt);
		
			if (appt != 1) { // Patient doesn't exist, create patient and add number_of_appts
				while (true) {
					System.out.print("\nPatient not yet in database, please add patient's information.\n");
					System.out.print("Please enter Patient's full name: ");
              				try {	
                   				name = in.readLine();
                    				if (name.length() <= 0 || name.length() > 128) {
                       		 			throw new RuntimeException("Patient's full name must be between 1 and 128 characters");
                    				}
						break;
                			}
                			catch (Exception e) {
                    				System.out.println("Invalid input! " + e.getMessage());
                    				continue;
                			}
				}

            			while (true) {
                			System.out.print("Please enter Patient's gender: ");
                			try {
                   	 			gender = in.readLine();
                    				if (gender.length() != 1) {
                        				throw new RuntimeException("Please enter M or F for Patient's gender");
                    				}
                    				break;
                			}

        			        catch (Exception e) {
              				      	System.out.println("Invalid input! " + e.getMessage());
                    				continue;
                			}
            			}
			
				while (true) {
                			System.out.print("Please enter Patient's age: ");
                			try{
                    				age = Integer.parseInt(in.readLine());
                    				if (age <= 0) {
                        				throw new RuntimeException("Patient's age must be greater than 0");
                    				}
                    				break;
                			}

                			catch (Exception e) {
						System.out.println("Invalid input! " + e.getMessage());
                    				continue;
                			}
            			}

            			while (true) {
                			System.out.print("Please enter Patient's address: ");
                    				try {
                        				address = in.readLine();
                        				break;
                    				}

                    				catch (Exception e) {
                        				System.out.println("Invalid input! " + e.getMessage());
                        				continue;
                    				}
            			}

            			try {
                			query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (" + pID + ", \'" + name + "\', \'" + gender + "\', " + age + ", \'" + address + "\', 1);";
                			esql.executeUpdate(query);
            			}

            			catch (Exception e) {
                			System.err.println("Query failed! " + e.getMessage());
            			}				
			}
		
			else if (appt == 1) { // Patient already exists, increase number_of_appts by 1
				query = "UPDATE Patient SET number_of_appts = number_of_appts + 1 WHERE patient_ID = " + pID + ";";
            			esql.executeUpdate(query);
			}
		
			System.out.print("\n");
			test = esql.executeQueryAndPrintResult(outPtnt);
			
			// insert into searches
			query = "INSERT INTO searches (hid, pid, aid) VALUES (" + hospID + ", " + pID + ", " + apptID + ");";
			esql.executeUpdate(query);

			System.out.print("\n");
			query = "SELECT hid, pid, aid\nFROM searches\n WHERE hid = " + hospID + " AND pid = " + pID + " AND aid = " + apptID + ";";
			test = esql.executeQueryAndPrintResult(query); 
		
		}
	
		catch (Exception e) {
			System.out.println("Invalid input! " + e.getMessage());
		}

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		
		// AC=active, AV=available
		// SELECT A.appnt_ID 
		// FROM Appointment A, Doctor D, has_appointment H
		// WHERE H.doctor_id = D.doctor_ID AND H.appt_id = A.appnt_ID AND (A.status = 'AV' OR A.status = 'AC') AND A.adate => startDate AND A.adate <= endDate
		// Note: D.doctor_ID, startDate, endDate is user inputted

		int docID;
		String startDate;
		String endDate;
		String query;

		//docID
		while(true)
		{
			System.out.print("Please enter Doctor ID: ");
			try
			{
				docID = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//startDate
		while(true)
		{
			System.out.print("Please enter a start date for your search(Use MM/DD/YY): ");
			try 
			{
				startDate = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//endDate
		while(true)
		{
			System.out.print("Please enter an end date for your search(Use MM/DD/YY): ");
			try 
			{
				endDate = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//combine queries
		try
		{
			
			query = "SELECT A.appnt_ID, A.adate, A.status\nFROM Appointment A, has_appointment H\nWHERE H.doctor_id = " + docID + " AND H.appt_id = A.appnt_ID AND (A.status = \'AV\' or A.status = \'AC\') AND (A.adate >= \'" + startDate + "\' AND A.adate <= \'" + endDate + "\')\nGROUP BY A.appnt_ID;";
			int temp = esql.executeQueryAndPrintResult(query);

		}
		catch (Exception e) 
		{
				System.err.println("Query failed! " + e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		
		// SELECT A.appnt_ID
		// FROM Appointment A, Department D, request_maintenance M, has_appointment H 
		// WHERE M.dept_name = dName AND M.did = H.doctor_id AND H.appt_id = A.appnt_ID AND A.status = 'AV' AND A.adate = date
		// dName and date is user inputted

		String dName;
		String date;
		String query;

		//dName
		while(true)
		{
			System.out.print("Please enter the Department name: ");
			try
			{
				dName = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//date
		while(true)
		{
			System.out.print("Please enter a specified date(Use MM/DD/YY): ");
			try
			{
				date = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//query
		try
		{
			query = "SELECT A.appnt_ID\nFROM Appointment A, Department D, request_maintenance M, has_appointment H\nWHERE M.dept_name = \'" + dName + "\' AND M.did = H.doctor_id AND H.appt_id = A.appnt_ID AND A.status = 'AV' AND A.adate = \'" + date + "\'\nGROUP BY A.appnt_ID;";
	//		esql.executeUpdate(query);
			int temp = esql.executeQueryAndPrintResult(query);
		}
		catch (Exception e) 
		{
				System.err.println("Query failed! " + e.getMessage());
		}
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		
		// SELECT D.doctor_ID, D.name, D.specialty, A.status, count(A.status) AS C
		// FROM Doctor D, Appointment A, has_appointment H
		// WHERE H.doctor_id = D.doctor_ID AND A.appnt_ID = H.appt_id
		// GROUP BY D.doctor_ID, D.name, D.specialty, A.status
		// ORDER BY C Desc

		String query;

		try
		{
			query = "SELECT D.doctor_ID, D.name, D.specialty, A.status, count(A.status) AS NumAppnts\n"
				+ "FROM Doctor D, Appointment A, has_appointment H\n"
				+ "WHERE H.doctor_id = D.doctor_ID AND A.appnt_ID = H.appt_id\n"
				+ "GROUP BY D.doctor_ID, D.name, D.specialty, A.status\n"
				+ "ORDER BY NumAppnts Desc;";

			int temp = esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e)
		{
			System.err.println("Query failed! " + e.getMessage());
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.

		// SELECT D.doctor_ID, D.name, D.specialty, count(S.pid) AS C
		// FROM Doctor D, Searches S, has_appointment H, Appointment A
		// WHERE H.doctor_id = D.doctor_ID AND A.status = status AND A.appnt_ID = S.aid AND H.appt_id = S.aid
		// GROUP BY D.doctor_ID, D.name, D.specialty

		// Note: in table searches: hid = hospital id, pid = patient id, aid = appointment id 
		// Also note status is retrieved from the user input 

		try
		{
			System.out.print("Please enter the appointment status: ");
			String status = in.readLine();

			String query = "SELECT D.doctor_ID, D.name, D.specialty, count(S.pid) AS NumPatients\n"
							+ "FROM Doctor D, Searches S, has_appointment H, Appointment A\n"
							+ "WHERE H.doctor_id = D.doctor_ID AND A.status = \'" + status + "\' AND A.appnt_ID = S.aid AND H.appt_id = S.aid\n"
							+ "GROUP BY D.doctor_ID, D.name, D.specialty;";
			int temp = esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e)
		{
			System.err.println("Query failed! " + e.getMessage());
		}
	}
}

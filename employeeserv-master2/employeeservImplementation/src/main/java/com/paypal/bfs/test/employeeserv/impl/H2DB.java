package com.paypal.bfs.test.employeeserv.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paypal.bfs.test.employeeserv.api.model.Address;
import com.paypal.bfs.test.employeeserv.api.model.Employee;

@Component
public class H2DB {

	private Connection conn;

	private Logger log = LoggerFactory.getLogger("H2DB");

	public H2DB() {
		try {
			this.conn = DriverManager.getConnection("jdbc:h2:mem:");
			this.createInMemoryTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createInMemoryTable() {

		try (Statement stmt = conn.createStatement()) {

			String q = "CREATE TABLE employee (" + //
					"  id INT auto_increment," + //
					"  first_name varchar(50) NOT NULL," + //
					"  last_name varchar(50) NOT NULL," + //
					"  date_of_birth datetime NOT NULL," + //
					"  address_line1 varchar(50) NOT NULL," + //
					"  address_line2 varchar(50)," + //
					"  city varchar(50)," + //
					"  state varchar(50)," + //
					"  country varchar(50)," + //
					"  zip_code varchar(10)" + //
					")";

			stmt.executeUpdate(q);
			log.info("Table created successfully ..... ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Employee addEmployee(Employee employee) throws Exception {

		validate("first_name", employee.getFirstName());
		validate("last_name", employee.getLastName());
		validate("data_of_birth", employee.getDateOfBirth());
		validate("address", employee.getAddress().getLine1());

		boolean exists = true;

		try {
			this._fetchEmployee(
					"first_name='" + employee.getFirstName() + "' AND last_name='" + employee.getLastName() + "'");

		} catch (Exception ex) {
			log.info("No duplicate");
			exists = false;
		}

		if (exists) {
			throw new Exception("Employee already exist");
		}

		log.info("Adding employee on database");

		String sql = "INSERT INTO employee(" //
				+ "first_name," //
				+ "last_name," //
				+ "date_of_birth," //
				+ "address_line1," //
				+ "address_line2," //
				+ "city," //
				+ "state," //
				+ "country," //
				+ "zip_code) VALUES(?,?,?,?,?,?,?,?,?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, employee.getFirstName());
			stmt.setString(2, employee.getLastName());

			java.sql.Date sqlDate = new java.sql.Date(employee.getDateOfBirth().getTime());
			stmt.setDate(3, sqlDate);

			Address add = employee.getAddress();

			stmt.setString(4, add.getLine1());
			stmt.setString(5, add.getLine2());
			stmt.setString(6, add.getCity());
			stmt.setString(7, add.getState());
			stmt.setString(8, add.getCountry());
			stmt.setString(9, add.getZipCode());

			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				log.info("Getting generated id");
				String id = rs.getString(1);
				log.info("Inserted ID : " + id); // display inserted record
				employee = this.getEmployee(id);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return employee;
	}

	public Employee getEmployee(String id) throws Exception {
		return _fetchEmployee("id=" + id);
	}

	private Employee _fetchEmployee(String where) throws Exception {

		log.info("_fetchEmployee : " + where);

		try (Statement stmt = conn.createStatement()) {

			String sql = "select * from employee WHERE " + where;

			log.info("_fetchEmployee query : {}", sql);

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next() == false) {
				throw new Exception("_fetchEmployee No data found " + where);
			}

			Employee employee = new Employee();

			employee.setId(rs.getInt("id"));
			employee.setFirstName(rs.getString("first_name"));
			employee.setLastName(rs.getString("last_name"));
			employee.setDateOfBirth(rs.getDate("date_of_birth"));
			employee.setAddress(_getAddress(rs));

			rs.close();

			log.info("_fetchEmployee done");

			return employee;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private Address _getAddress(ResultSet rs) throws SQLException {
		Address add = new Address();
		add.setLine1(rs.getString("address_line1"));
		add.setLine2(rs.getString("address_line2"));
		add.setCity(rs.getString("city"));
		add.setState(rs.getString("state"));
		add.setCountry(rs.getString("country"));
		add.setZipCode(rs.getString("zip_code"));
		return add;
	}

	private void validate(String field, Object data) throws Exception {
		if (data == null) {
			throw new Exception("Invalid data - NULL value : " + field);
		} else if (data.toString().equalsIgnoreCase("")) {
			throw new Exception("Eempty  " + field);
		}

	}

}

package com.paypal.bfs.test.employeeserv;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Calendar;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.paypal.bfs.test.employeeserv.api.model.Address;
import com.paypal.bfs.test.employeeserv.api.model.Employee;
import com.paypal.bfs.test.employeeserv.impl.H2DB;

@AutoConfigureMockMvc
@SpringBootTest
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private H2DB h2db;

	@Autowired
	protected WebApplicationContext wac;

	@Before
	public void setup() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void test1() throws Exception {
		Employee emp = new Employee();
		emp.setFirstName("David");
		emp.setLastName("Raja");
		emp.setDateOfBirth(Calendar.getInstance().getTime());
		Address add = new Address();
		add.setLine1("2/3, Church Street");
		add.setLine2("line2");
		emp.setAddress(add);
		Employee e1 = h2db.addEmployee(emp);
		assertTrue(e1.getId() == 1);
	}

	@Test
	public void test2() throws Exception {
		mockMvc.perform(get("/v1/bfs/employees/1")).andDo(print()).andExpect(status().isOk());
	}
	
	@Test
	public void test3() throws Exception {
		mockMvc.perform(get("/v1/bfs/employees/2")).andDo(print()).andExpect(status().is(400));
	}

}

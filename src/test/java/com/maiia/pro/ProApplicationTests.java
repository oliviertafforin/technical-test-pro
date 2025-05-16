package com.maiia.pro;

import com.maiia.pro.controller.ProAppointmentController;
import com.maiia.pro.controller.ProAvailabilityController;
import com.maiia.pro.controller.ProPatientController;
import com.maiia.pro.controller.ProPractitionerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProApplicationTests {

	@Autowired
	private ProAppointmentController appointmentController;

	@Autowired
	private ProAvailabilityController availabilityController;

	@Autowired
	private ProPatientController patientController;

	@Autowired
	private ProPractitionerController practitionerController;


	@Test
	void contextLoads() {
		assertNotNull(practitionerController);
		assertNotNull(appointmentController);
		assertNotNull(patientController);
		assertNotNull(availabilityController);
	}

}

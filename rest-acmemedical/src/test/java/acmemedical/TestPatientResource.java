/********************************************************************************************************
 * File:  TestACMEMedicalSystem.java Course Materials CST 8277
 * Last Updated: 2024-12-04
 * 
 * @author Teddy Yap
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */
package acmemedical;

import static acmemedical.utility.MyConstants.APPLICATION_API_VERSION;
import static acmemedical.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.PHYSICIAN_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.Matchers.nullValue;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import acmemedical.entity.Patient;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestPatientResource {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    
    static final String PATIENT_RESOURCE_NAME = "patient";

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
    }

    protected WebTarget webTarget;

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_all_patients_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Patient> patients = response.readEntity(new GenericType<List<Patient>>() {});
        assertThat(patients, is(not(empty())));
    }

    @Test
    public void test02_patient_by_id_with_adminrole() {
        int patientId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path(String.valueOf(patientId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Patient patient = response.readEntity(Patient.class);
        assertThat(patient, is(not(nullValue())));
        assertThat(patient.getId(), is(patientId));
    }

    @Test
    public void test03_add_patient_with_adminrole() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("John");
        newPatient.setLastName("Doe");
        newPatient.setYear(1994);
        newPatient.setAddress("123 Elm Street");
        newPatient.setHeight(180);
        newPatient.setWeight(75);
        newPatient.setSmoker((byte) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .post(Entity.json(newPatient));
        
        assertThat(response.getStatus(), is(200));
        Patient createdPatient = response.readEntity(Patient.class);
        assertThat(createdPatient, is(not(nullValue())));
        assertThat(createdPatient.getFirstName(), is("John"));
        assertThat(createdPatient.getLastName(), is("Doe"));
    }

    @Test
    public void test04_update_patient_with_adminrole() {
        int patientId = 1;
        Patient updatedPatient = new Patient();
        updatedPatient.setFirstName("Jane");
        updatedPatient.setLastName("Doe");
        updatedPatient.setYear(1996);
        updatedPatient.setAddress("1234 Elm Street");
        updatedPatient.setHeight(165);
        updatedPatient.setWeight(60);
        updatedPatient.setSmoker((byte) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path(String.valueOf(patientId))
            .request()
            .put(Entity.json(updatedPatient));

        assertThat(response.getStatus(), is(200));

        Patient patient = response.readEntity(Patient.class);

        assertThat(patient.getFirstName(), is("Jane"));
        assertThat(patient.getLastName(), is("Doe"));
        assertThat(patient.getYear(), is(1996));
        assertThat(patient.getAddress(), is("1234 Elm Street"));
        assertThat(patient.getHeight(), is(165));
        assertThat(patient.getWeight(), is(60));
        assertThat(patient.getSmoker(), is((byte) 0));

        assertThat(patient.getId(), is(patientId));
    }

    @Test
    public void test05_delete_patient_with_adminrole() {
        int patientId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path(String.valueOf(patientId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }

}

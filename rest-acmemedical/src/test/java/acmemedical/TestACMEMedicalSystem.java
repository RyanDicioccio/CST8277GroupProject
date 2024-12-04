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
import java.time.LocalDateTime;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import acmemedical.entity.*;
import acmemedical.rest.resource.*;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import acmemedical.ejb.ACMEMedicalService;
import jakarta.ws.rs.core.Response;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMEMedicalSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    
    static final String MEDICAL_CERTIFICATE_RESOURCE_NAME = "medical_certificate";
    static final String MEDICAL_SCHOOL_RESOURCE_NAME = "medical_school";
    static final String MEDICAL_TRAINING_RESOURCE_NAME = "medical_training";
    static final String PATIENT_RESOURCE_NAME = "patient";


    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

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
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_all_physicians_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>(){});
        assertThat(physicians, is(not(empty())));
        assertThat(physicians, hasSize(1));
    }
    
 
    @Test
    public void test02_medical_certificate_by_id_with_adminrole() {
        int certificateId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assert certificate != null : "Medical certificate should not be null";
        assertThat(certificate.getId(), is(certificateId));
    }
    
    @Test
    public void test03_add_medical_certificate_with_adminrole() {
        MedicalCertificate newCertificate = new MedicalCertificate();
        newCertificate.setSigned((byte) 1);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCertificate));
        assertThat(response.getStatus(), is(200));
        MedicalCertificate createdCertificate = response.readEntity(MedicalCertificate.class);
        assert createdCertificate != null : "Created medical certificate should not be null";
        assertThat(createdCertificate.getSigned(), is((byte) 1));
    }
    
    @Test
    public void test04_update_medical_certificate_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        MedicalCertificate updatedCertificate = new MedicalCertificate();
        updatedCertificate.setSigned((byte) 0);

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .put(Entity.json(updatedCertificate));
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assertThat(certificate.getSigned(), is((byte) 0));
    }
    
    @Test
    public void test05_delete_medical_certificate_with_adminrole() {
        int certificateId = 1; // Change to a valid ID for your test environment
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path(String.valueOf(certificateId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test06_get_all_medical_schools_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<MedicalSchool> medicalSchools = response.readEntity(new GenericType<List<MedicalSchool>>() {});
        assertThat(medicalSchools, is(not(empty())));
    }
    
    @Test
    public void test07_get_medical_school_by_id_with_adminrole() {
        int medicalSchoolId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalSchool medicalSchool = response.readEntity(MedicalSchool.class);
        assert medicalSchool != null : "Medical school should not be null";
        assertThat(medicalSchool.getId(), is(medicalSchoolId));
    }
    
    @Test
    public void test08_add_medical_school_with_adminrole() {
        MedicalSchool newMedicalSchool = new PrivateSchool();
        newMedicalSchool.setName("New Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.json(newMedicalSchool));
        assertThat(response.getStatus(), is(200));
        MedicalSchool createdSchool = response.readEntity(MedicalSchool.class);
        assertThat(createdSchool.getName(), is("New Medical School"));
    }
    
    @Test
    public void test09_update_medical_school_with_adminrole() {
        int medicalSchoolId = 1;
        MedicalSchool updatedSchool = new PrivateSchool();
        updatedSchool.setName("Updated Medical School");

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .put(Entity.json(updatedSchool));
        assertThat(response.getStatus(), is(200));
        MedicalSchool resultSchool = response.readEntity(MedicalSchool.class);
        assertThat(resultSchool.getName(), is("Updated Medical School"));
    }
    
    @Test
    public void test10_delete_medical_school_with_adminrole() {
        int medicalSchoolId = 1; 
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test11_add_medical_training_to_medical_school_with_adminrole() {
        int medicalSchoolId = 1;
        MedicalTraining newTraining = new MedicalTraining();
        newTraining.setDurationAndStatus(new DurationAndStatus());;

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(medicalSchoolId))
            .path("medicaltraining")
            .request()
            .post(Entity.json(newTraining));
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    public void test12_get_all_schools_and_verify_one_with_name() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<MedicalSchool> schools = response.readEntity(new GenericType<List<MedicalSchool>>() {});
        assertThat(schools, is(not(empty())));

        boolean hasSpecificSchool = schools.stream().anyMatch(s -> s.getName().equals("Harvard Medical School"));
        assertThat(hasSpecificSchool, is(true)); // Replace "Harvard Medical School" with a valid name
    }

    @Test
    public void test13_delete_medical_school_with_invalid_id() {
        int invalidId = 9999999; 
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path(String.valueOf(invalidId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(404)); // Expecting not found
    }

    
    @Test
    public void test14_get_all_medical_trainings_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<MedicalTraining> trainings = response.readEntity(new GenericType<List<MedicalTraining>>() {});
        assertThat(trainings, is(not(empty())));
    }
    
    @Test
    public void test15_get_medical_training_by_id_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalTraining training = response.readEntity(MedicalTraining.class);
        assert training != null : "Medical training should not be null";
        assertThat(training.getId(), is(trainingId));
    }
    
    @Test
    public void test16_add_medical_training_with_adminrole() {
        MedicalTraining newTraining = new MedicalTraining();
        newTraining.setDurationAndStatus(new DurationAndStatus());

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .post(Entity.json(newTraining));

        assertThat(response.getStatus(), is(200));

        MedicalTraining createdTraining = response.readEntity(MedicalTraining.class);
        assert createdTraining != null : "Created medical training should not be null";
    }

    @Test
    public void test17_update_medical_training_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        MedicalTraining updatedTraining = new MedicalTraining();

        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .put(Entity.json(updatedTraining));

        assertThat(response.getStatus(), is(200));
    }

    
    @Test
    public void test18_delete_medical_training_with_adminrole() {
        int trainingId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(trainingId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204)); 
    }
    
    @Test
    public void test19_get_training_by_invalid_id() {
        int invalidId = 9999; // An ID that does not exist
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path(String.valueOf(invalidId))
            .request()
            .get();
        assertThat(response.getStatus(), is(404)); // Expecting not found
    }
    
    @Test
    public void test20_get_all_medicines_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>() {});
        assertThat(medicines, is(not(empty())));
    }

    @Test
    public void test21_get_medicine_by_id_with_adminrole() {
        int medicineId = 1; // Replace with a valid ID
        Response response = webTarget
            .register(adminAuth)
            .path("medicine") // Use the correct resource name
            .path(String.valueOf(medicineId))
            .request()
            .get();
        assertThat(response.getStatus(), is(200));

        Medicine medicine = response.readEntity(Medicine.class);
        assert medicine != null : "Medicine should not be null";
        assertThat(medicine.getId(), is(medicineId));
    }

    @Test
    public void test22_add_medicine_with_adminrole() {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("New Medicine");

        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .post(Entity.json(newMedicine));

        assertThat(response.getStatus(), is(200));
        Medicine createdMedicine = response.readEntity(Medicine.class);
        assert createdMedicine != null : "Created medicine should not be null";
    }

    @Test
    public void test23_update_medicine_with_adminrole() {
        int medicineId = 1; // Replace with a valid ID
        Medicine updatedMedicine = new Medicine();
        updatedMedicine.setDrugName("Updated Medicine"); 

        Response response = webTarget
            .register(adminAuth)
            .path("medicine") 
            .path(String.valueOf(medicineId))
            .request()
            .put(Entity.json(updatedMedicine));
        assertThat(response.getStatus(), is(200));
    }

    @Test
    public void test24_delete_medicine_with_adminrole() {
        int medicineId = 1; 
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .path(String.valueOf(medicineId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test25_get_all_medicines_and_verify_availability() {
        Response response = webTarget
            .register(adminAuth)
            .path("medicine")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>() {});
        assertThat(medicines, is(not(empty())));

        boolean hasSpecificMedicine = medicines.stream().anyMatch(m -> m.getDrugName().equals("Aspirin"));
        assertThat(hasSpecificMedicine, is(true));
    }
    
    @Test
    public void test26_all_patients_with_adminrole() {
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
    public void test27_patient_by_id_with_adminrole() {
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
    public void test28_add_patient_with_adminrole() {
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
    public void test29_update_patient_with_adminrole() {
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
    public void test30_delete_patient_with_adminrole() {
        int patientId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path(String.valueOf(patientId))
            .request()
            .delete();
        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test31_get_physicians_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>() {});
        assertThat(physicians, is(not(empty())));
    }

    @Test
    public void test32_get_physician_by_id_with_adminrole() {
        int physicianId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        Physician physician = response.readEntity(Physician.class);
        assertThat(physician.getId(), is(physicianId));
    }

    @Test
    public void test33_get_physician_by_id_with_userrole_own_physician() {
        int physicianId = 1;
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        Physician physician = response.readEntity(Physician.class);
        assertThat(physician.getId(), is(physicianId));
    }

    @Test
    public void test34_get_physician_by_id_with_userrole_other_physician() {
        int physicianId = 2;
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .get();

        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void test35_delete_physician_with_adminrole() {
        int physicianId = 1;
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .delete();

        assertThat(response.getStatus(), is(204));
    }
    
    @Test
    public void test36_create_physician_with_adminrole() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Dr. Sarah");
        newPhysician.setLastName("Lee");

        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.json(newPhysician));

        assertThat(response.getStatus(), is(200));
        Physician createdPhysician = response.readEntity(Physician.class);
        assertThat(createdPhysician, is(not(nullValue())));
        assertThat(createdPhysician.getFirstName(), is("Dr. Sarah"));
        assertThat(createdPhysician.getLastName(), is("Lee"));
    }

    @Test
    public void test37_update_physician_with_adminrole() {
        int physicianId = 1; // Replace with a valid physician ID for your test
        Physician updatedPhysician = new Physician();
        updatedPhysician.setFirstName("Dr. Michael");
        updatedPhysician.setLastName("Taylor");

        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .put(Entity.json(updatedPhysician));

        assertThat(response.getStatus(), is(200));

        Physician physician = response.readEntity(Physician.class);

        assertThat(physician.getFirstName(), is("Dr. Michael"));
        assertThat(physician.getLastName(), is("Taylor"));

        assertThat(physician.getId(), is(physicianId));
    }

}
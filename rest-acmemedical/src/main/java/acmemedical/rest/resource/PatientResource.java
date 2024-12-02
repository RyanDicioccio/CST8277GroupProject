package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.PATIENT_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Patient;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


/**
 * This is a resource class for the patient Entity of the application.
 * 
 * @author Dan Blais
 */

@Path(PATIENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {
	private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getPatients() {
        LOG.debug("retrieving all physicians ...");
        List<Patient> patients = service.getAll(Patient.class, "Patient.findAll");
        Response response = Response.ok(patients).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getPatientById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific patient " + id);
        Response response = null;
        Patient patient = null;

        if (sc.isCallerInRole(ADMIN_ROLE)) {
        	patient = service.getById(Patient.class, "Patient.findById", id);
            response = Response.status(patient == null ? Status.NOT_FOUND : Status.OK).entity(patient).build();
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPatient(Patient newPatient) {
        Response response = null;
        Patient newPatientWithIdTimestamps = service.persistPatient(newPatient);
        response = Response.ok(newPatientWithIdTimestamps).build();
        return response;
    }
    
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updatePatient(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Patient updatedPatient) {
        Response response = null;
        Patient patient = service.updatePatientById(id, updatedPatient);
        response = Response.ok(patient).build();
        return response;
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePatientById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Response response = null;
        service.deletePatientById(id); 
        response = Response.status(Status.NO_CONTENT).build();        
        return response;
    }
}

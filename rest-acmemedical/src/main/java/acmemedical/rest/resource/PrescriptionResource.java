/********************************************************************************************************
 * File:  MedicalTraining.java Course Materials CST 8277
 * Last Updated: 2024-12-02
 * 
 * @author Teddy Yap
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */
package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.PRESCRIPTION_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
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
 * This is a resource class for the Prescription Entity of the application.
 * 
 */

@Path(PRESCRIPTION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrescriptionResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getPrescriptions() {
        LOG.debug("Retrieving all prescriptions...");
        List<Prescription> prescriptions = service.getAll(Prescription.class, "Prescription.findAll");
        Response response = Response.ok(prescriptions).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getPrescriptionById(@PathParam(RESOURCE_PATH_ID_ELEMENT) String id) {
        LOG.debug("Trying to retrieve specific prescription with ID " + id);
        String[] idParts = id.split(":");
        int physicianId = Integer.parseInt(idParts[0]);
        int patientId = Integer.parseInt(idParts[1]);
        PrescriptionPK prescriptionPK = new PrescriptionPK(physicianId, patientId);
        Response response = null;
        Prescription prescription = service.getPrescriptionById(prescriptionPK);
        response = Response.status(prescription == null ? Status.NOT_FOUND : Status.OK).entity(prescription).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPrescription(Prescription newPrescription) {
        LOG.debug("Adding new prescription...");
        Response response = null;
        Prescription newPrescriptionWithId = service.persistPrescription(newPrescription);
        response = Response.ok(newPrescriptionWithId).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updatePrescription(@PathParam(RESOURCE_PATH_ID_ELEMENT) String id, Prescription updatedPrescription) {
        LOG.debug("Updating prescription with ID " + id);
        String[] idParts = id.split(":");
        int physicianId = Integer.parseInt(idParts[0]);
        int patientId = Integer.parseInt(idParts[1]);
        PrescriptionPK prescriptionPK = new PrescriptionPK(physicianId, patientId);
        Response response = null;
        Prescription prescription = service.updatePrescriptionById(prescriptionPK, updatedPrescription);
        response = Response.ok(prescription).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePrescriptionById(@PathParam(RESOURCE_PATH_ID_ELEMENT) String id) {
        LOG.debug("Deleting prescription with ID " + id);
        String[] idParts = id.split(":");
        int physicianId = Integer.parseInt(idParts[0]);
        int patientId = Integer.parseInt(idParts[1]);
        PrescriptionPK prescriptionPK = new PrescriptionPK(physicianId, patientId);
        Response response = null;
        service.deletePrescriptionById(prescriptionPK);
        response = Response.status(Status.NO_CONTENT).build();
        return response;
    }
}

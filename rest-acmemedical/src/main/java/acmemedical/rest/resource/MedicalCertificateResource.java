/********************************************************************************************************
 * File:  MedicalCertificateResource.java Course Materials CST 8277
 * Last Updated: 2024-12-02
 * 
 * @author Dan Blais
 * @author Imed Cherabi
 * @author Ryan Di Cioccio
 * @author Aaron Renshaw
 * 
 */
package acmemedical.rest.resource;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.MEDICAL_CERTIFICATE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalCertificate;
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

@Path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalCertificateResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMedicalCertificates() {
        LOG.debug("Retrieving all medical certificates...");
        List<MedicalCertificate> certificates = service.getAll(MedicalCertificate.class, "MedicalCertificate.findAll");
        Response response = Response.ok(certificates).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicalCertificateById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Trying to retrieve specific medical certificate with ID " + id);
        Response response = null;
        MedicalCertificate certificate = null;
        String username = sc.getCallerPrincipal().getName();

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            certificate = service.getById(MedicalCertificate.class, "MedicalCertificate.findById", id);
            response = Response.status(certificate == null ? Status.NOT_FOUND : Status.OK).entity(certificate).build();
        } else if (sc.isCallerInRole("USER_ROLE")) {
            certificate = service.getById(MedicalCertificate.class, "MedicalCertificate.findById", id);

            if (certificate == null) {
                response = Response.status(Status.NOT_FOUND).build();
             // Ensure the certificate belongs to the current user
            } else if (!certificate.getOwner().getFirstName().equals(username)) {
                response = Response.status(Status.FORBIDDEN).build();
            } else {
                response = Response.status(Status.OK).entity(certificate).build(); 
            }
        }else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalCertificate(MedicalCertificate newCertificate) {
        LOG.debug("Adding new medical certificate...");
        Response response = null;
        MedicalCertificate newCertificateWithId = service.persistMedicalCertificate(newCertificate);
        response = Response.ok(newCertificateWithId).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMedicalCertificate(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, MedicalCertificate updatedCertificate) {
        LOG.debug("Updating medical certificate with ID " + id);
        Response response = null;
        MedicalCertificate certificate = service.updateMedicalCertificateById(id, updatedCertificate);
        response = Response.ok(certificate).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicalCertificateById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Deleting medical certificate with ID " + id);
        Response response = null;
        service.deleteMedicalCertificateById(id);
        response = Response.status(Status.NO_CONTENT).build();
        return response;
    }
}

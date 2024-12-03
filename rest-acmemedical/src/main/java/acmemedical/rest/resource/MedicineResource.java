/********************************************************************************************************
 * File:  MedicineResource.java Course Materials CST 8277
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
import static acmemedical.utility.MyConstants.MEDICINE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmemedical.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Medicine;
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

@Path(MEDICINE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicineResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMedicines() {
        LOG.debug("Retrieving all medicines...");
        List<Medicine> medicines = service.getAll(Medicine.class, "Medicine.findAll");
        return Response.ok(medicines).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMedicineById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Trying to retrieve specific medicine with ID: " + id);
        Medicine medicine = service.getById(Medicine.class, "Medicine.findById", id);
        return Response.status(medicine == null ? Status.NOT_FOUND : Status.OK).entity(medicine).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicine(Medicine newMedicine) {
        LOG.debug("Adding new medicine...");
        Medicine persistedMedicine = service.persistMedicine(newMedicine);
        return Response.ok(persistedMedicine).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMedicine(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Medicine updatedMedicine) {
        LOG.debug("Updating medicine with ID: " + id);
        Medicine medicine = service.updateMedicineById(id, updatedMedicine);
        return Response.ok(medicine).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMedicineById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("Deleting medicine with ID: " + id);
        service.deleteMedicineById(id);
        return Response.status(Status.NO_CONTENT).build();
    }
}

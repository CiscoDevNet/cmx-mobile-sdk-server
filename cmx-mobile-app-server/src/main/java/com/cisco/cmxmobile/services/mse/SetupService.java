package com.cisco.cmxmobile.services.mse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.model.Campaign;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.server.mse.Setup;
import com.cisco.cmxmobile.server.stats.ConnectAndEngageStats;
import com.cisco.cmxmobile.utils.MDCKeys;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.cacheService.service.SetupSource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Component
@Path("/api/v1/setup")
public class SetupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupService.class);

    @Autowired
    public MobileServerCacheService mobileServerCacheService;
    
    @Autowired
    public Setup setup;
    
    @Value("${map.location}")
    private String rootMapLocation;    
    
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setup(ArrayList<Venue> venueList) {
        if (venueList == null) {
            LOGGER.info("Setup invoked but either Venue is null so no operation performed");
            return Response.status(Status.BAD_REQUEST).build();
        }
        
        ConnectAndEngageStats.getInstance().incrementConfigureUpdateCount();
        LOGGER.info("Setup Venue has been invoked for {} venues", venueList.size());

        try {
            setup.setupVenue(venueList);
            
            LOGGER.info("Completed Venue Setup");
            
            //Get the MSEUDID from the Venue in the VenueList and then
            //Save the Setup Source - 
            SetupSource.APP_ENGAGE.apply(((Venue) venueList.get(0)).getMseUdId());
            
            return Response.ok().entity("Venue List Stored in Cloud").build();
        } catch (Exception e) {
            LOGGER.error("Failed to Save Venues", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to Save Venues" + e.getLocalizedMessage()).build();
        } 

        //TODO: Remove this Save the Venue List
        //mobileServerCacheService.saveVenue(venueList);
    }
    
    @POST
    @Path("/campaign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupCampaign(ArrayList<Campaign> campaignList) {
        if (campaignList == null) {
            LOGGER.info("Setup invoked but either Campaign is null so no operation performed");
            return Response.status(Status.BAD_REQUEST).build();
        }

        ConnectAndEngageStats.getInstance().incrementCampaignUpdateCount();
        LOGGER.info("Setup Campaign has been invoked for {} campaigns", campaignList.size()); 
        try {
            mobileServerCacheService.getCampaignCacheService().saveCampaignList(campaignList);
            LOGGER.info("Completed Campaign Setup");
            return Response.ok().entity("Campaign List Stored in Cloud").build();
        } catch (Exception e) {
            LOGGER.error("Failed to Save Campaigns", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to Save Campaign" + e.getLocalizedMessage()).build();
        } 
    }

    @POST
    @Path("/uploadFloorImage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFloorImage(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileInfo, @FormDataParam("mseVenueUdId") String mseVenueUdId, @FormDataParam("mseFloorId") String mseFloorId, @FormDataParam("filename") String filename) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        double fileSize = 0;
        try {
            ConnectAndEngageStats.getInstance().incrementFloorImageUploadCount();
            LOGGER.info("Uploading floor image for venue ID '{}' floor ID '{}' image name {}", mseVenueUdId, mseFloorId, filename);
            // Check if Directory is present
            // If failed to create MD5 for mseVenueUdId, none of the floor
            // images will save
            File venueDirectory = new File(rootMapLocation, EncryptionUtil.generateMD5(mseVenueUdId));

            // Check if the Venue Directory Exists
            if ((!venueDirectory.getCanonicalFile().exists()) || (venueDirectory.getCanonicalFile().exists() && !venueDirectory.getCanonicalFile().isDirectory())) {
                LOGGER.trace("Create venue upload image directory for {}", venueDirectory);
                // Make the Directory
                if (!venueDirectory.mkdir()) {
                    LOGGER.error("Unable to upload image since the directory was failed to be created for {}", venueDirectory);
                    MDC.remove(MDCKeys.VENUE_ID);
                    return Response.status(500).entity("Failed to create directory").build();
                }
            }
            fileSize = writeToFile(file, new File(venueDirectory, mseFloorId + "_" + filename).getAbsolutePath());

        }
        catch (IOException e) {
            LOGGER.error("Failed to upload floor image", e);
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload floor images").build();
        }
        catch (NoSuchAlgorithmException algoEx) {
            LOGGER.error("Failed to generate MD5 for MSE UDID {}", algoEx.getLocalizedMessage());
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload floor images").build();
        }

        LOGGER.info("Completed uploading floor image for venue ID '{}' floor ID '{}' image name {} with size: {} Megabytes", mseVenueUdId, mseFloorId, filename, fileSize);
        MDC.remove(MDCKeys.VENUE_ID);
        return Response.status(Status.OK).entity("Floor Image loaded for MSE: " + mseVenueUdId + ", File name : " + filename).build();
    }

    @POST
    @Path("/uploadPoiImage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPoiImage(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileInfo, @FormDataParam("mseVenueUdId") String mseVenueUdId, @FormDataParam("mseFloorId") String mseFloorId, @FormDataParam("poiId") String poiId) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        double fileSize = 0;
        try {
            ConnectAndEngageStats.getInstance().incrementPoiImageUploadCount();
            LOGGER.info("Uploading POI image for venue ID '{}' floor ID '{}' POI ID '{}'", mseVenueUdId, mseFloorId, poiId);

            // Check if Directory is present
            // POI Images will be stored under ->
            // md5(mseVenueUdId)/mseFloorId/{poiId}
            File poiDirectory = new File(new File(rootMapLocation, EncryptionUtil.generateMD5(mseVenueUdId)), EncryptionUtil.generateMD5(mseFloorId));

            // Check if the POI Directory Exists
            if ((!poiDirectory.getCanonicalFile().exists()) || (poiDirectory.getCanonicalFile().exists() && !poiDirectory.getCanonicalFile().isDirectory())) {
                LOGGER.trace("Create POI upload image directory for {}", poiDirectory);
                // Make the Directory and parent directory if necessary
                if (!poiDirectory.mkdirs()) {
                    LOGGER.error("Unable to upload image since the directory was failed to be created for {}", poiDirectory);
                    MDC.remove(MDCKeys.VENUE_ID);
                    return Response.status(500).entity("Failed to create directory").build();
                }
            }

            LOGGER.info("POI directory {}", poiDirectory);
            String imageType = ".jpg";
            fileSize = writeToFile(file, new File(poiDirectory, poiId + imageType).getAbsolutePath());

        }
        catch (IOException e) {
            LOGGER.error("Failed to upload POI image", e);
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload POI images").build();
        }
        catch (NoSuchAlgorithmException algoEx) {
            LOGGER.error("Failed to generate MD5 for MSE UDID {}", algoEx.getLocalizedMessage());
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload POI images").build();
        }

        LOGGER.info("Completed uploading POI image for venue ID '{}' floor ID '{}' POI ID '{}' with size: {} Megabytes", mseVenueUdId, mseFloorId, poiId, fileSize);
        MDC.remove(MDCKeys.VENUE_ID);
        return Response.status(Status.OK).entity("POI Image loaded for MSE: " + mseVenueUdId + ", POI ID : " + poiId).build();
    }

    @POST
    @Path("/uploadCertificate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCertificateFile(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileInfo, @FormDataParam("mseVenueUdId") String mseVenueUdId, @FormDataParam("certificateFile") String certificateFile) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        ConnectAndEngageStats.getInstance().incrementCertificateUploadCount();
        LOGGER.info("Uploading Certificate file for venue ID '{}' ", mseVenueUdId);
        double fileSize = 0;
        try {
            // Check if Directory is present
            // If failed to create MD5 for mseVenueUdId, none of the floor
            // images will save
            File venueDirectory = new File(rootMapLocation);

            // Check if the Venue Directory Exists
            if ((!venueDirectory.getCanonicalFile().exists()) || (venueDirectory.getCanonicalFile().exists() && !venueDirectory.getCanonicalFile().isDirectory())) {
                LOGGER.trace("Create certificate upload image directory for {}", venueDirectory);
                // Make the Directory
                if (!venueDirectory.mkdir()) {
                    LOGGER.error("Unable to upload image since the directory was failed to be created for {}", venueDirectory);
                    MDC.remove(MDCKeys.VENUE_ID);
                    return Response.status(500).entity("Failed to create directory").build();
                }
            }
            fileSize = writeToFile(file, new File(venueDirectory, certificateFile).getAbsolutePath());

        }
        catch (IOException e) {
            LOGGER.error("Failed to save Certificate File : {}", e.getLocalizedMessage());
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload Certificate file").build();
        }
        catch (Exception e) {
            LOGGER.error("Failed to save Certificate File : {}", e.getLocalizedMessage());
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(500).entity("Failed to upload Certificate file").build();
        }

        LOGGER.info("Completed uploading Certificate file for {} with size: {} Megabytes", mseVenueUdId, fileSize);
        MDC.remove(MDCKeys.VENUE_ID);
        return Response.status(Status.OK).entity("Certificate file loaded").build();
    }
    
    @POST
    @Path("/uploadBannerImage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadBannerImage(
        @FormDataParam("file") InputStream file, 
    	@FormDataParam("file") FormDataContentDisposition fileInfo, 
    	@FormDataParam("mseUdId") String mseUdId, 
    	@FormDataParam("bannerId") String bannerId,
        @FormDataParam("fileType") String fileType)
    {
        MDC.put(MDCKeys.MSE_UDID, mseUdId);
        double fileSize = 0;
        try {
            ConnectAndEngageStats.getInstance().incrementBannerImageUploadCount();
            LOGGER.info("Uploading Banner image for MSE UDID '{}' Banner ID '{}'", mseUdId, bannerId, bannerId);

            // Check if Directory is present
            // Banner Images will be stored under ->
            // md5(mseUdid)/{bannerId}
            File mseUdidDirectory = new File(rootMapLocation, EncryptionUtil.generateMD5(mseUdId));

            // Check if the POI Directory Exists
            if ((!mseUdidDirectory.getCanonicalFile().exists()) || (mseUdidDirectory.getCanonicalFile().exists() && !mseUdidDirectory.getCanonicalFile().isDirectory())) {
                LOGGER.trace("Create banner upload image directory for {}", mseUdidDirectory);
                // Make the Directory and parent directory if necessary
                if (!mseUdidDirectory.mkdirs()) {
                    LOGGER.error("Unable to upload Banner Image. Failed to create directory for MSE UDID {}", mseUdidDirectory);
                    MDC.remove(MDCKeys.MSE_UDID);
                    return Response.status(500).entity("Failed to create directory").build();
                }
            }

            LOGGER.info("Banner directory {}", mseUdidDirectory);
            
            String fileExtention = fileType.substring(fileType.lastIndexOf("/") + 1);
            if (fileExtention.equalsIgnoreCase("jpeg")) {
                fileExtention = "jpg";
            }
            
            fileSize = writeToFile(file, new File(mseUdidDirectory, bannerId + "." + fileExtention).getAbsolutePath());
        }
        catch (IOException e) {
            LOGGER.error("Failed to upload Banner image", e);
            MDC.remove(MDCKeys.MSE_UDID);
            return Response.status(500).entity("Failed to upload Banner images").build();
        }
        catch (NoSuchAlgorithmException algoEx) {
            LOGGER.error("Failed to generate MD5 for MSE UDID {}", algoEx);
            MDC.remove(MDCKeys.MSE_UDID);
            return Response.status(500).entity("Failed to upload Banner images").build();
        }

        LOGGER.info("Completed uploading Banner image for MSE UDID '{}' Banner ID '{}' with size: {} Megabytes", mseUdId, bannerId, fileSize);
        MDC.remove(MDCKeys.MSE_UDID);
        return Response.status(Status.OK).entity("Banner Image loaded for MSE: " + mseUdId + ", Banner ID : " + bannerId).build();
    }
    
    @DELETE
    @Path("/delete/{mseUdId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response deleteSetup(@PathParam("mseUdId") String mseUdId)
    {
        if (mseUdId == null) {
            LOGGER.info("Delete Setup invoked but MSEUDID is null");
            return Response.status(Status.BAD_REQUEST).build();
        }
        
        LOGGER.info("Delete Setup has been invoked for MSE with MSEUDID {}", mseUdId);

        try {
            setup.deleteAllVenues(mseUdId);
            
            LOGGER.info("Deleted Setup for MSE with MSEUDID {}", mseUdId);
                 
            return Response.ok().entity("Deleted MSE Setup").build();
        } catch (Exception e) {
            LOGGER.error("Failed to Delete Setup of MSE with MSEUDID : {}", mseUdId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to Delete MSE Setup" + e.getLocalizedMessage()).build();
        } 
    }
    
    //TODO: Move this out
    // save uploaded file to new location
    private double writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        try {
            OutputStream output = new FileOutputStream(new File(uploadedFileLocation));
            try {
                int bytesRead;
                while ((bytesRead = uploadedInputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
            finally {
                output.close();
            }
        }
        finally {
            uploadedInputStream.close();
        }
        File outputFile = new File(uploadedFileLocation);
        return (outputFile.length() / 1048576L);
    }
}

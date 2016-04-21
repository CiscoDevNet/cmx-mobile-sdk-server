package com.cisco.cmxmobile.services.clients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.dto.BannerDTO;
import com.cisco.cmxmobile.model.Banner;
import com.cisco.cmxmobile.model.Zone;
import com.cisco.cmxmobile.server.stats.MobileAppStats;
import com.cisco.cmxmobile.utils.MDCKeys;

@Component
@Path("/api/cmxmobile/v1/banners")
public class CampaignService 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignService.class);

    @Autowired
    private MobileServerCacheService mobileServerCacheService;
    
    @Context
    private UriInfo uriInfo;

    @Value("${map.location}")
    private String bannerImageLocation;
    
    
    /**
     *  API : /api/cmxmobile/v1/banners/info/:venueId/:floorId/:zoneId
     */
    @GET
    @Path("/info/{venueId}/{floorId}/{zoneId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBannerInfo(@PathParam("venueId") String venueUdId, 
                                    @PathParam("floorId") String floorId, 
                                    @PathParam("zoneId") String zoneId) 
    {
        MobileAppStats.getInstance().incrementCampaignRequestsCount();
        Zone zone = mobileServerCacheService.getZoneByFloor(venueUdId, floorId, zoneId);
        
        if (zone == null) {
            String zoneNotFoundError = new StringBuilder("Zone not found for : VenueId - ").append(venueUdId).
                    append(" FloorId - ").append(floorId).append(" ZoneId - " + zoneId).toString(); 
            
            LOGGER.error(zoneNotFoundError);
            
            return Response.status(Status.NOT_FOUND).entity(zoneNotFoundError).build();
        }
        
        try {
            List<String> campaignList = zone.getCampaignIdList();
            
            if (campaignList == null) {
                return Response.status(Status.NOT_FOUND).entity("No Campaign for Zone : " + zoneId).build();
            }
            
            List<BannerDTO> bannerDTOListForZone = new ArrayList<BannerDTO>();
            
            for (String campaignId : campaignList) {
                //Get all the Banners
                List<Banner> bannerList = mobileServerCacheService.getCampaignCacheService().getAllBannersForCampaign(campaignId);
                
                List<BannerDTO> bannerDTOList = new ArrayList<BannerDTO>();
                
                //Add the Image 
                for (Banner banner : bannerList) {
                    
                    BannerDTO bannerDTO = new BannerDTO();
                    bannerDTO.setId(banner.getId());
                    //TODO: Dynamic
                    bannerDTO.setImageType("jpg");
                    bannerDTO.setName(banner.getTitle());
                    bannerDTO.setZoneid(zoneId);
                    bannerDTO.setVenueid(venueUdId);
                    bannerDTO.setTargetUrl(banner.getTargetUrl());
                    
                    // Construct the based on venueId, floorId, zoneId, bannerId
                    // http://<serverIP>/api/cmxmobile/v1/banners/image/{venueId}/{floorId}/{zoneId}/{bannerId}
                    UriBuilder ub = uriInfo.getBaseUriBuilder();
                    URI bannerImageUri = 
                            ub.path("api/cmxmobile/v1/banners/image").path(venueUdId).path(floorId).path(zoneId).path(banner.getId()).build();
                    
                    bannerDTO.setImageUri(bannerImageUri.toString());
                    
                    bannerDTOList.add(bannerDTO);
                }
                
                bannerDTOListForZone.addAll(bannerDTOList);            
            }
            
            return Response.ok(bannerDTOListForZone, MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            LOGGER.error("Failed to execute getBannerInfo for Venue ID : '{}', Floor ID : '{}', Zone ID : '{}'", venueUdId, floorId, zoneId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } 
    }    
    
    /**
     * API : /api/cmxmobile/v1/banners/image/:venueId/:floorId/:zoneId/:imageId
     * Banners are stored under  md5(mseUdid)/{bannerId}
     */
    @GET
    @Path("/image/{venueId}/{floorId}/{zoneId}/{imageId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBannerImage(@PathParam("venueId") String venueUdId, 
                                @PathParam("floorId") String floorId, 
                                @PathParam("zoneId") String zoneId,
                                @PathParam("imageId") String bannerId)
    {
        MDC.put(MDCKeys.VENUE_ID, venueUdId);
        LOGGER.debug("Request to get Banners for venue : '{}', floor : '{}' and Venue : '{}'", venueUdId, floorId, zoneId);
               
        FileInputStream inputStream = null;

        try {
            
            Zone zone = mobileServerCacheService.getZoneByFloor(venueUdId, floorId, zoneId);
            
            if (zone == null) {
                String zoneNotFoundError = new StringBuilder("Zone not found for : VenueId - ").append(venueUdId).
                        append(" FloorId - ").append(floorId).append(" ZoneId - " + zoneId).toString(); 
                
                LOGGER.error(zoneNotFoundError);
                
                return Response.status(Status.NOT_FOUND).entity(zoneNotFoundError).build();
            }
            
            File mseDirectory = new File(bannerImageLocation, EncryptionUtil.generateMD5(zone.getMseUdId()));
            
            //TODO: Change the extenstion of Image File to be dynamic
            //File imageFile = new File(mseDirectory, bannerId + ".jpg");
            File imageFile = getBannerFileFromFileSystem(mseDirectory, bannerId);
            
            if (!imageFile.exists()) {
                String bannerNotFoundError = new StringBuilder("Banner Image not found for : VenueId - ").append(venueUdId).
                    append(" FloorId - ").append(floorId).append(" ZoneId - ").append(zoneId).
                    append(" BannerId - ").append(bannerId).toString();
                
                LOGGER.error(bannerNotFoundError);
                
                return Response.status(Status.NOT_FOUND).entity(bannerNotFoundError).build();
            }
            
            LOGGER.debug("Banner image file name {} is for Venue ID : '{}', Floor ID '{}', Zone ID '{}', Banner ID : '{}'", 
                imageFile, venueUdId, floorId, zoneId, bannerId);
            
            inputStream = new FileInputStream(imageFile);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = inputStream.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
            byte[] bytes = bos.toByteArray();
            ResponseBuilder response = Response.ok(bytes);
            
            response.type("image/gif");
            
            String fileName = imageFile.getName();
            if (fileName != null && fileName.length() > 0) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (extension != null && extension.equalsIgnoreCase("png")) {
                    response.type("image/png");
                }
                else if (extension != null && extension.equalsIgnoreCase("jpg")) {
                    response.type("image/jpeg");
                }
            }
            LOGGER.debug("Completed request to get map venue image for '{}' with floor ID '{}'", venueUdId, floorId);
            MDC.remove(MDCKeys.VENUE_ID);
            return response.build();
        }
        catch (Exception ex) {
            MDC.remove(MDCKeys.VENUE_ID);
            
            String bannerRetrievalError = new StringBuilder("Failed to get Banner Image  for : VenueId - ").append(venueUdId).
                    append(" FloorId - ").append(floorId).append(" ZoneId - ").append(zoneId).
                    append(" BannerId - ").append(bannerId).toString();
                
            LOGGER.error(bannerRetrievalError, ex);

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(bannerRetrievalError).build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    LOGGER.info("Failed to close Input Stream", e);
                }
            }
        }
    }
    
    private File getBannerFileFromFileSystem(final File path, final String bannerId)
    {
        File bannerFile = null;
        File [] files = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(bannerId);
            }
        });
        
        if (files != null && files.length > 0) {
            if (files.length > 1) {
                LOGGER.error("Found '{}' banners of name '{}' under '{}' folder", files.length, bannerId, path);
            }                //Pick the First One
            bannerFile = files[0];
        } else {
            LOGGER.error("No banners of name '{}' were found under '{}' folder", bannerId, path);
        }
        
        return bannerFile;
    }
    

}

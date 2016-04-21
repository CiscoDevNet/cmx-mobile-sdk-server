package com.cisco.cmxmobile.services.mse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.model.CasFloorInfo;
import com.cisco.cmxmobile.model.CasVenue;
import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.cacheService.service.SetupSource;
import com.cisco.cmxmobile.setup.CasFloor;
import com.cisco.cmxmobile.setup.CasFloorImageInfo;
import com.cisco.cmxmobile.setup.CasFloorImageInfoList;
import com.cisco.cmxmobile.setup.CasSetup;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

@Component
@Path("/api/v1/obsoleteSetup")
public class ObsoleteSetupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObsoleteSetupService.class);

    @Autowired
    public MobileServerCacheService mobileServerCacheService;

    @Context
    private UriInfo uriInfo;

    @Value("${map.location}")
    private String rootMapLocation;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupByCas(String setupInfo) {

        LOGGER.info("Obsolete setup has been invoked");
        // This is to be sent in the response
        // "images_needed": [
        // {
        // "imageName": "domain_0_1349311055718.png",
        // "imageType": "image/png",
        // "uploadUrl": "http://<serverIP>/upload/{uniqueFloorId}
        // } ]
        LOGGER.trace("Obsolete setup has data {}", setupInfo);

        CasFloorImageInfoList casFloorImageInfoListToRetrieve = new CasFloorImageInfoList();
        
        String casFloorImageInfoListToRetrieveJson = null;
        
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Parse and Map The JSON CasSetup information to an Object
            CasSetup casSetup = mapper.readValue(setupInfo, CasSetup.class);

            // Get the Floors from the casSetup
            List<CasFloorInfo> floorInfoList = toCasFloorInfoList(casSetup, casSetup.getFloors());

            // Create a List of Images that need to be retrieved from CAS
            List<CasFloorImageInfo> casFloorImageInfoList = new ArrayList<CasFloorImageInfo>();
            for (CasFloorInfo casFloorInfo : floorInfoList) {

                CasFloorImageInfo floorImageInfo = new CasFloorImageInfo();
                floorImageInfo.setImageName(casFloorInfo.getImageName());
                floorImageInfo.setImageType(casFloorInfo.getImageType());

                // Generate Unique ID based on appId, mseUdi, aesUid for MAP
                // Store CasFloorInfo object which is a model object in Cache
                // for later lookup
                //TODO : To be moved out
                /*casFloor.get
                CasFloorInfo casFloorInfo = createCasFloorInfo(Long.toString(casSetup.getAppId()), casSetup.getMseUdi(), casFloor.getAesUid());
                mobileServerCacheService.save(casFloorInfo);
                */

                // Add the Unique Identifier based on appId, mseUdi, aesUid to
                // the path
                // http://<serverIP>/upload/{uniqueVenueId}/{uniqueFloorId}
                UriBuilder ub = uriInfo.getAbsolutePathBuilder();
                URI imageUploadUri = ub.path("upload").path(casFloorInfo.getVenueUdid()).path(casFloorInfo.getAesUid()).build();
                floorImageInfo.setUploadUrl(imageUploadUri.toASCIIString());

                casFloorImageInfoList.add(floorImageInfo);
            }

            casFloorImageInfoListToRetrieve.setImagesNeeded(casFloorImageInfoList);
            
            casFloorImageInfoListToRetrieveJson = 
                mapper.writeValueAsString(casFloorImageInfoListToRetrieve).replaceFirst("imagesNeeded", "images_needed");
            
            //Save the Venue and corresponding floors
            mobileServerCacheService.saveCasVenue(buildVenueList(floorInfoList));
            
            //Get the MSEUDID from the CAS Setup in the VenueList and then
            //Save the Setup Source
            SetupSource.LOCATION_MODULE.apply(casSetup.getMseUdi());
            
        }
        catch (JsonParseException e) {
            LOGGER.error("Failed to Parse Setup Info : {} ", setupInfo, e);
            return Response.status(400).entity(casFloorImageInfoListToRetrieve).build();
        }
        catch (JsonMappingException e) {
            LOGGER.error("Failed to Mapping from JSON : {}", setupInfo, e);
            return Response.status(400).entity(casFloorImageInfoListToRetrieve).build();
        }
        catch (IOException e) {
            LOGGER.error("IO Exception while Mapping from JSON : {}", setupInfo, e);
            return Response.status(500).entity(casFloorImageInfoListToRetrieve).build();
        }

        LOGGER.info("Completed obsolete setup");
        return Response.status(201).entity(casFloorImageInfoListToRetrieveJson).build();
    }

    @POST
    @Path("/upload/{venueUdid}/{floorId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response handleUpload(@PathParam("venueUdid") String venueUdid, 
                                 @PathParam("floorId") String floorId, 
                                 FormDataMultiPart data) throws Exception 
    {
        LOGGER.info("Obsolete upload of file");
        String fileName = ((FormDataBodyPart) data.getBodyParts().get(0)).getContentDisposition().getFileName();
        LOGGER.info("Obsolete file name {}", fileName);
        // String subType =
        // ((FormDataBodyPart)data.getBodyParts().get(0)).getMediaType().getSubtype();
        BodyPartEntity entity = (BodyPartEntity) ((FormDataBodyPart) data.getBodyParts().get(0)).getEntity();

        // Check if Directory is present
        // If failed to create MD5 for mseVenueUdId, none of the floor
        // images will save
        File venueDirectory = new File(rootMapLocation, EncryptionUtil.generateMD5(venueUdid));

        // Check if the Venue Directory Exists
        if ((!venueDirectory.getCanonicalFile().exists()) || (venueDirectory.getCanonicalFile().exists() && !venueDirectory.getCanonicalFile().isDirectory())) {
            LOGGER.trace("Create venue upload image directory for {}", venueDirectory);
            // Make the Directory
            if (!venueDirectory.mkdir()) {
                LOGGER.error("Unable to upload image since the directory was failed to be created for {}", venueDirectory);
                return Response.status(500).entity("Failed to create directory").build();
            }
        }
        
        // TODO: File Location need to loaded from Properties File
        // Get the Resource Information by uniqueId from Cache and create File
        // Name
        String subType = fileName.substring(fileName.lastIndexOf("."));
        String uploadedFileLocation = new File(venueDirectory, floorId + subType).getCanonicalPath();

        // save it
        writeToFile(entity.getInputStream(), uploadedFileLocation);

        LOGGER.info("Completed obsolete upload of file {}", fileName);
        return Response.status(200).entity("File Uploaded : " + fileName).build();
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
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
    }
    
    private List<CasFloorInfo> toCasFloorInfoList(CasSetup casSetup, List<CasFloor> casFloorList) 
    {
        List<CasFloorInfo> casFloorInfoList = new ArrayList<CasFloorInfo>();
        String appId = Long.toString(casSetup.getAppId());
        String mseUdi = casSetup.getMseUdi();
        for (CasFloor casFloor : casFloorList) {
            casFloorInfoList.add(createCasFloorInfo(appId, mseUdi, casFloor));
        }
        return casFloorInfoList;
    }

    private CasFloorInfo createCasFloorInfo(String appId, String mseUdi, CasFloor casFloor) {
        CasFloorInfo casFloorInfo = new CasFloorInfo();
        casFloorInfo.setAppId(appId);
        casFloorInfo.setMseUdi(mseUdi);
        casFloorInfo.setAesUid(casFloor.getAesUid());
        
        casFloorInfo.setFloorName(casFloor.getName());
        
        List<String> idHirarchyList = casFloor.getIdHierarchy();
        List<String> textHirarchyList = casFloor.getTextHierarchy();
        
        if (idHirarchyList.size() >= 2 && textHirarchyList.size() >= 2) {
            casFloorInfo.setVenueId(casFloor.getIdHierarchy().get(casFloor.getIdHierarchy().size() - 2));
            casFloorInfo.setVenueName(casFloor.getTextHierarchy().get(casFloor.getTextHierarchy().size() - 2));
        } else {
            //Add Defaults - UNKNOWN
            casFloorInfo.setVenueId(Integer.toString(Integer.MIN_VALUE));
            casFloorInfo.setVenueName("UNKNOWN");
        }
        
        //VenueUdid = MSEUdi:VenueId:AesUid - to be truly unique
        casFloorInfo.setVenueUdid(casFloorInfo.getMseUdi() + ":" + casFloorInfo.getVenueId());
        
        //Floor Image Details
        casFloorInfo.setImageName(casFloor.getImageName());
        casFloorInfo.setImageType(casFloor.getImageType());
        casFloorInfo.setLength(casFloor.getLength());
        casFloorInfo.setWidth(casFloor.getWidth());
        casFloorInfo.setHeight(casFloor.getHeight());
        
        return casFloorInfo;
    }
    
    public List<CasVenue> buildVenueList(List<CasFloorInfo> casFloorInfoList) 
    {
        Map<String, List<CasFloorInfo>> venueIdFloorMapping = new HashMap<String, List<CasFloorInfo>>();
        List<CasVenue> casVenueList = new ArrayList<CasVenue>();
        
        //Create the Mapping
        for (CasFloorInfo casFloorInfo : casFloorInfoList) {
            String venueUdid = casFloorInfo.getVenueUdid();
            if (venueIdFloorMapping.containsKey(venueUdid)) {
                venueIdFloorMapping.get(venueUdid).add(casFloorInfo);
            } else {
                List<CasFloorInfo> floorList = new ArrayList<CasFloorInfo>();
                floorList.add(casFloorInfo);
                venueIdFloorMapping.put(venueUdid, floorList);
            }
        }
        
        //Create the Venue List
        Iterator<Map.Entry<String, List<CasFloorInfo>>> iter = venueIdFloorMapping.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<CasFloorInfo>> entry = iter.next();
            CasVenue venue = new CasVenue();
            venue.setVenueUdId(entry.getKey());
            List<CasFloorInfo> floorList = entry.getValue();
            
            //Get one of the Floors to extract MSEID and APPID
            CasFloorInfo casFloorInfo = floorList.get(0);
            venue.setAppId(casFloorInfo.getAppId());
            venue.setMseUdid(casFloorInfo.getMseUdi());
            venue.setVenueName(casFloorInfo.getVenueName());
            venue.setVenueId(casFloorInfo.getVenueId());
            venue.setCasFloorInfoList(floorList);
            
            casVenueList.add(venue);
        }
        
        return casVenueList;
    }

}

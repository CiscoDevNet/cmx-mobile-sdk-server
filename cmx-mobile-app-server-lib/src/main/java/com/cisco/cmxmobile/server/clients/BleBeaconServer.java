package com.cisco.cmxmobile.server.clients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.dto.BleBeaconDTO;
import com.cisco.cmxmobile.model.BleBeacon;

@Component
public class BleBeaconServer 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BleBeaconServer.class);
    
    @Autowired
    private MobileServerCacheService mobileServerCacheService;
    
    public List<BleBeaconDTO> getBleBeaconsInfoListByFloor(String venueUdId, String floorId) 
    {
        LOGGER.debug("Request to get BLE Beacon List information for Venue {} and Floor {}", venueUdId, floorId);

        List<BleBeaconDTO> bleBeaconDTOList = new ArrayList<BleBeaconDTO>();

        List<BleBeacon> bleBeaconList = mobileServerCacheService.getBleBeaonListByFloor(venueUdId, floorId);
        for (BleBeacon bleBeacon : bleBeaconList) {
            bleBeaconDTOList.add(createBleBeaconDTO(bleBeacon));
        }

        LOGGER.debug("Completed request to get venue information for {}. Number of BLE Beacons {}", venueUdId, bleBeaconDTOList.size());
        
        return bleBeaconDTOList;
    }

    public List<BleBeaconDTO> getBleBeaconsInfoListByVenue(String venueUdId) 
    {
        LOGGER.debug("Request to get BLE Beacon List information for", venueUdId);
        
        List<BleBeaconDTO> bleBeaconDTOList = new ArrayList<BleBeaconDTO>();

        List<BleBeacon> bleBeaconList = mobileServerCacheService.getBleBeaonListByVenue(venueUdId);
        for (BleBeacon bleBeacon : bleBeaconList) {
            bleBeaconDTOList.add(createBleBeaconDTO(bleBeacon));
        }

        LOGGER.debug("Completed request to get venue information for {}. Number of BLE Beacons {}", venueUdId, bleBeaconDTOList.size());
        
        return bleBeaconDTOList;
    }

    private BleBeaconDTO createBleBeaconDTO(BleBeacon bleBeacon)
    {
        BleBeaconDTO bleBeaconDTO = new BleBeaconDTO();
        bleBeaconDTO.setUuid(bleBeacon.getUuid());
        bleBeaconDTO.setMajor(bleBeacon.getMajor());
        bleBeaconDTO.setMinor(bleBeacon.getMinor());
        bleBeaconDTO.setMfgId(bleBeacon.getMfgId());
        bleBeaconDTO.setFloorId(bleBeacon.getFloorId());
        bleBeaconDTO.setxCord(bleBeacon.getxCord());
        bleBeaconDTO.setyCord(bleBeacon.getyCord());
        bleBeaconDTO.setzCord(bleBeacon.getzCord());
        bleBeaconDTO.setBleBeaconName(bleBeacon.getBleBeaconName());
        bleBeaconDTO.setBleBeaconType(bleBeacon.getBleBeaconType());
        bleBeaconDTO.setMessage(bleBeacon.getMessage());
        bleBeaconDTO.setRegionIdentifier(bleBeacon.getRegionIdentifier());
        bleBeaconDTO.setZoneId(bleBeacon.getZoneId());     
        
        return bleBeaconDTO;
    }
}

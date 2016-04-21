package com.cisco.server.cmxmobile.clientService.FunctionalTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aes.notification.datamodel.AssociationEvent;
import com.aes.notification.datamodel.MovementEvent;
import com.aes.rest.datamodel.location.MapCoordinate;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheException;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.PendingAssociationNotification;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.services.clients.ClientService;
import com.cisco.cmxmobile.services.mse.LocationNotificationService;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class ClientServiceIT extends AbstractTestNGSpringContextTests {
	
	//TODO: automate the creation of these URLs
	final String mNotificationServiceURL = 
			"http://localhost:9090/cmx-cloud-server/api/cmxmobile/v1/notify";
	final String mClientServiceURL = 
			"http://localhost:9090/cmx-cloud-server/api/cmxmobile/v1/clients";
	
	MovementEvent movementEventForAABBCCDDEEFF;
	MovementEvent movementEvent_nonOptedInClient;
	AssociationEvent associationEvent_nonOptedInClient;
	AssociationEvent associationEvent_onlyOnceClient;
	AssociationEvent associationEvent_duplicateRegistration;
	
	final String MOVEMENT_EVENT_FOR_AABBCCDDEEFF = "{\"MovementEvent\": {\"subscriptionName\":"+
			"\"Test-Event-LocationChange\"," +
	        "\"entity\": \"WIRELESS_CLIENTS\","+
	        "\"deviceId\": \"aa:bb:cc:dd:ee:ff\"," +
	        "\"locationMapHierarchy\": \"System Campus>SJC-14>3rd Floor\"," +
	        "\"locationCoordinate\": {" +
	            "\"x\": 20.0," +
	            "\"y\": 20.0," +
	            "\"unit\": \"FEET\"" +
	        "},\"moveDistanceInFt\": 0," +
	        "\"timestamp\": \"2013-08-08T11:22:29.649-0700\"}}";
	ClientConfig clientConfig;
	
	@Autowired
	private MobileServerCacheService mobileServerCacheService;

	@BeforeClass
	public void setupObjects() {
		String nonOptedInClient_MACAddress = "b0:bb:0b:b0:bb:0b";
		String registerClientMAC = "00:00:00:00:00:00";
		String duplicateRegisterClientMAC = "00:00:00:00:00:01";
		clientConfig = new DefaultClientConfig(); 
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		
		MapCoordinate aMapCoordinate = new MapCoordinate();
		aMapCoordinate.setX(20.0f);
		aMapCoordinate.setY(20.0f);
		List<String> clientIPList = new ArrayList<String>(1);
		clientIPList.add("2.2.2.2");
		
		movementEventForAABBCCDDEEFF = new MovementEvent("subscriptionName");
		movementEventForAABBCCDDEEFF.setDeviceId("aa:bb:cc:dd:ee:ff");
		
		movementEventForAABBCCDDEEFF.setLocationCoordinate(aMapCoordinate);
		movementEventForAABBCCDDEEFF.setLocationMapHierarchy("System Campus>Building>Floor");
		//leaving off the timestamp and reference marker
		
		movementEvent_nonOptedInClient = new MovementEvent("subscriptionName");
		movementEvent_nonOptedInClient.setDeviceId(nonOptedInClient_MACAddress);
		movementEvent_nonOptedInClient.setLocationCoordinate(aMapCoordinate);
		movementEvent_nonOptedInClient.setLocationMapHierarchy("System Campus>Building>Floor");
		
		associationEvent_nonOptedInClient = new AssociationEvent("subscriptionName");
		associationEvent_nonOptedInClient.setDeviceId(movementEvent_nonOptedInClient.getDeviceId());
		associationEvent_nonOptedInClient.setApMacAddress("aa:aa:aa:aa:aa:aa");
		associationEvent_nonOptedInClient.setAssociation(true);
		associationEvent_nonOptedInClient.setIpAddress(clientIPList);
		associationEvent_nonOptedInClient.setLocationCoordinate(aMapCoordinate);
		
		associationEvent_onlyOnceClient = new AssociationEvent("subscriptionName");
		associationEvent_onlyOnceClient.setDeviceId(registerClientMAC);
		associationEvent_onlyOnceClient.setApMacAddress("aa:aa:aa:aa:aa:ab");
		associationEvent_onlyOnceClient.setAssociation(true);
		associationEvent_onlyOnceClient.setIpAddress(clientIPList);
		associationEvent_onlyOnceClient.setLocationCoordinate(aMapCoordinate);
		
		associationEvent_duplicateRegistration = new AssociationEvent("subscriptionName");
		associationEvent_duplicateRegistration.setDeviceId(duplicateRegisterClientMAC);
		associationEvent_duplicateRegistration.setApMacAddress("aa:aa:aa:aa:aa:ac");
		associationEvent_duplicateRegistration.setAssociation(true);
		associationEvent_duplicateRegistration.setIpAddress(clientIPList);
		associationEvent_duplicateRegistration.setLocationCoordinate(aMapCoordinate);
	}
	
	
	//@Test
	public void testDropLocationNotificationForUnregisteredTest() {
		//tests that we drop location notifications for clients that haven't 
		// opted in
		
		//send location notification for mac address b0:bb:0b:b0:bb:0b
		String clientMAC = movementEvent_nonOptedInClient.getDeviceId();
		WirelessClient notPresentClient = mobileServerCacheService.getWirelessClient(clientMAC);
		System.out.println("@Test - testLocationNotificationNoRegister - initial client get");
		Assert.assertNull(notPresentClient);
		Client restClient = Client.create(clientConfig);
		WebResource webResource = restClient.resource(mNotificationServiceURL);
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.type(MediaType.APPLICATION_JSON_VALUE)
				.entity(wrapEvent(movementEvent_nonOptedInClient, LocationNotificationService.Event.MOVEMENT))
				.post(ClientResponse.class);
		
		//The cloud server shouldn't give the MSE any hint as to whether or not
		// that client is opted in
		Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
		
		//check location table for mac address b0:bb:0b:b0:bb:0b
		notPresentClient = mobileServerCacheService.getWirelessClient(clientMAC);
		System.out.println("@Test - testLocationNotificationNoRegister - secondary client get.");
		if (notPresentClient != null) {
			System.out.println("MAC=" + notPresentClient.getMacAddress());
		}
		Assert.assertNull(notPresentClient);
	}
	
	
	//@Test
	public void testLocationNotificationSameVenue() {
		//tests that a location notification for an opted in client updates its
		// location
		//register-slash-opt-in a client
		//TODO: set up a venue for the client to 'be' in
		String clientMAC = "aa:bb:cc:dd:ee:ff";
		String clientPushToken = "clientpushtoken";
		WirelessClient client = new WirelessClient(clientMAC, "100", "1000");
		client.setX(10.0f);
		client.setY(10.0f);
		client.setFloorId("10");
		client.setVenueUdId("10");
		client.setPushNotificationRegistrationId(clientPushToken);
		client.setDeviceType(DeviceType.IOS);
		client.setNotificationZone("how did Ramesh's code work?!", 5);
		
		try {
			mobileServerCacheService.addOrUpdateWirelessClient(client, -1, false);
		} catch (MobileServerCacheException e) {
			e.printStackTrace();
	        Assert.fail(e.getMessage() + "; Classname = " + e.getClass().toString());
		}
		
		//send a location notification about that client
		//TODO: needs MSE authentication cookie
		Client restClient = Client.create();
		WebResource webResource = restClient.resource(mNotificationServiceURL);
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.type(MediaType.APPLICATION_JSON_VALUE)
				.entity(MOVEMENT_EVENT_FOR_AABBCCDDEEFF)
				.post(ClientResponse.class);
		Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
		client = mobileServerCacheService.getWirelessClient(clientMAC);
		Assert.assertEquals(client.getX(), 20.0f);
		Assert.assertEquals(client.getY(), 20.0f);
	}
	
	
	@Test
	public void testLocationNotificationNewVenue() {
		//tests that a location notification placing the user in a new venue
		// 1. expires the user's session so they have to do the location challenge
		//    again
		// 2. actually shows the user's location in the new venue
	}
	
	@Test
	public void testMalformedLocationNotification() {
		
	}
	
	
	//@Test
	public void testAssociationNotificationForUnregistered() {
		//tests that an association notification for a client that hasn't 
		// opted in gets stored for later
		String clientMAC = associationEvent_nonOptedInClient.getDeviceId();
		WirelessClient notPresentClient = mobileServerCacheService.getWirelessClient(clientMAC);
		Assert.assertNull(notPresentClient);
		
		
		Client mseClient = Client.create(clientConfig);
		//TODO: needs MSE authentication cookie
		WebResource webResource = mseClient.resource(mNotificationServiceURL);
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.type(MediaType.APPLICATION_JSON_VALUE)
				.entity(wrapEvent(associationEvent_nonOptedInClient, LocationNotificationService.Event.ASSOCIATION))
				.post(ClientResponse.class);
		Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
		
		PendingAssociationNotification pan = 
				mobileServerCacheService.getPendingAssociation(
						associationEvent_nonOptedInClient.getApMacAddress(), 
						associationEvent_nonOptedInClient.getIpAddress().get(0));
		Assert.assertNotNull(pan);
		Assert.assertEquals(pan.getApMAC(), associationEvent_nonOptedInClient.getApMacAddress());
	}
	
	
	@Test
	public void testAssociationNotificationRegistered() {
		//tests that an association notification for a client that has
		// opted in gets dropped
	}
	
	@Test
	public void testMalformedAssociationNotification() {
		
	}
	
	@Test
	public void testRegisterRequest() {
		//a registration request from a not-yet-registered client must create a record
		// for later
	}
	
	@Test
	public void testMalformedRegisterRequest() {
		
	}
	
	
	//@Test
	public void testRegisterRequestAfterAssociationForiOS() {
		//a registration request from a not-yet-registered client, received 
		// after an association notification for that client, should register
		// that client and remove the association notification
		String pushTokenID = "uniquePushToken";
		String apMAC = associationEvent_onlyOnceClient.getApMacAddress();
		String clientIP = associationEvent_onlyOnceClient.getIpAddress().get(0);
		
		//check that client doesn't exist yet
		WirelessClient existingClient = mobileServerCacheService.getWirelessClient(associationEvent_onlyOnceClient.getDeviceId());
		Assert.assertNull(existingClient);
		
		//send association event
		Client mseClient = Client.create(clientConfig);
		//TODO: needs MSE authentication cookie
		WebResource webResource = mseClient.resource(mNotificationServiceURL);
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.type(MediaType.APPLICATION_JSON_VALUE)
				.entity(wrapEvent(associationEvent_onlyOnceClient, LocationNotificationService.Event.ASSOCIATION))
				.post(ClientResponse.class);
		Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
		
		//send registration
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
		formData.add("apMACAddress", apMAC);
		formData.add("clientIPAddress", clientIP);
		formData.add("pushNotificationRegistrationId", pushTokenID);
		formData.add("clientType", DeviceType.IOS.toString());
		WebResource clientResource = mseClient.resource(mClientServiceURL + "/register");
		ClientResponse registrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		
		Assert.assertEquals(registrationResponse.getStatus(), ClientResponse.Status.CREATED.getStatusCode());
		//check for wireless client presence
		WirelessClient clientByMAC = mobileServerCacheService.getWirelessClient(associationEvent_onlyOnceClient.getDeviceId());
		Assert.assertNotNull(clientByMAC);
		WirelessClient clientByToken = mobileServerCacheService.getWirelessClientByUniqueID(clientByMAC.getUniqueID());
		Assert.assertNotNull(clientByToken);
		Assert.assertEquals(clientByMAC, clientByToken);
		validateRegistrationCookies(registrationResponse.getCookies());
		
	}
	
	
	@Test
	public void testAssociationNotificationAfterRegisterRequest() {
		//an association notification sent after a registration request
		// that matches that registration request should register the client
		//TODO: wait a minute, this produces some rather bad race conditions 'n'
		// things, plus the whole job-in-progress-check-back-later mess...
	}
	
	
	//@Test
	public void testDuplicateRegistrationsForiOS() {
		Client mseClient = Client.create(clientConfig);
		//TODO: needs MSE authentication cookie
		WebResource webResource = mseClient.resource(mNotificationServiceURL);
		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.type(MediaType.APPLICATION_JSON_VALUE)
				.entity(wrapEvent(associationEvent_duplicateRegistration, LocationNotificationService.Event.ASSOCIATION))
				.post(ClientResponse.class);
		Assert.assertEquals(response.getStatus(), ClientResponse.Status.OK.getStatusCode());
		
		//send registration
		String pushTokenID = "duplicatePushToken";
		String apMAC = associationEvent_duplicateRegistration.getApMacAddress();
		String clientIP = associationEvent_duplicateRegistration.getIpAddress().get(0);
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
		formData.add("apMACAddress", apMAC);
		formData.add("clientIPAddress", clientIP);
		formData.add("pushNotificationRegistrationId", pushTokenID);
		formData.add("clientType", DeviceType.IOS.toString());

		//register once
		WebResource clientResource = mseClient.resource(mClientServiceURL + "/register");
		ClientResponse registrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		
		Assert.assertEquals(registrationResponse.getStatus(), ClientResponse.Status.CREATED.getStatusCode());
		//check for wireless client presence
		WirelessClient clientByMAC = mobileServerCacheService.getWirelessClient(associationEvent_duplicateRegistration.getDeviceId());
		Assert.assertNotNull(clientByMAC);

		//register again - this should make an error
		ClientResponse secondRegistrationAttempt = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		
		Assert.assertEquals(secondRegistrationAttempt.getStatus(), ClientResponse.Status.CONFLICT.getStatusCode());
	}
	
	//@Test
	public void testRegistrationForAndroid() {
		final String androidMACAddressForRegistration = "d2:01:d5:a2:ec:00";
		final String pushTokenForAndroidRegistration = "android registration";
		
		WirelessClient existingClient = mobileServerCacheService.getWirelessClient(androidMACAddressForRegistration);
		Assert.assertNull(existingClient);
		
		Client webClient = Client.create(clientConfig);
		WebResource clientResource = webClient.resource(mClientServiceURL + "/register");
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
		formData.add("clientMACAddress", androidMACAddressForRegistration);
		formData.add("pushNotificationRegistrationId", pushTokenForAndroidRegistration);
		formData.add("clientType", "android");
		ClientResponse registrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		
		Assert.assertEquals(registrationResponse.getStatus(), ClientResponse.Status.CREATED.getStatusCode());
		WirelessClient clientByMAC = mobileServerCacheService.getWirelessClient(androidMACAddressForRegistration);
		Assert.assertNotNull(clientByMAC);
		WirelessClient clientByToken = mobileServerCacheService.getWirelessClientByUniqueID(clientByMAC.getUniqueID());
		Assert.assertNotNull(clientByToken);
		Assert.assertEquals(clientByToken, clientByMAC);

		validateRegistrationCookies(registrationResponse.getCookies());
	}
	
	//@Test
	public void testDuplicateRegistrationForAndroid() {
		final String androidMACAddressForDuplicateRegistration1 = "d0:01:11:ca:7e:55";
		final String androidMACAddressForDuplicateRegistration2 = "d0:01:11:ca:7e:56";
		final String pushTokenForAndroidDuplicateRegistration1 = "duplicate android registration";
		final String pushTokenForAndroidDuplicateRegistration2 = "duplicate android registration2";
		
		//make sure no clients exist yet
		WirelessClient existingClient = mobileServerCacheService.getWirelessClient(androidMACAddressForDuplicateRegistration1);
		Assert.assertNull(existingClient);
		existingClient = mobileServerCacheService.getWirelessClient(androidMACAddressForDuplicateRegistration2);
		Assert.assertNull(existingClient);

		//send registration
		Client webClient = Client.create(clientConfig);
		WebResource clientResource = webClient.resource(mClientServiceURL + "/register");
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
		formData.add("clientMACAddress", androidMACAddressForDuplicateRegistration1);
		formData.add("pushNotificationRegistrationId", pushTokenForAndroidDuplicateRegistration1);
		formData.add("clientType", "android");
		ClientResponse registrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		
		Assert.assertEquals(registrationResponse.getStatus(), ClientResponse.Status.CREATED.getStatusCode());
		WirelessClient clientByMAC = mobileServerCacheService.getWirelessClient(androidMACAddressForDuplicateRegistration1);
		Assert.assertNotNull(clientByMAC);
		WirelessClient clientByToken = mobileServerCacheService.getWirelessClientByUniqueID(clientByMAC.getUniqueID());
		Assert.assertNotNull(clientByToken);
		Assert.assertEquals(clientByToken, clientByMAC);
		
		//duplicate registration - test the duplicate MAC address
		formData = new MultivaluedMapImpl();
		formData.add("clientMACAddress", androidMACAddressForDuplicateRegistration1);
		formData.add("pushNotificationRegistrationId", pushTokenForAndroidDuplicateRegistration2);
		formData.add("clientType", "android");
		ClientResponse duplicateRegistrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		Assert.assertEquals(duplicateRegistrationResponse.getStatus(), ClientResponse.Status.CONFLICT.getStatusCode());
		
		//duplicate registration - test the duplicate push token
		/*
		formData = new MultivaluedMapImpl();
		formData.add("clientMACAddress", androidMACAddressForDuplicateRegistration2);
		formData.add("pushNotificationRegistrationId", pushTokenForAndroidDuplicateRegistration1);
		formData.add("clientType", "android");
		duplicateRegistrationResponse = clientResource
				.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.post(ClientResponse.class, formData);
		Assert.assertEquals(duplicateRegistrationResponse.getStatus(), ClientResponse.Status.CONFLICT.getStatusCode());
		*/
	}
	
	@Test
	public void testMalformedRegistrationForAndroid() {
		
	}
	
	//@Test
	public void testRejectAccessToClientLocation() {
		//make a client
		WirelessClient client = new WirelessClient("ff:ff:ff:ff:ff:ff", "100", "1000");
		client.setPushNotificationRegistrationId("pushnotification");
		client.setDeviceType(DeviceType.ANDROID);
		
		try {
			mobileServerCacheService.addOrUpdateWirelessClient(client, -1, false);
		} catch (MobileServerCacheException e) {
			Assert.fail();
		}
		//send a request to get its location - should be a 401/not authorized
		Client webClient = Client.create(clientConfig);
		WebResource clientResource = webClient.resource(mClientServiceURL + "/location/" + client.getUsername());
		ClientResponse locationResponse = clientResource.get(ClientResponse.class);
		
		Assert.assertEquals(locationResponse.getStatus(), ClientResponse.Status.UNAUTHORIZED.getStatusCode());
		//include a cookie for its password which is _not_ the client's password - should still be a 401/not authorized
		ClientResponse locationResponseWithBadCookie = 
				clientResource.cookie(new Cookie(ClientService.CLIENT_AUTHENTICATION_COOKIE_NAME, "bob"))
				.get(ClientResponse.class);
		
		Assert.assertEquals(locationResponseWithBadCookie.getStatus(), ClientResponse.Status.UNAUTHORIZED.getStatusCode());
	}
	
	//@Test
	public void testAccessToClientLocation() {
		//make a client
		WirelessClient client = new WirelessClient("ee:ee:ee:ee:ee:ee", "100", "1000");
		client.setPushNotificationRegistrationId("pushnotification2");
		client.setDeviceType(DeviceType.ANDROID);
		
		try {
			mobileServerCacheService.addOrUpdateWirelessClient(client, -1, false);
		} catch (MobileServerCacheException e) {
			Assert.fail();
		}
		//include the cookie for its password - should be a 200/OK
		Client webClient = Client.create(clientConfig);
		WebResource clientResource = webClient.resource(mClientServiceURL + "/location/" + client.getUsername());
		ClientResponse locationResponse = clientResource
				.cookie(new Cookie(ClientService.CLIENT_AUTHENTICATION_COOKIE_NAME, client.getPassword()))
				.get(ClientResponse.class);
		
		Assert.assertEquals(locationResponse.getStatus(), ClientResponse.Status.OK.getStatusCode());
	}
	
	@Test
	public void testOptOut() {
		//clients should be able to opt out
	}
	
	@Test
	public void testDeviceTyeEquals() {
		DeviceType da1 = DeviceType.ANDROID;
		DeviceType da2 = DeviceType.ANDROID;
		DeviceType os1 = DeviceType.IOS;
		Assert.assertTrue(da1.equals(da2));
		Assert.assertFalse(da1.equals(os1));
	}
	
	@Test
	public void testUnauthorizedOptOut() {
		//clients should not be able to opt other clients out
	}
	
	/*@Test
	public void testMalformedOptOut() {
		
	}*/
	
	/*LocationNotificationService assumes that each event it receives from the
	 * MSE is wrapped in a string that explains what type of event it is.*/
	private HashMap<String,Object> wrapEvent(Object o, LocationNotificationService.Event e) {
		HashMap<String,Object> result = new HashMap<String,Object>(1);
		result.put(e.toString(), o);
		return result;
	}
	
	private void validateRegistrationCookies(List<NewCookie> registrationCookies) {
		Assert.assertNotNull(registrationCookies);
		
		boolean foundPasswordCookie = false;
		for (NewCookie c : registrationCookies) {
			if (ClientService.CLIENT_AUTHENTICATION_COOKIE_NAME.equals(c.getName())) {
				foundPasswordCookie = true;
				Assert.assertNotEquals(c.getValue().length(), 0);
				break;
			}
		}
		Assert.assertEquals(foundPasswordCookie, true);
	}
}

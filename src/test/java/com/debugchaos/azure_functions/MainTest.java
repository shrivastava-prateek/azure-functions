package com.debugchaos.azure_functions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.client.utils.URIBuilder;

import com.debugchaos.azure_functions.entity.UserConfig;

public class MainTest {
	
	private static final String BASE_URI = "https://api.twitter.com/2/";
	private static final String USER_TWEETS = "users/:id/tweets";
	
	public static void main(String[] args) throws URISyntaxException {
		URI uri = new URIBuilder(BASE_URI)
					.setPathSegments("param4")
					.setParameter("pathparam1", "SETTING path param")
			      .addParameter("param1", "value1")
			      .addParameter("param2", "value2")
			      .build();
		System.out.println(uri.toString());
		
		Random r = new Random();
		System.out.println(r.nextInt(2));
		
		Map<String, UserConfig> userConfigMap = new HashMap<>();
		userConfigMap.put("1", new UserConfig());
		userConfigMap.put("2", new UserConfig());
		
		UserConfig[] userConfigs = userConfigMap.values().toArray(new UserConfig[0]);
		
		System.out.println(userConfigs.length);
		
		
	}

}

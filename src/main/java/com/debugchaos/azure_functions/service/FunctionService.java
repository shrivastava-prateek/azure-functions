package com.debugchaos.azure_functions.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.debugchaos.azure_functions.entity.AppConfig;
import com.debugchaos.azure_functions.entity.UserConfig;
import com.debugchaos.azure_functions.exception.AuthServiceCallFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;

public class FunctionService {

	//private static final String BASIC_SCHEME_TEMPLATE = "Basic %s";
	private static final String BEARER_SCHEME_TEMPLATE = "Bearer %s";
	private static final String USER_AGENT = "Apache HTTP4 Client";
	
	private static final String BASE_URI = "https://api.twitter.com/2/users/";
	private static final String USER_TWEETS = "/tweets";
	private static final String TWEET_RETWEET = "/retweets";
	private static final String LIKE_TWEET = "/likes";
	private static final String AUTH_URI = "https://api.twitter.com/2/oauth2/token";

	private Random r = new Random();
	private Set<String> lastRecentTweetIds = new HashSet<>();
	private OutputBinding<UserConfig> cosmosDBOutput = null;
	private Map<String, UserConfig> userConfigMap = null;
	private AppConfig appConfig = null;
	private static FunctionService funcService = new FunctionService();
	private ExecutionContext context;
	
	
	private FunctionService() {
		
	}
	
	public static FunctionService getInstance() {
		if(funcService == null) {
			funcService = new FunctionService();
			return funcService;
		}
		return funcService;
	}

	public void retweetOrLikeRecentTweets(String ofUserId) {

		Set<String> recentTweetIds = getRecentTweetsByUserId(ofUserId);
		
			int size = userConfigMap.size();
			int randomNo = r.nextInt(size);
			
			UserConfig[] userConfigs = userConfigMap.values().toArray(new UserConfig[0]);
		
			context.getLogger().info("Random No: "+randomNo +" User Configs length: "+ userConfigs.length);
			UserConfig userConfig = userConfigs[randomNo];
			
			try {
					//context.getLogger().info("Even: Calling like and retweet methods for userId: " + userConfig.getId());
					//retweet(userConfig.getId(), recentTweetIds);
					//likeTweet(userConfig.getId(), recentTweetIds);
		
					context.getLogger().info("Odd: Calling just the like method for userId: " + userConfig.getId());
					likeTweet(userConfig.getId(), recentTweetIds);

			}catch(RuntimeException e) {
				e.printStackTrace();
			}
	}

	public Set<String> getRecentTweetsByUserId(String userId) {

		Set<String> recentTweetIds = null;

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			CloseableHttpResponse response;
			String result = "";
			URI uri = new URIBuilder(BASE_URI + userId + USER_TWEETS).build();
			HttpGet request = new HttpGet(uri.toASCIIString());
			request.addHeader(HttpHeaders.AUTHORIZATION, String.format(BEARER_SCHEME_TEMPLATE, appConfig.getBearerToken()));
			request.addHeader(HttpHeaders.ACCEPT, "*/*");
			request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);

			recentTweetIds = parseRecentTweets(result);

		} catch (IOException | URISyntaxException e2) {
			e2.printStackTrace();
		}
		return recentTweetIds;
	}

	public Set<String> parseRecentTweets(String result) {
		Set<String> recentTweetIds = null;
		List<Map<String, String>> recentTweets = null;

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> responseMap = new HashMap<>();
		try {
			responseMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		Object responseSet = responseMap.get("data");
		recentTweets = objectMapper.convertValue(responseSet, new TypeReference<List<Map<String, String>>>() {
		});

		recentTweetIds = recentTweets.stream()
				.filter(element -> !(element.get("text").startsWith("@")))
				.map(element -> element.get("id"))
				.collect(Collectors.toSet());
		
		context.getLogger().info("Recent Tweets" + recentTweetIds);
		
		Set<String> filteredTweetIds = recentTweetIds.stream()
				.filter(recentTweetId -> !lastRecentTweetIds.contains(recentTweetId))
				.collect(Collectors.toSet());
		
		context.getLogger().info("Filtered Tweets: "+ filteredTweetIds);
		
		lastRecentTweetIds = recentTweetIds;
		
		return filteredTweetIds;

	}

	public void retweet(String fromUserId, Set<String> tweetIds) {

		UserConfig config = userConfigMap.get(fromUserId);

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			URI uri = new URIBuilder(BASE_URI + fromUserId + TWEET_RETWEET).build();
			HttpPost request = new HttpPost(uri.toASCIIString());
			request.addHeader(HttpHeaders.ACCEPT, "*/*");
			request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);
			request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

			tweetIds.forEach(tweetId -> {

				request.setHeader(HttpHeaders.AUTHORIZATION, String.format(BEARER_SCHEME_TEMPLATE, config.getBearerToken()));

				CloseableHttpResponse response;
				try {
					String json = "{\"tweet_id\":\"" + tweetId + "\"}";
					StringEntity entity = new StringEntity(json);
					request.setEntity(entity);

					// printHttpRequest(request);

					response = httpClient.execute(request);
					int status = response.getStatusLine().getStatusCode();

					HttpEntity respEntity = null;
					if (status >= 200 && status < 300) {
						respEntity = response.getEntity();
					} else if (status == 401) {
						try {
							fetchNewTokensUsingRefreshToken(fromUserId);
						} catch (AuthServiceCallFailedException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
						retweet(fromUserId, new HashSet<>(Arrays.asList(tweetId)));
						return;
					} else {
						context.getLogger().info("Something went wrong");
					}

					String result = EntityUtils.toString(respEntity);
					context.getLogger().info("Retweet: userId: " + fromUserId + " response: " + result);

				} catch (IOException | AuthenticationException | URISyntaxException e) {
					e.printStackTrace();
				}
			});

		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}

	}

	public void likeTweet(String fromUserId, Set<String> tweetIds) {

		UserConfig config = userConfigMap.get(fromUserId);

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			URI uri = new URIBuilder(BASE_URI + fromUserId + LIKE_TWEET).build();
			HttpPost request = new HttpPost(uri.toASCIIString());
			request.addHeader(HttpHeaders.AUTHORIZATION, String.format(BEARER_SCHEME_TEMPLATE, config.getBearerToken()));
			request.addHeader(HttpHeaders.ACCEPT, "*/*");
			request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);
			request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

			tweetIds.forEach(tweetId -> {
				CloseableHttpResponse response;
				try {
					String json = "{\"tweet_id\":\"" + tweetId + "\"}";
					StringEntity entity = new StringEntity(json);
					request.setEntity(entity);

					response = httpClient.execute(request);

					int status = response.getStatusLine().getStatusCode();

					HttpEntity respEntity = null;
					if (status >= 200 && status < 300) {
						respEntity = response.getEntity();
					} else if (status == 401) {
						try {
							fetchNewTokensUsingRefreshToken(fromUserId);
						} catch (AuthServiceCallFailedException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
						likeTweet(fromUserId, new HashSet<>(Arrays.asList(tweetId)));
						return;
					} else {
						context.getLogger().info("Something went wrong");
					}

					String result = EntityUtils.toString(respEntity);
					context.getLogger().info("likeTweet: userId: " + fromUserId + " response: " + result);
				} catch (IOException | AuthenticationException | URISyntaxException e) {
					e.printStackTrace();
				}
			});

		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}

	}

	private void fetchNewTokensUsingRefreshToken(String userId) throws AuthenticationException, URISyntaxException, IOException, AuthServiceCallFailedException {

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			URI uri = new URIBuilder(AUTH_URI).build();
			HttpPost request = new HttpPost(uri.toASCIIString());

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(appConfig.getClientId(), appConfig.getClientSecret());
			request.addHeader(new BasicScheme().authenticate(creds, request, null));

			request.addHeader(HttpHeaders.ACCEPT, "*/*");
			request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);
			request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

			List<NameValuePair> bodyParams = new ArrayList<>();
			bodyParams.add(new BasicNameValuePair("refresh_token", userConfigMap.get(userId).getRefreshToken()));
			bodyParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
			bodyParams.add(new BasicNameValuePair("client_id", appConfig.getClientId()));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyParams, Consts.UTF_8);
			request.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(request);
			
			int status = response.getStatusLine().getStatusCode();
			
			HttpEntity respEntity = response.getEntity();
			String result = EntityUtils.toString(respEntity);
			context.getLogger().info("fetchNewTokensUsingRefreshToken: response: " + result);
			
			if(status>=200 && status<300) {
				updateNewAccessTokenAndRefreshToken(userId, result);	
			}
			else {
				throw new AuthServiceCallFailedException("Can't get the new access token");
			}
			
		}

	}

	private void updateNewAccessTokenAndRefreshToken(String userId, String result) {

		UserConfig config = userConfigMap.get(userId);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> responseMap = new HashMap<>();
		try {
			responseMap = objectMapper.readValue(result, new TypeReference<Map<String, String>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		config.setBearerToken(responseMap.get("access_token"));
		config.setRefreshToken(responseMap.get("refresh_token"));
		
		updateUserConfig(config);

	}
	
	public void updateUserConfig(UserConfig config) {
		
		cosmosDBOutput.setValue(config);
		
	}

	public OutputBinding<UserConfig> getCosmosDBOutput() {
		return cosmosDBOutput;
	}

	public void setCosmosDBOutput(OutputBinding<UserConfig> cosmosDBOutput) {
		this.cosmosDBOutput = cosmosDBOutput;
	}


	public Map<String, UserConfig> getUserConfigMap() {
		return userConfigMap;
	}

	public void setUserConfigMap(Map<String, UserConfig> userConfigMap) {
		this.userConfigMap = userConfigMap;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public ExecutionContext getContext() {
		return context;
	}

	public void setContext(ExecutionContext context) {
		this.context = context;
	}
	
	
	
}

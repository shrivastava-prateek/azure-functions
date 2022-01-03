package com.debugchaos.azure_functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.debugchaos.azure_functions.entity.AppConfig;
import com.debugchaos.azure_functions.entity.UserConfig;
import com.debugchaos.azure_functions.service.FunctionService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.TimerTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
	/**
	 * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
	 * using "curl" command in bash: 1. curl -d "HTTP Body" {your
	 * host}/api/HttpExample 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
	 */
	

	private FunctionService funcService = FunctionService.getInstance();
	private static String SOURCE_USER_ID = System.getenv("SOURCE_USER_ID");	
	
	{
		System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
	}
	
	@FunctionName("HTTPTriggerTwitterLikeRT")
	public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) 
			HttpRequestMessage<Optional<String>> request,
			@CosmosDBOutput(name = "databaseOutput", databaseName = "twitter-utilities", 
			  collectionName = "twitter-utilities-container", 
			  connectionStringSetting = "cosmosDBConnectionString") 
			  OutputBinding<UserConfig> output,
			 @CosmosDBInput(name = "databaseInputAppConfig",
              databaseName = "twitter-utilities",
              collectionName = "twitter-utilities-container",
              sqlQuery = "select * from Items r where r.id = 'debugchaos_apicheck'",
              connectionStringSetting = "cosmosDBConnectionString")
            AppConfig[] appConfig,
			@CosmosDBInput(name = "databaseInputUserConfig",
            databaseName = "twitter-utilities",
            sqlQuery = "select * from Items r where r.id <> 'debugchaos_apicheck'",
            collectionName = "twitter-utilities-container",
            connectionStringSetting = "cosmosDBConnectionString")
          	UserConfig[] userConfigs,
			final ExecutionContext context) {
		context.getLogger().info("Java HTTP trigger processed a request.");

		AppConfig applicationConfig = appConfig[0];
		context.getLogger().info("App Config : "+ applicationConfig);
		
		Map<String, UserConfig> userConfigMap = new HashMap<>();
		for(UserConfig userConfig: userConfigs) {
			context.getLogger().info("User Config " + userConfig);
			userConfigMap.put(userConfig.getId(), userConfig);
		}
		
		
		
		funcService.setAppConfig(applicationConfig);
		funcService.setCosmosDBOutput(output);
		funcService.setUserConfigMap(userConfigMap);
		funcService.setContext(context);
		
		
		try {
			funcService.retweetOrLikeRecentTweets(SOURCE_USER_ID);
			return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
		}catch(Exception e) {
			return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong")
					.build();
		}
	}

	@FunctionName("TimerTriggerTwitterLikeRT")
	public void timerTriggerTwitterLikeRT(
			@TimerTrigger(name = "TimerTriggerTwitterLikeRT", schedule = "0 */30 * * * *") 
			String timerInfo,
			@CosmosDBOutput(name = "databaseOutput", databaseName = "twitter-utilities", 
			  collectionName = "twitter-utilities-container", 
			  connectionStringSetting = "cosmosDBConnectionString") 
			  OutputBinding<UserConfig> output,
			@CosmosDBInput(name = "databaseInputAppConfig",
              databaseName = "twitter-utilities",
              collectionName = "twitter-utilities-container",
              sqlQuery = "select * from Items r where r.id = 'debugchaos_apicheck'",
              connectionStringSetting = "cosmosDBConnectionString")
            AppConfig[] appConfig,
			@CosmosDBInput(name = "databaseInputUserConfig",
              databaseName = "twitter-utilities",
            sqlQuery = "select * from Items r where r.id <> 'debugchaos_apicheck'",
              collectionName = "twitter-utilities-container",
              connectionStringSetting = "cosmosDBConnectionString")
            	UserConfig[] userConfigs,
			final ExecutionContext context) {

		context.getLogger().info("Timer is triggered " + timerInfo);
		
		AppConfig applicationConfig = appConfig[0];
		context.getLogger().info("App Config : "+ applicationConfig);
		Map<String, UserConfig> userConfigMap = new HashMap<>();
		for(UserConfig userConfig: userConfigs) {
			context.getLogger().info("User Config " + userConfig);
			userConfigMap.put(userConfig.getId(), userConfig);
		}
		
		funcService.setAppConfig(applicationConfig);
		funcService.setCosmosDBOutput(output);
		funcService.setUserConfigMap(userConfigMap);
		funcService.setContext(context);
		
		funcService.retweetOrLikeRecentTweets(SOURCE_USER_ID);
		
	}


}

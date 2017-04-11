package co.nontster.perftest;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;

public class EsUtils {
	
	private static final Logger LOGGER = LogManager.getLogger(InspectBee.class.getName());
	
	private static RestClient client;
	
	
	private static void setRequestRef(HarLog harLog, String source, String pageRef) {
		
		String uid = UUID.randomUUID().toString();
		
		for(HarEntry entry : harLog.getEntries()){
			entry.setPageref(source);
			entry.setComment(uid);
			entry.getRequest().setComment(pageRef);
		}
	}

	private static synchronized RestClient getRestClient(String username, String password){		
		if (client == null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

			client = RestClient.builder(new HttpHost("localhost", 9200, "http"))
					.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
						@Override
						public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
							return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
						}
					}).build();
		}
		
		return client;
	}
	
	static String buildBulkIdx(HarLog harLog, String idx, String type, String source, String pageRef) throws JsonProcessingException{
		
		StringBuffer bulkIdxBuffer = new StringBuffer();			
		ObjectMapper mapper = new ObjectMapper();
		
		String bulkIsxHeader = "{\"index\":{\"_index\":\""+idx+"\",\"_type\":\""+ type +"\"}}";
		
		// Set request reference 
		setRequestRef(harLog, source, pageRef);
				
		for(HarEntry entry : harLog.getEntries()){			
			String entryString = mapper.writeValueAsString(entry);			
			bulkIdxBuffer.append(bulkIsxHeader).append(System.lineSeparator()).append(entryString).append(System.lineSeparator());
		}
				
		return bulkIdxBuffer.toString();		
	}
	
	static void bulkStore(String esUsername, String esPassword, String jsonString) throws IOException{
		RestClient restClient = getRestClient(esUsername, esPassword);

		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		
		Response indexResponse = restClient.performRequest(
		        "POST",
		        "/_bulk/",
		        Collections.<String, String>emptyMap(),
		        entity);
		
		LOGGER.debug(EntityUtils.toString(indexResponse.getEntity()));

		restClient.close();
	}
	
	static void store(String esUsername, String esPassword, String indexString, String jsonString) throws IOException {
		
		RestClient restClient = getRestClient(esUsername, esPassword);

		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		
		Response indexResponse = restClient.performRequest(
		        "POST",
		        "/"+indexString+"/logs/",
		        Collections.<String, String>emptyMap(),
		        entity);
		
		LOGGER.debug(EntityUtils.toString(indexResponse.getEntity()));

		restClient.close();
	}
}

package com.everdata.resource;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestResponse;

import com.everdata.command.CommandException;
import com.everdata.server.ESUtils;
import com.everdata.server.RequestMap;
import com.everdata.server.SearchService;

@Path("/users/{userid}/taskid/{taskid}/_COMMAND/{command}")
public class AppResource {
	@PathParam("command")
	 private String command;
	@GET
	@POST
	@Path("/_COMMAND/{command}")
	@Produces({"application/xml", "application/json"})
	public RestResponse search(){
		Client client=ESUtils.getEsClient();
		RequestMap request=new RequestMap();
		request.put("q", command);
		SearchService searchService=new SearchService();
		try {
			return searchService.handleRequest(request,ESUtils.getEsClient());
		} catch (IOException e) {
				return null;
		} catch (CommandException e) {
			return null;
		}
	}
	public static void main(String[] args) {
		Client client=ESUtils.getEsClient();
		String command="search index=log sourcetype=*  ";
		RequestMap request=new RequestMap();
		request.put("q", command);
		SearchService searchService=new SearchService();
		try {
			 System.out.println(searchService.handleRequest(request,ESUtils.getEsClient()).toString());
		} catch (IOException e) {
				
		} catch (CommandException e) {
		
		}
		
	}
}

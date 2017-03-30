package com.everdata.server;

import java.io.IOException;
import java.io.OutputStream;

import com.everdata.command.Search;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.BytesRestResponse;

import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;

import com.everdata.command.CommandException;
import com.everdata.command.ReportResponse;
import com.everdata.command.Search.QueryResponse;
import com.everdata.parser.AST_Start;
import com.everdata.parser.CommandParser;
import com.everdata.parser.ParseException;
import com.everdata.xcontent.CsvXContent;

public class SearchService {
	private static ESLogger logger=Loggers.getLogger(SearchService.class);
	public RestResponse handleRequest(RequestMap request,Client client) throws IOException, CommandException {
		
		String	command = request.get("q","");
	
		if (command.length() == 0) {
			logger.info("命令为空", new CommandException("命令为空"));
			return null;
		} else {
			if (!command.startsWith(Search.PREFIX_SEARCH_STRING))
				command = Search.PREFIX_SEARCH_STRING + " " + command;
		}
		
		logger.info(command);

		final int from = request.paramAsInt("from", 0);
		final int size = request.paramAsInt("size", 10);
		final String format = request.get("format", "json");
		final boolean download = request.paramAsBoolean("download", false);
		final boolean download2 = request.paramAsBoolean("download2", false);
		final boolean showMeta = request.paramAsBoolean("showMeta", true);
	
		XContent xContent = XContentType.JSON.xContent();
		XContentBuilder builder=XContentBuilder.builder(xContent);

		if (format.equalsIgnoreCase("csv"))
			xContent = CsvXContent.csvXContent;

		final Search search;

		try {

			CommandParser parser = new CommandParser(command);

			AST_Start.dumpWithLogger(logger, parser.getInnerTree(), "");

			search = new Search(parser, client, logger);

		} catch (CommandException e2) {
			
			logger.info("抛出 CommandException异常");
			return null;
		} catch (ParseException e1) {
			logger.info("抛出ParseException 异常");
			return null;
		} catch (IOException e) {
			logger.info("抛出 IOException异常");
			return null;
		}
			
		//执行delete查询
		if (request.paramAsBoolean("delete", false)) {
		/*	DeleteByQueryResponse result=search.executeDelete();
			search.buildDelete(builder, result);
			return new BytesRestResponse(result.status(),builder);*/
			
		}
			//如果参数中query为true
		if (request.paramAsBoolean("query", true)) {
			if (download || download2) {
				search.executeDownload(new OutputStream() {
					byte[] innerBuffer = new byte[1200];
					int idx = 0;

					@Override
					public void write(int b) throws IOException {
						innerBuffer[idx++] = (byte) b;
						if (idx == innerBuffer.length) {
							/*
							channel.sendContinuousBytes(innerBuffer, 0, idx,
									false);
							*/
							idx = 0;
						}
					}

					@Override
					public void close() throws IOException {
					/*	if (idx > 0)
							channel.sendContinuousBytes(innerBuffer, 0, idx, true);
						else
							channel.sendContinuousBytes(null, 0, 0, true);*/
						idx=0;
					}

				}, xContent, download2);
				
			} else if (search.joinSearchs.size() > 0) {

				 QueryResponse result=search.executeQuery(from, size, new String[0]);
				 Search.buildQuery(from, builder, result, logger, search.tableFieldNames, showMeta);
				 return new BytesRestResponse(RestStatus.OK, builder);
			} else {
				SearchResponse result=search.executeQueryWithNonJoin(from, size, new String[0]);
				Search.buildQuery(from, builder, result, logger, search.tableFieldNames, showMeta);
				return new BytesRestResponse(RestStatus.OK,builder);
			}
			//如果参数中query为false
		} else {
			final ReportResponse result = new ReportResponse();
			result.bucketFields = search.bucketFields;
			result.statsFields = search.statsFields;
            result.response=search.executeReport(from, size);
            Search.buildReport(from, size, builder, result, logger);
            return new BytesRestResponse(result.response.status(), builder);
		}
		return null;

	}


}

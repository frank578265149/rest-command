package org.elasticsearch.plugin.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import com.everdata.command.Search;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.action.support.RestBuilderListener;
import org.elasticsearch.rest.support.RestUtils;

import com.everdata.command.CommandException;
import com.everdata.command.ReportResponse;
import com.everdata.command.Search.QueryResponse;
import com.everdata.parser.AST_Start;
import com.everdata.parser.CommandParser;
import com.everdata.parser.ParseException;
import com.everdata.xcontent.CsvXContent;

public class CommandRestHandler extends BaseRestHandler {
	@Inject
	public CommandRestHandler(Settings settings, Client client,
			RestController controller) {
		super(settings, controller, client);
		controller.registerHandler(GET, "/_command", this);
		controller.registerHandler(POST, "/_command", this);
	}

	@Override
	public void handleRequest(RestRequest request, final RestChannel channel, Client client) {
		
		String command = "";
		
		if (request.method() == RestRequest.Method.GET)
			command = request.param("q", "");
		else {
			HashMap<String, String> post = new HashMap<String, String>();
			RestUtils.decodeQueryString(request.content().toUtf8(), 0, post);
			if (post.containsKey("q")) {
				command = post.get("q");
			}
		}

		if (command.length() == 0) {
			SendFailure(request, channel, new CommandException("命令为空"));
			return;
		} else {
			if (!command.startsWith(Search.PREFIX_SEARCH_STRING))
				command = Search.PREFIX_SEARCH_STRING + " " + command;
		}
		logger.info(command);

		final int from = request.paramAsInt("from", 0);
		final int size = request.paramAsInt("size", 10);
		final String format = request.param("format", "json");
		final boolean download = request.paramAsBoolean("download", false);
		final boolean download2 = request.paramAsBoolean("download2", false);
		final boolean showMeta = request.paramAsBoolean("showMeta", true);

		XContent xContent = XContentType.JSON.xContent();

		if (format.equalsIgnoreCase("csv"))
			xContent = CsvXContent.csvXContent;

		final Search search;

		try {

			CommandParser parser = new CommandParser(command);

			AST_Start.dumpWithLogger(logger, parser.getInnerTree(), "");

			search = new Search(parser, client, logger);

		} catch (CommandException e2) {
			SendFailure(request, channel, e2);
			return;
		} catch (ParseException e1) {
			SendFailure(request, channel, e1);
			return;
		} catch (IOException e) {
			SendFailure(request, channel, e);
			return;
		}

		if (request.paramAsBoolean("delete", false)) {

			search.executeDelete(new RestBuilderListener<DeleteByQueryResponse>(
					channel) {
				@Override
				public RestResponse buildResponse(DeleteByQueryResponse result,
						XContentBuilder builder) throws Exception {
					search.buildDelete(builder, result);
					return new BytesRestResponse(result.status(), builder);
				}
			});
			return;
		}

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
						/*
						if (idx > 0)
							channel.sendContinuousBytes(innerBuffer, 0, idx, true);
						else
							channel.sendContinuousBytes(null, 0, 0, true);
							*/
					}

				}, xContent, download2);

			} else if (search.joinSearchs.size() > 0) {

				search.executeQuery(new RestBuilderListener<QueryResponse>(
						channel) {
					@Override
					public RestResponse buildResponse(QueryResponse result,
							XContentBuilder builder) throws Exception {
						Search.buildQuery(from, builder, result, logger, search.tableFieldNames, showMeta);
						return new BytesRestResponse(RestStatus.OK, builder);
					}
				}, from, size, new String[0]);
			} else {
				search.executeQueryWithNonJoin(
						new RestBuilderListener<SearchResponse>(channel) {
							@Override
							public RestResponse buildResponse(
									SearchResponse result,
									XContentBuilder builder) throws Exception {
								Search.buildQuery(from, builder, result, logger, search.tableFieldNames, showMeta);
								return new BytesRestResponse(RestStatus.OK,
										builder);
							}
						}, from, size, new String[0]);
			}
		} else {
			final ReportResponse result = new ReportResponse();
			result.bucketFields = search.bucketFields;
			result.statsFields = search.statsFields;

			search.executeReport(new RestBuilderListener<SearchResponse>(
					channel) {
				@Override
				public RestResponse buildResponse(SearchResponse response,
						XContentBuilder builder) throws Exception {
					result.response = response;
					Search.buildReport(from, size, builder, result, logger);
					return new BytesRestResponse(response.status(), builder);
				}

			}, from, size);
		}

	}

	public void SendFailure(RestRequest request, RestChannel channel,
			Throwable e) {
		try {
			channel.sendResponse(new BytesRestResponse(channel, e));
		} catch (IOException e1) {
			logger.error("Failed to send failure response", e1);
		}
	}

}

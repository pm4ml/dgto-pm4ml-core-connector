package com.modusbox.client.router;

import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.model.dataformat.JsonLibrary;


public class PartiesRouter extends RouteBuilder {

	private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

	private static final String TIMER_NAME = "histogram_get_parties_timer";

	public static final Counter reqCounter = Counter.build()
			.name("counter_get_parties_requests_total")
			.help("Total requests for GET /parties.")
			.register();

	private static final Histogram reqLatency = Histogram.build()
			.name("histogram_get_parties_request_latency")
			.help("Request latency in seconds for GET /parties.")
			.register();

	public void configure() {

		// Add our global exception handling strategy
		exceptionHandlingConfigurer.configureExceptionHandling(this);

		// In this case the GET parties will return the loan account with client details
		from("direct:getPartiesByIdTypeIdValue").routeId("com.modusbox.getPartiesByIdTypeIdValue").doTry()
				.process(exchange -> {
					reqCounter.inc(1); // increment Prometheus Counter metric
					exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
				})
				.to("bean:customJsonMessage?method=logJsonMessage(" +
						"'info', " +
						"${header.X-CorrelationId}, " +
						"'Request received at GET /parties/${header.idType}/${header.idValue}', " +
						"'Tracking the request', " +
						"'Call the DGTO API,  Track the response', " +
						"'fspiop-source: ${header.fspiop-source} Input Payload: ${body}')") // default logger
				/*
				 * BEGIN processing
				 */
				.marshal().json()
				.transform((datasonnet("resource:classpath:mappings/postCollectRequest.ds")))
				.setBody(simple("${body.content}"))
				.marshal().json(JsonLibrary.Gson)

				.removeHeaders("CamelHttp*")
				.removeHeader(Exchange.HTTP_URI)
				.setHeader("Content-Type", constant("application/json"))
				.setHeader("Accept", constant("application/json"))
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.to("bean:customJsonMessage?method=logJsonMessage(" +
						"'info', " +
						"${header.X-CorrelationId}, " +
						"'Calling DGTO backend API, collect, LOOKUP action', " +
						"'Tracking the request', " +
						"'Track the response', " +
						"'Request sent to, POST http://dgtof-saccos.co.tz/api/payments/collect Payload: ${body}')")
				.toD("http://dgtof-saccos.co.tz/api/payments/collect")
				.unmarshal().json(JsonLibrary.Gson)
				.to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
						"'Response from DGTO backend API, collect, LOOKUP action,, getParties: ${body}', " +
						"'Tracking the response', 'Verify the response', null)")
				.marshal().json()
				.transform(datasonnet("resource:classpath:mappings/getPartiesResponse.ds"))
				.setBody(simple("${body.content}"))
				/*
				 * END processing
				 */
				.to("bean:customJsonMessage?method=logJsonMessage(" +
						"'info', " +
						"${header.X-CorrelationId}, " +
						"'Response for GET /parties/${header.idType}/${header.idValue}', " +
						"'Tracking the response', " +
						"null, " +
						"'Output Payload: ${body}')") // default logger
				.doFinally().process(exchange -> {
					((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
				}).end()
		;
	}
}
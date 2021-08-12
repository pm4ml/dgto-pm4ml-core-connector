package com.modusbox.client.jaxrs;

import com.modusbox.client.api.QuoterequestsApi;
import com.modusbox.client.model.QuoteRequest;
import com.modusbox.client.model.QuoteResponse;

import javax.validation.Valid;

public class QuotesApiImpl implements QuoterequestsApi {

    @Override
    public QuoteResponse postQuoteRequests(@Valid QuoteRequest quoteRequest) {
        return null;
    }
}

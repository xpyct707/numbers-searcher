package com.xpyct707.numbers_searcher.web;

import com.xpyct707.numbers_searcher.web_service.FindNumberRequest;
import com.xpyct707.numbers_searcher.web_service.FindNumberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class FindNumberEndpoint {
    private static final String NAMESPACE_URI = "http://xpyct707.com/numbers-searcher/web-service";

    private FilesRepository filesRepository;

    @Autowired
    public FindNumberEndpoint(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "findNumberRequest")
    @ResponsePayload
    public FindNumberResponse findNumber(@RequestPayload FindNumberRequest request) {
        FindNumberResponse response = new FindNumberResponse();
        response.setResult(filesRepository.findNumber(request.getNumber()));
        return response;
    }
}

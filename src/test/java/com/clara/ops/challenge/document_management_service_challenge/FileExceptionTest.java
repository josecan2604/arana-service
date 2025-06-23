package com.clara.ops.challenge.document_management_service_challenge;


import static org.assertj.core.api.Assertions.assertThat;

import com.clara.ops.challenge.document_management_service_challenge.dtos.ApiResponseWrapper;
import com.clara.ops.challenge.document_management_service_challenge.exception.ExceptionHandlerUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

class FileExceptionTest {

    private ExceptionHandlerUtility handler;

    @BeforeEach
    void setUp() {
        handler = new ExceptionHandlerUtility();
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerError() {
        Exception exception = new Exception("Generic error");
        HttpServletRequest request = new MockHttpServletRequest();

        var response = handler.handleAllExceptions(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApiResponseWrapper<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Generic error");
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getResponse()).isNull();
    }

    @Test
    void handleMissingPart_shouldReturnBadRequest() {
        MissingServletRequestPartException exception = new MissingServletRequestPartException("file");

        var response = handler.handleMissingPart(exception);

        assertThat(response).isNotNull();
        var wrapper = (ApiResponseWrapper<?>) ((ResponseEntity<?>) response).getBody();
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(wrapper.getMessage()).contains("file");
        assertThat(wrapper.getStatus()).isEqualTo(400);
        assertThat(wrapper.getResponse()).isNull();
    }

    @Test
    void handleMissingParams_shouldReturnBadRequest() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("user", "String");

        var response = handler.handleMissingParams(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApiResponseWrapper<?> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("user");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getResponse()).isNull();
    }
}

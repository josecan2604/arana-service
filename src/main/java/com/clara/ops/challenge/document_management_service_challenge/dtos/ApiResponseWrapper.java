package com.clara.ops.challenge.document_management_service_challenge.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ApiResponseWrapper<T> {
  private T response;
  private String message;
  private int status;

  public ApiResponseWrapper(T response, String message, int status) {
    this.response = response;
    this.message = message;
    this.status = status;
  }

  @Override
  public String toString() {
    return "ApiResponseWrapper{"
        + "response="
        + response
        + ", message='"
        + message
        + '\''
        + ", status="
        + status
        + '}';
  }
}

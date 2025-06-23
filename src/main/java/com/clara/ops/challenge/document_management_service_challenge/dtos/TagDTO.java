package com.clara.ops.challenge.document_management_service_challenge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
  private Long id;
  private String name;
}

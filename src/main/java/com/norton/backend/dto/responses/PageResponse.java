package com.norton.backend.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

  private List<T> content;

  @JsonProperty("page_number")
  private int page;

  @JsonProperty("page_size")
  private int size;

  @JsonProperty("total_elements")
  private long totalElements;

  @JsonProperty("total_pages")
  private int totalPages;

  private boolean first;
  private boolean last;
  private boolean empty;
}

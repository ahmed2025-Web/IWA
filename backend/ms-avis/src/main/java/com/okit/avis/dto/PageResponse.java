package com.okit.avis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    @JsonProperty("content")
    List<T> content,

    @JsonProperty("page")
    int page,

    @JsonProperty("size")
    int size,

    @JsonProperty("totalElements")
    @JsonAlias("total_elements")
    long totalElements,

    @JsonProperty("totalPages")
    @JsonAlias("total_pages")
    int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}

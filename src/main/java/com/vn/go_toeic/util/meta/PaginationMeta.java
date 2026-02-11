package com.vn.go_toeic.util.meta;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PaginationMeta {
    private final int currentPage;
    private final int totalPages;
    private final long totalItems;
    private final int pageSize;
    private final boolean hasPrevious;
    private final boolean hasNext;
    private final String requestParams;

    public PaginationMeta(Page<?> page, String requestParams) {
        this.currentPage = page.getNumber() + 1;
        this.totalPages = page.getTotalPages();
        this.totalItems = page.getTotalElements();
        this.pageSize = page.getSize();
        this.hasPrevious = page.hasPrevious();
        this.hasNext = page.hasNext();
        this.requestParams = requestParams;
    }
}

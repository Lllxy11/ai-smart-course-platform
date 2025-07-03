package com.aicourse.dto;

import java.util.List;

/**
 * 分页响应DTO
 */
public class PagedResponse<T> {

    private List<T> content;
    private Long total;
    private Long totalElements;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public PagedResponse() {}

    public PagedResponse(List<T> content, Long total, Integer page, Integer pageSize) {
        this.content = content;
        this.total = total;
        this.totalElements = total; // 为了兼容前端，同时提供totalElements字段
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = total > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }

    // Builder pattern
    public static <T> PagedResponse<T> of(List<T> content, Long total, Integer page, Integer pageSize) {
        return new PagedResponse<>(content, total, page, pageSize);
    }

    // Getters and Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    // 为了向后兼容，保留items字段
    public List<T> getItems() {
        return content;
    }

    public void setItems(List<T> items) {
        this.content = items;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        this.totalElements = total;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
        this.total = totalElements;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PagedResponse{" +
                "total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", contentCount=" + (content != null ? content.size() : 0) +
                '}';
    }
} 
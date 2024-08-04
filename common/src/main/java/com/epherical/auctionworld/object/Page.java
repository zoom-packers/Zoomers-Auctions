package com.epherical.auctionworld.object;

public class Page {

    private int page;
    private int pageSize;


    public Page(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageOffset() {
        return (page - 1) * pageSize;
    }

    public int getPagedItems() {
        return (page* pageSize);
    }

    public int getMaxPages(int divideBy) {
        return (int) Math.ceil((double) pageSize / divideBy); //silly
    }


}

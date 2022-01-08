package com.game.controller;

public interface PlayerPageRequest {
    int DEFAULT_PAGE_NUMBER = 0;
    int DEFAULT_PAGE_SIZE = 3;
    String DEFAULT_SORT_BY = PlayerOrder.ID.getFieldName();

}

package com.xpyct707.numbers_searcher.repository;

import com.xpyct707.numbers_searcher.model.RequestHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RequestHistoryRepository extends CrudRepository<RequestHistory, UUID> {
    //No custom methods so far
}

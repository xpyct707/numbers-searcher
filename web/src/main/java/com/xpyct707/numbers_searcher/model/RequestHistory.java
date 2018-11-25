package com.xpyct707.numbers_searcher.model;

import com.xpyct707.numbers_searcher.web_service.Code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "request_history")
public class RequestHistory {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Code code;

    private int number;

    @Column(name = "filenames")
    private String fileNames;

    private String error;

    protected RequestHistory() {
        //Empty
    }

    public RequestHistory(Code code, int number, String fileNames, String error) {
        this.code = code;
        this.number = number;
        this.fileNames = fileNames;
        this.error = error;
    }
}

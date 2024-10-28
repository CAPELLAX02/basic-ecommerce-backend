package com.capellax.ecommerce.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DataChange<T> {

    private ChangeType changeType;
    private T data;

    public enum ChangeType {
        INSERT,
        UPDATE,
        DELETE
    }

}

package com.example.api.converter;

import java.util.List;

public abstract class BaseConverter<E, D> {
    public abstract E toEntity(D dto);

    public abstract D toDto(E e);

    public List<D> toDto(List<E> entities){
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public List<E> toEntity(List<D> dtos){
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}

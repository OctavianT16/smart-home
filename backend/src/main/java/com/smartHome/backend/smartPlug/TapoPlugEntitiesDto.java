package com.smartHome.backend.smartPlug;

public record TapoPlugEntitiesDto(
        HaEntityStateDto switchEntity,
        HaEntityStateDto powerEntity,
        HaEntityStateDto energyEntity,
        HaEntityStateDto voltageEntity,
        HaEntityStateDto currentEntity
) {
}
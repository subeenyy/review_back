package org.example.campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignMapper {
    @Mapping(target = "campaignId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateFromDto(CampaignResponseDto dto, @MappingTarget Campaign entity);

    default String map(List<String> value) {
        if (value == null) return null;
        return String.join(",", value); // DB에는 콤마로 저장
    }
}

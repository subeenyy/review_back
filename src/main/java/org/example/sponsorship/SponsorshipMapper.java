package org.example.sponsorship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SponsorshipMapper {
    @Mapping(target = "id", ignore = true)  // id는 업데이트 안함
    void updateFromDto(SponsorshipResponseDto dto, @MappingTarget Sponsorship entity);

    default String map(List<String> value) {
        if (value == null) return null;
        return String.join(",", value); // DB에는 콤마로 저장
    }
}

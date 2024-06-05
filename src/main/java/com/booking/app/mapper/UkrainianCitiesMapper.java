package com.booking.app.mapper;

import com.booking.app.dto.CityDto;
import com.booking.app.entity.UkrainianCities;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UkrainianCitiesMapper {

    @Mapping(target = "cityUa", source = "nameUa", ignore = true)
    @Mapping(target = "cityEng", source = "nameEng", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "country", source = "country")
    CityDto toCitiesDTO(UkrainianCities ukrainianCities);

    default List<CityDto> toCitiesDtoList(List<UkrainianCities> ukrainianCitiesList, String inputLanguage, String siteLanguage) {
        return ukrainianCitiesList.stream()
                .map(place -> {
                    CityDto cityDTO = toCitiesDTO(place);
                    cityDTO.setLanguage(siteLanguage);

                    switch (siteLanguage) {
                        case "ua":
                            switch (inputLanguage) {
                                case "ua":
                                    cityDTO.setCityUa(place.getNameUa());
                                    break;
                                case "eng":
                                    cityDTO.setCityEng(place.getNameEng());
                                    cityDTO.setCityUa(place.getNameUa());
                                    break;
                            }
                            break;

                        case "eng":
                            switch (inputLanguage) {
                                case "eng":
                                    cityDTO.setCityEng(place.getNameEng());
                                    break;
                                case "ua":
                                    cityDTO.setCityEng(place.getNameEng());
                                    cityDTO.setCityUa(place.getNameUa());
                                    break;
                            }
                            break;
                    }

                    return cityDTO;
                })
                .toList();
    }

}

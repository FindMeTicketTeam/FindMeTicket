package com.booking.app.mappers;

import com.booking.app.dto.users.BasicLoginDto;
import com.booking.app.dto.users.EmailDto;
import com.booking.app.dto.users.RegistrationDTO;
import com.booking.app.dto.users.UserDTO;
import com.booking.app.entities.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDTO toUserDto(User user);

    User toUser(UserDTO userDTO);

    BasicLoginDto toLoginDto(User user);

    User toUser(BasicLoginDto userDTO);

    RegistrationDTO toRegistrationDto(User user);

    User toUser(RegistrationDTO registrationDTO);

    EmailDto toEmailDto(User user);

}
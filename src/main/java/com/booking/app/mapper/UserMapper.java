package com.booking.app.mapper;

import com.booking.app.dto.EmailDto;
import com.booking.app.dto.LoginDto;
import com.booking.app.dto.RegistrationDTO;
import com.booking.app.dto.UserDTO;
import com.booking.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDTO toUserDto(User user);

    User toUser(UserDTO userDTO);

    LoginDto toLoginDto(User user);

    User toUser(LoginDto userDTO);

    RegistrationDTO toRegistrationDto(User user);

    User toUser(RegistrationDTO registrationDTO);

    EmailDto toEmailDto(User user);

}
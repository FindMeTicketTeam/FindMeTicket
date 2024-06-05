package com.booking.app.dto;

import com.booking.app.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RoleDto {

    private final int id;

    @NotNull
    private final Role.RoleType type;

}

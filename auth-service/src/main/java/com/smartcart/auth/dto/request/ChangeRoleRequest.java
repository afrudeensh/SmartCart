package com.smartcart.auth.dto.request;

import com.smartcart.auth.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotNull
    private RoleName role;
}
package com.debuggeandoideas.JobBoardAPI.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchApplicationStatusRequest {

    @NotNull(message = "El status es obligatorio")
    @Pattern(regexp = "accepted|rejected", message = "El status debe ser accepted o rejected")
    private String status;
}

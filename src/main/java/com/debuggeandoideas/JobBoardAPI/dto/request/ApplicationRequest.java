package com.debuggeandoideas.JobBoardAPI.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotNull(message = "El candidate_id es obligatorio.")
    @Positive(message = "El candidate_id debe ser un número positivo.")
    @JsonProperty("candidate_id")
    private Long candidateId;

    @NotNull(message = "El job_id es obligatorio.")
    @Positive(message = "El job_id debe ser un número positivo.")
    @JsonProperty("job_id")
    private Long jobId;
}

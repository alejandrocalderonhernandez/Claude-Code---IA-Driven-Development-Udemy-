package com.debuggeandoideas.JobBoardAPI.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    @NotBlank(message = "El título no puede estar vacío.")
    @Size(max = 255, message = "El título no puede superar 255 caracteres.")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía.")
    private String description;

    @NotBlank(message = "El nombre de la empresa es obligatorio.")
    @Size(max = 255, message = "El nombre de la empresa no puede superar 255 caracteres.")
    private String company;

    @NotBlank(message = "La ubicación es obligatoria.")
    @Size(max = 255, message = "La ubicación no puede superar 255 caracteres.")
    private String location;
}

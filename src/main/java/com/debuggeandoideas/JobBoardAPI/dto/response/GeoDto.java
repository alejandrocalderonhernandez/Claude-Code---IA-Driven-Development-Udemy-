package com.debuggeandoideas.JobBoardAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoDto {
    private String lat;
    private String lng;
}

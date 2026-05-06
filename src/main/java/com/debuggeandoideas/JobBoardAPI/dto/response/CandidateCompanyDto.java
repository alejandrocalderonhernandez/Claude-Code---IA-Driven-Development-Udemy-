package com.debuggeandoideas.JobBoardAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateCompanyDto {
    private String name;
    private String catchPhrase;
    private String bs;
}

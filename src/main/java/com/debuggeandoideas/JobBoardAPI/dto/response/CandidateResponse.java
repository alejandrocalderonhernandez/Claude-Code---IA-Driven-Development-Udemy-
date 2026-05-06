package com.debuggeandoideas.JobBoardAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String website;
    private CandidateAddressDto address;
    private CandidateCompanyDto company;
}

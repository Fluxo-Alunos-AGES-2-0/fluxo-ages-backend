package com.fluxo.board.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDTO {
    private Integer id;
    private String name;
    private String email;
    private String avatarUrl;
    private Integer agesLevel;
    private ProjectDTO currentProject;
    private AuxDTO professor;
    private AttendanceDTO attendance;
}

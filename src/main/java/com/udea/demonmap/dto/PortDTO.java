package com.udea.demonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar información de un puerto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortDTO {
    private Integer portNumber;
    private String protocol;
    private String state;
    private String service;
    private String version;
}

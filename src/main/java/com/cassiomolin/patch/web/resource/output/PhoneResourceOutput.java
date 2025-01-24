package com.cassiomolin.patch.web.resource.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneResourceOutput {

    private String phone;

    private String type;
}
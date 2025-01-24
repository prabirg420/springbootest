package com.cassiomolin.patch.web.resource.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResourceInput {

    @NotBlank
    private String name;

    private LocalDate birthday;

    private WorkResourceInput work;

    private List<PhoneResourceInput> phones;

    private List<EmailResourceInput> emails;

    private List<String> groups;

    private Boolean favorite;

    private String notes;
}
package com.cassiomolin.patch.web.resource.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResourceOutput {

    private Long id;

    private String name;

    private LocalDate birthday;

    private WorkResourceOutput work;

    private List<PhoneResourceOutput> phones;

    private List<EmailResourceOutput> emails;

    private List<String> groups;

    private Boolean favorite;

    private String notes;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime lastModifiedDateTime;
}

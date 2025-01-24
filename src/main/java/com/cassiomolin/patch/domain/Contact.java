package com.cassiomolin.patch.domain;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contact {

    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String name;

    private LocalDate birthday;

    private Work work;

    private List<Phone> phones;

    private List<Email> emails;

    private List<String> groups;

    private Boolean favorite;

    private String notes;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime lastModifiedDateTime;
}
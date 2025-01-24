package com.cassiomolin.patch.web.mapper;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.web.resource.input.ContactResourceInput;
import com.cassiomolin.patch.web.resource.output.ContactResourceOutput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface ContactMapper {

    Contact asContact(ContactResourceInput resourceInput);

    ContactResourceInput asInput(Contact contact);

    void update(@MappingTarget Contact contact, ContactResourceInput resourceInput);

    ContactResourceOutput asOutput(Contact contact);

    List<ContactResourceOutput> asOutput(List<Contact> contacts);
}

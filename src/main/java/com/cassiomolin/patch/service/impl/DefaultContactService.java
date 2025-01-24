package com.cassiomolin.patch.service.impl;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.service.ContactService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DefaultContactService implements ContactService {

    private static final List<Contact> CONTACTS = new ArrayList<>();

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    @Override
    public Contact createContact(Contact contact) {
        contact.setId(ID_GENERATOR.incrementAndGet());
        contact.setCreatedDateTime(OffsetDateTime.now(ZoneOffset.UTC));
        contact.setLastModifiedDateTime(OffsetDateTime.now(ZoneOffset.UTC));
        CONTACTS.add(contact);
        return contact;
    }

    @Override
    public List<Contact> findContacts() {
        return CONTACTS;
    }

    @Override
    public Optional<Contact> findContact(Long id) {
        return CONTACTS.stream()
                .filter(contact -> id.equals(contact.getId()))
                .findFirst();
    }

    @Override
    public void updateContact(Contact contact) {
        contact.setLastModifiedDateTime(OffsetDateTime.now(ZoneOffset.UTC));
        CONTACTS.set(CONTACTS.indexOf(contact), contact);
    }

    @Override
    public void deleteContact(Contact contact) {
        CONTACTS.remove(contact);
    }
}
package com.cassiomolin.patch.web.controller;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.service.ContactService;
import com.cassiomolin.patch.web.PatchMediaType;
import com.cassiomolin.patch.web.exception.ResourceNotFoundException;
import com.cassiomolin.patch.web.mapper.ContactMapper;
import com.cassiomolin.patch.web.resource.input.ContactResourceInput;
import com.cassiomolin.patch.web.resource.output.ContactResourceOutput;
import com.cassiomolin.patch.web.util.PatchHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMapper mapper;

    private final ContactService service;

    private final PatchHelper patchHelper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateContact(@Valid @RequestBody ContactResourceInput contactResource) {

        Contact contact = mapper.asContact(contactResource);
        Contact contactCreated = service.createContact(contact);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(contactCreated.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ContactResourceOutput>> findContacts() {

        List<Contact> contacts = service.findContacts();
        List<ContactResourceOutput> contactResources = mapper.asOutput(contacts);

        return ResponseEntity.ok(contactResources);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactResourceOutput> findContact(@PathVariable Long id) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        ContactResourceOutput contactResource = mapper.asOutput(contact);

        return ResponseEntity.ok(contactResource);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                              @RequestBody @Valid ContactResourceInput contactResource) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        mapper.update(contact, contactResource);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
    public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                              @RequestBody JsonPatch patchDocument) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        ContactResourceInput contactResource = mapper.asInput(contact);
        ContactResourceInput contactResourcePatched = patchHelper.patch(patchDocument, contactResource, ContactResourceInput.class);

        mapper.update(contact, contactResourcePatched);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}", consumes = PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Void> updateContact(@PathVariable Long id,
                                              @RequestBody JsonMergePatch mergePatchDocument) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        ContactResourceInput contactResource = mapper.asInput(contact);
        ContactResourceInput contactResourcePatched = patchHelper.mergePatch(mergePatchDocument, contactResource, ContactResourceInput.class);

        mapper.update(contact, contactResourcePatched);
        service.updateContact(contact);

        return ResponseEntity.noContent().build();
    }



    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {

        Contact contact = service.findContact(id).orElseThrow(ResourceNotFoundException::new);
        service.deleteContact(contact);

        return ResponseEntity.noContent().build();
    }


}

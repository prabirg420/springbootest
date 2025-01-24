package com.cassiomolin.patch.web.controller;

import com.cassiomolin.patch.config.JacksonConfig;
import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.service.ContactService;
import com.cassiomolin.patch.web.PatchMediaType;
import com.cassiomolin.patch.web.exception.WebApiExceptionHandler;
import com.cassiomolin.patch.web.mapper.ContactMapper;
import com.cassiomolin.patch.web.mapper.ContactMapperImpl;
import com.cassiomolin.patch.web.resource.input.ContactResourceInput;
import com.cassiomolin.patch.web.util.PatchHelper;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({
        ContactController.class,
        ContactMapperImpl.class,
        PatchHelper.class,
        JacksonConfig.class,
        WebApiExceptionHandler.class
})
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService service;

    @SpyBean
    private ContactMapper mapper;

    @SpyBean
    private PatchHelper patchHelper;

    @Test
    @SneakyThrows
    public void createContact_shouldReturn201_whenInputIsValid() {

        Contact contactPersisted = contactPersisted();
        when(service.createContact(any(Contact.class))).thenReturn(contactPersisted);

        mockMvc.perform(post("/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fromFile("json/contact/post-with-valid-payload.json")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern("http://*/contacts/" + contactPersisted.getId()));

        verify(mapper).asContact(any(ContactResourceInput.class));

        ArgumentCaptor<Contact> contactArgumentCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(service).createContact(contactArgumentCaptor.capture());
        verifyNoMoreInteractions(service);

        verifyZeroInteractions(patchHelper);

        assertThat(contactArgumentCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(contactToPersist());
    }

    @Test
    @SneakyThrows
    public void findContact_shouldReturn200_whenContactExists() {

        Contact contactPersisted = contactPersisted();
        when(service.findContact(anyLong())).thenReturn(Optional.of(contactPersisted));

        mockMvc.perform(get("/contacts/{id}", 1)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Appleseed"))
                .andExpect(jsonPath("$.createdDateTime").value("2019-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.lastModifiedDateTime").value("2019-01-01T00:00:00Z"));

        verify(service).findContact(anyLong());
        verifyNoMoreInteractions(service);

        verifyZeroInteractions(patchHelper);

        verify(mapper).asOutput(any(Contact.class));
    }

    @Test
    @SneakyThrows
    public void findContacts_shouldReturn200_whenThereIsNoContact() {

        when(service.findContacts()).thenReturn(Lists.newArrayList());

        mockMvc.perform(get("/contacts")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).findContacts();
        verifyNoMoreInteractions(service);

        verifyZeroInteractions(patchHelper);

        verify(mapper).asOutput(anyList());
    }

    @Test
    @SneakyThrows
    public void findContacts_shouldReturn200_whenThereAreContacts() {

        when(service.findContacts()).thenReturn(Lists.list(contactPersisted()));

        mockMvc.perform(get("/contacts")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].*", hasSize(4)))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("John Appleseed"))
                .andExpect(jsonPath("$.[0].createdDateTime").value("2019-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.[0].lastModifiedDateTime").value("2019-01-01T00:00:00Z"));

        verify(service).findContacts();
        verifyNoMoreInteractions(service);

        verifyZeroInteractions(patchHelper);

        verify(mapper).asOutput(anyList());
    }

    @Test
    @SneakyThrows
    public void updateContact_shouldReturn204_whenInputIsValidAndContactExists() {

        when(service.findContact(anyLong())).thenReturn(Optional.of(contactPersisted()));

        mockMvc.perform(put("/contacts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(fromFile("json/contact/put-with-valid-payload.json")))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(mapper).update(any(Contact.class), any(ContactResourceInput.class));

        ArgumentCaptor<Contact> contactArgumentCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(service).findContact(anyLong());
        verify(service).updateContact(contactArgumentCaptor.capture());
        verifyNoMoreInteractions(service);

        verifyZeroInteractions(patchHelper);

        assertThat(contactArgumentCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(contactToUpdate());
    }

    @Test
    @SneakyThrows
    public void updateContactUsingJsonPatch_shouldReturn204_whenInputIsValidAndContactExists() {

        when(service.findContact(anyLong())).thenReturn(Optional.of(contactPersisted()));

        mockMvc.perform(patch("/contacts/{id}", 1L)
                .contentType(PatchMediaType.APPLICATION_JSON_PATCH_VALUE)
                .content(fromFile("json/contact/patch-with-valid-json-patch-payload.json")))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(mapper).asInput(any(Contact.class));
        verify(mapper).update(any(Contact.class), any(ContactResourceInput.class));

        verify(patchHelper).patch(any(JsonPatch.class), isA(ContactResourceInput.class), eq(ContactResourceInput.class));
        verifyNoMoreInteractions(patchHelper);

        ArgumentCaptor<Contact> contactArgumentCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(service).findContact(anyLong());
        verify(service).updateContact(contactArgumentCaptor.capture());
        verifyNoMoreInteractions(service);

        assertThat(contactArgumentCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(contactToUpdate());
    }

    @Test
    @SneakyThrows
    public void updateContactUsingJsonMergePatch_shouldReturn204_whenInputIsValidAndContactExists() {

        when(service.findContact(anyLong())).thenReturn(Optional.of(contactPersisted()));

        mockMvc.perform(patch("/contacts/{id}", 1L)
                .contentType(PatchMediaType.APPLICATION_MERGE_PATCH_VALUE)
                .content(fromFile("json/contact/patch-with-valid-json-merge-patch-payload.json")))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(mapper).asInput(any(Contact.class));
        verify(mapper).update(any(Contact.class), any(ContactResourceInput.class));

        verify(patchHelper).mergePatch(any(JsonMergePatch.class), isA(ContactResourceInput.class), eq(ContactResourceInput.class));
        verifyNoMoreInteractions(patchHelper);

        ArgumentCaptor<Contact> contactArgumentCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(service).findContact(anyLong());
        verify(service).updateContact(contactArgumentCaptor.capture());
        verifyNoMoreInteractions(service);

        assertThat(contactArgumentCaptor.getValue()).isEqualToComparingFieldByFieldRecursively(contactToUpdate());
    }

    @Test
    @SneakyThrows
    public void deleteContact_shouldReturn204_whenContactExists() {

        Contact contact = mock(Contact.class);
        when(service.findContact(anyLong())).thenReturn(Optional.of(contact));

        mockMvc.perform(delete("/contacts/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verifyZeroInteractions(mapper);

        verifyZeroInteractions(patchHelper);

        verify(service).findContact(anyLong());
        verify(service).deleteContact(any(Contact.class));
        verifyNoMoreInteractions(service);
    }

    @SneakyThrows
    private byte[] fromFile(String path) {
        return new ClassPathResource(path).getInputStream().readAllBytes();
    }

    private Contact contactToPersist() {

        return Contact.builder()
                .name("John Appleseed")
                .build();
    }

    private Contact contactPersisted() {

        return Contact.builder()
                .id(1L)
                .name("John Appleseed")
                .createdDateTime(OffsetDateTime.parse("2019-01-01T00:00:00Z"))
                .lastModifiedDateTime(OffsetDateTime.parse("2019-01-01T00:00:00Z"))
                .build();
    }

    private Contact contactToUpdate() {

        return Contact.builder()
                .id(1L)
                .name("Johnny Appleseed")
                .createdDateTime(OffsetDateTime.parse("2019-01-01T00:00:00Z"))
                .lastModifiedDateTime(OffsetDateTime.parse("2019-01-01T00:00:00Z"))
                .build();
    }
}

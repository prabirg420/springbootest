package com.cassiomolin.patch;

import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.domain.Phone;
import com.cassiomolin.patch.domain.Work;
import com.cassiomolin.patch.service.ContactService;
import com.cassiomolin.patch.web.PatchMediaType;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.time.LocalDate;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PatchTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        setupTestData();
    }

    @Test
    @SneakyThrows
    public void updateContact_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateContact(id, fromFile("json/contact/put.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findContact(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(6))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.name", is("John W. Appleseed"))
                .assertThat("$.work.*", hasSize(2))
                .assertThat("$.work.company", is("Acme"))
                .assertThat("$.work.title", is("Senior Engineer"))
                .assertThat("$.phones", hasSize(2))
                .assertThat("$.phones[0].*", hasSize(2))
                .assertThat("$.phones[0].phone", is("1111111111"))
                .assertThat("$.phones[0].type", is("work"))
                .assertThat("$.phones[1].*", hasSize(1))
                .assertThat("$.phones[1].phone", is("2222222222"))
                .assertThat("$.favorite", is(true));
    }

    @Test
    @SneakyThrows
    public void updateContactUsingJsonPatch_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateContactUsingJsonPatch(id, fromFile("json/contact/json-patch.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findContact(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(6))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.name", is("John W. Appleseed"))
                .assertThat("$.work.*", hasSize(2))
                .assertThat("$.work.company", is("Acme"))
                .assertThat("$.work.title", is("Senior Engineer"))
                .assertThat("$.phones", hasSize(2))
                .assertThat("$.phones[0].*", hasSize(2))
                .assertThat("$.phones[0].phone", is("1111111111"))
                .assertThat("$.phones[0].type", is("work"))
                .assertThat("$.phones[1].*", hasSize(1))
                .assertThat("$.phones[1].phone", is("2222222222"))
                .assertThat("$.favorite", is(true));
    }

    @Test
    @SneakyThrows
    public void updateContactUsingJsonMergePatch_shouldSucceed() {

        Long id = 1L;
        ResponseEntity<String> patchResponse = updateContactUsingJsonMergePatch(id, fromFile("json/contact/merge-patch.json"));

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(patchResponse.getBody()).isNull();

        ResponseEntity<String> findResponse = findContact(id);
        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        with(findResponse.getBody())
                .assertThat("$.*", hasSize(6))
                .assertThat("$.id", is(id.intValue()))
                .assertThat("$.name", is("John W. Appleseed"))
                .assertThat("$.work.*", hasSize(2))
                .assertThat("$.work.company", is("Acme"))
                .assertThat("$.work.title", is("Senior Engineer"))
                .assertThat("$.phones", hasSize(2))
                .assertThat("$.phones[0].*", hasSize(2))
                .assertThat("$.phones[0].phone", is("1111111111"))
                .assertThat("$.phones[0].type", is("work"))
                .assertThat("$.phones[1].*", hasSize(1))
                .assertThat("$.phones[1].phone", is("2222222222"))
                .assertThat("$.favorite", is(true));
    }

    private ResponseEntity<String> findContact(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/contacts/{id}", HttpMethod.GET, new HttpEntity<>(headers), String.class, id);
    }

    private ResponseEntity<String> updateContact(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/contacts/{id}", HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class, id);
    }

    private ResponseEntity<String> updateContactUsingJsonPatch(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PatchMediaType.APPLICATION_JSON_PATCH);
        return restTemplate.exchange("/contacts/{id}", HttpMethod.PATCH, new HttpEntity<>(payload, headers), String.class, id);
    }

    private ResponseEntity<String> updateContactUsingJsonMergePatch(Long id, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PatchMediaType.APPLICATION_MERGE_PATCH);
        return restTemplate.exchange("/contacts/{id}", HttpMethod.PATCH, new HttpEntity<>(payload, headers), String.class, id);
    }

    @SneakyThrows
    private String fromFile(String path) {
        return StreamUtils.copyToString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }

    private void setupTestData() {

        contactService.createContact(Contact.builder()
                .name("John Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Engineer").build())
                .phones(Lists.newArrayList(Phone.builder().phone("0000000000").build()))
                .notes("Cool guy!")
                .build());

        contactService.createContact(Contact.builder()
                .name("James Doe")
                .phones(Lists.newArrayList(Phone.builder().phone("3333333333").type("mobile").build()))
                .favorite(true)
                .build());
    }
}

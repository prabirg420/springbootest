package com.cassiomolin.patch.web.util;


import com.cassiomolin.patch.config.JacksonConfig;
import com.cassiomolin.patch.domain.Contact;
import com.cassiomolin.patch.domain.Phone;
import com.cassiomolin.patch.domain.Work;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.json.*;
import javax.validation.Validator;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({JacksonConfig.class, PatchHelper.class})
public class PatchHelperTest {

    @MockBean
    private Validator validator;

    @Autowired
    private PatchHelper patchHelper;

    @Test
    public void patch_shouldPatchDocument() {

        when(validator.validate(any())).thenReturn(Sets.newHashSet());

        Contact target = Contact.builder()
                .id(1L)
                .name("John Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Engineer").build())
                .phones(Lists.newArrayList(Phone.builder().phone("0000000000").build()))
                .notes("Cool guy!")
                .favorite(false)
                .build();

        JsonPatch patch = Json.createPatchBuilder()
                .replace("/name", "John W. Appleseed")
                .replace("/work/title", "Senior Engineer")
                .replace("/phones/0/phone", "1111111111")
                .add("/phones/0/type", "work")
                .add("/phones/1", JsonObject.EMPTY_JSON_OBJECT)
                .add("/phones/1/phone", "2222222222")
                .remove("/notes")
                .replace("/favorite", JsonValue.TRUE)
                .build();

        Contact expected = Contact.builder()
                .id(1L)
                .name("John W. Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Senior Engineer").build())
                .phones(Lists.newArrayList(
                        Phone.builder().phone("1111111111").type("work").build(),
                        Phone.builder().phone("2222222222").build()))
                .favorite(true)
                .build();

        Contact result = patchHelper.patch(patch, target, Contact.class);
        assertThat(result).isEqualToComparingFieldByField(expected);

        verify(validator).validate(any());
    }

    @Test
    public void mergePatch_shouldMergePatchDocument() {

        when(validator.validate(any())).thenReturn(Sets.newHashSet());

        Contact target = Contact.builder()
                .id(1L)
                .name("John Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Engineer").build())
                .phones(Lists.newArrayList(Phone.builder().phone("0000000000").build()))
                .notes("Cool guy!")
                .favorite(false)
                .build();

        JsonMergePatch mergePatch = Json.createMergePatch(Json.createObjectBuilder()
                .add("name", "John W. Appleseed")
                .add("work", Json.createObjectBuilder()
                        .add("title", "Senior Engineer"))
                .add("phones", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("phone", "1111111111")
                                .add("type", "work"))
                        .add(Json.createObjectBuilder()
                                .add("phone", "2222222222"))
                )
                .add("notes", JsonValue.NULL)
                .add("favorite", JsonValue.TRUE)
                .build());

        Contact expected = Contact.builder()
                .id(1L)
                .name("John W. Appleseed")
                .birthday(LocalDate.parse("1990-01-01"))
                .work(Work.builder().company("Acme").title("Senior Engineer").build())
                .phones(Lists.newArrayList(
                        Phone.builder().phone("1111111111").type("work").build(),
                        Phone.builder().phone("2222222222").build()))
                .favorite(true)
                .build();

        Contact result = patchHelper.mergePatch(mergePatch, target, Contact.class);
        assertThat(result).isEqualToComparingFieldByField(expected);

        verify(validator).validate(any());
    }
}

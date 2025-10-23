package com.eaglebank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createUser_withAddressAndPhone_returnsCreatedAndContainsFields() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"Test User\",\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"phoneNumber\": \"07777555444\",\n" +
                "  \"address\": {\n" +
                "    \"line1\": \"1 Main St\",\n" +
                "    \"line2\": \"Suite 1\",\n" +
                "    \"line3\": \"Building A\",\n" +
                "    \"town\": \"Townsville\",\n" +
                "    \"county\": \"Countyshire\",\n" +
                "    \"postcode\": \"AB12 3CD\"\n" +
                "  }\n" +
                "}";

        String result = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(result);
        assertThat(node.get("id").asText()).startsWith("usr-");
        assertThat(node.get("name").asText()).isEqualTo("Test User");
        assertThat(node.get("email").asText()).isEqualTo("user@example.com");
        assertThat(node.get("phoneNumber").asText()).isEqualTo("07777555444");
        JsonNode addr = node.get("address");
        assertThat(addr).isNotNull();
        assertThat(addr.get("line1").asText()).isEqualTo("1 Main St");
        assertThat(addr.get("postcode").asText()).isEqualTo("AB12 3CD");
    }

    @Test
    public void patchUser_partialAddressMerge_preservesUnspecifiedFields() throws Exception {
        // First create a user with a full address
        String createPayload = "{\n" +
                "  \"name\": \"Patch Test\",\n" +
                "  \"email\": \"patch@example.com\",\n" +
                "  \"phoneNumber\": \"07777666556\",\n" +
                "  \"address\": {\n" +
                "    \"line1\": \"Old Line1\",\n" +
                "    \"line2\": \"Old Line2\",\n" +
                "    \"town\": \"OldTown\",\n" +
                "    \"county\": \"OldCounty\",\n" +
                "    \"postcode\": \"OLD 1AA\"\n" +
                "  }\n" +
                "}";

        String createResult = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResult);
        String id = created.get("id").asText();

        // Now patch only the postcode
        String patchPayload = "{\n" +
                "  \"address\": {\n" +
                "    \"postcode\": \"NEW 9ZZ\"\n" +
                "  }\n" +
                "}";

        String patchResult = mockMvc.perform(patch("/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode patched = objectMapper.readTree(patchResult);
        JsonNode addr = patched.get("address");
        assertThat(addr).isNotNull();
        // previously set values should be preserved
        assertThat(addr.get("line1").asText()).isEqualTo("Old Line1");
        assertThat(addr.get("line2").asText()).isEqualTo("Old Line2");
        assertThat(addr.get("town").asText()).isEqualTo("OldTown");
        // postcode should be updated
        assertThat(addr.get("postcode").asText()).isEqualTo("NEW 9ZZ");
    }
}

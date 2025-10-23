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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        // Now patch only the postcode; include user-id header matching the created id
        String patchPayload = "{\n" +
                "  \"address\": {\n" +
                "    \"postcode\": \"NEW 9ZZ\"\n" +
                "  }\n" +
                "}";

        String patchResult = mockMvc.perform(patch("/v1/users/" + id)
                        .header("user-id", id)
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

    @Test
    public void getUser_withValidHeader_returnsUser() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"Fetch User\",\n" +
                "  \"email\": \"fetch@example.com\",\n" +
                "  \"phoneNumber\": \"07777000001\",\n" +
                "  \"address\": {\n" +
                "    \"line1\": \"1 Fetch Rd\",\n" +
                "    \"line2\": \"Unit 2\",\n" +
                "    \"town\": \"FetchTown\",\n" +
                "    \"county\": \"FetchCounty\",\n" +
                "    \"postcode\": \"FC1 1FC\"\n" +
                "  }\n" +
                "}";

        String createResult = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResult);
        String id = created.get("id").asText();

        String getResult = mockMvc.perform(get("/v1/users/" + id)
                        .header("user-id", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode fetched = objectMapper.readTree(getResult);
        assertThat(fetched.get("id").asText()).isEqualTo(id);
        assertThat(fetched.get("name").asText()).isEqualTo("Fetch User");
    }

    @Test
    public void getUser_mismatchedHeader_returnsForbidden() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"Private User\",\n" +
                "  \"email\": \"private@example.com\",\n" +
                "  \"phoneNumber\": \"07777000002\",\n" +
                "  \"address\": {\n" +
                "    \"line1\": \"5 Private St\",\n" +
                "    \"line2\": \"Flat 3\",\n" +
                "    \"town\": \"PrivTown\",\n" +
                "    \"county\": \"PrivCounty\",\n" +
                "    \"postcode\": \"PV1 2PV\"\n" +
                "  }\n" +
                "}";

        String createResult = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResult);
        String id = created.get("id").asText();

        mockMvc.perform(get("/v1/users/" + id)
                        .header("user-id", "usr-xxxxx"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void patchUser_mismatchedHeader_returnsForbidden() throws Exception {
        String createPayload = "{\n" +
                "  \"name\": \"Patch Private\",\n" +
                "  \"email\": \"patchpriv@example.com\",\n" +
                "  \"phoneNumber\": \"07777000003\",\n" +
                "  \"address\": {\n" +
                "    \"line1\": \"9 Patch St\",\n" +
                "    \"line2\": \"Apt 4\",\n" +
                "    \"town\": \"PatchTown\",\n" +
                "    \"county\": \"PatchCounty\",\n" +
                "    \"postcode\": \"PT9 9PT\"\n" +
                "  }\n" +
                "}";

        String createResult = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResult);
        String id = created.get("id").asText();

        String patchPayload = "{\n" +
                "  \"phoneNumber\": \"07000000000\"\n" +
                "}";

        mockMvc.perform(patch("/v1/users/" + id)
                        .header("user-id", "usr-xxxxx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isForbidden());
    }
}

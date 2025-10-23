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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createAccount_withValidType_returnsCreated() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"Personal Bank Account\",\n" +
                "  \"accountType\": \"personal\"\n" +
                "}";

        String result = mockMvc.perform(post("/v1/accounts")
                        .header("user-id", "usr-abc12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(result);
        assertThat(node.get("accountNumber").asText()).matches("^\\d{8}$");
        assertThat(node.get("sortCode").asText()).matches("^\\d{2}-\\d{2}-\\d{2}$");
        assertThat(node.get("name").asText()).isEqualTo("Personal Bank Account");
        assertThat(node.get("accountType").asText()).isEqualTo("personal");
        assertThat(node.get("balance").asInt()).isEqualTo(0);
        assertThat(node.get("currency").asText()).isEqualTo("GBP");
    }

    @Test
    public void createAccount_withInvalidType_returnsBadRequest() throws Exception {
        String payload = "{\n" +
                "  \"name\": \"Kid Account\",\n" +
                "  \"accountType\": \"kids\"\n" +
                "}";

        mockMvc.perform(post("/v1/accounts")
                        .header("user-id", "usr-xyz09")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAccount_existingId_returnsAccount() throws Exception {
        // create an account first with known user-id
        String payload = "{\n" +
                "  \"name\": \"Fetchable Account\",\n" +
                "  \"accountType\": \"personal\"\n" +
                "}";

        String createResult = mockMvc.perform(post("/v1/accounts")
                        .header("user-id", "usr-get01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResult);

        // now fetch by the account id (which is the user-id header used when creating)
        String getResult = mockMvc.perform(get("/v1/accounts/usr-get01"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode fetched = objectMapper.readTree(getResult);
        assertThat(fetched.get("accountNumber").asText()).isEqualTo(created.get("accountNumber").asText());
        assertThat(fetched.get("sortCode").asText()).isEqualTo(created.get("sortCode").asText());
        assertThat(fetched.get("name").asText()).isEqualTo("Fetchable Account");
        assertThat(fetched.get("accountType").asText()).isEqualTo("personal");
    }
}

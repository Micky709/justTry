package me.eddypbr.todolist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.eddypbr.todolist.user.UserModel;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String createdTaskId;
    private static final UUID FIXED_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static RequestPostProcessor withMockUser() {
        return request -> {
            UserModel user = new UserModel();
            user.setId(FIXED_USER_ID);
            user.setUsername("testuser");
            request.setAttribute("user", user);
            return request;
        };
    }

    @Test
    @Order(1)
    public void testCreateTask() throws Exception {
        String taskJson = """
            {
                "title": "Integration Task",
                "description": "Test",
                "startAt": "2026-04-20T10:00:00",
                "endAt": "2026-04-21T10:00:00"
            }
            """;

        String response = mockMvc.perform(post("/tasks")
                .with(withMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdTaskId = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        System.out.println("Created Task ID: " + createdTaskId);
    }

    @Test
    @Order(2)
    public void testUpdateTask() throws Exception {
    	System.out.println("Created Task ID: " + createdTaskId);
        String updateJson = """
            {
                "title": "Updated Integration Task"
            }
            """;

        mockMvc.perform(put("/tasks/" + createdTaskId)
                .with(withMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"));
    }

    @Test
    @Order(3)
    public void testDeleteTask() throws Exception {
        System.out.println("Deleting Task ID: " + createdTaskId);
        
        mockMvc.perform(delete("/tasks/" + createdTaskId)
                .with(withMockUser()))
                .andDo(result -> {
                    System.out.println("Delete Status: " + result.getResponse().getStatus());
                })
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void testCreateTaskWithInvalidDate() throws Exception {
        String invalidJson = """
            {
                "title": "Invalid Task",
                "startAt": "2026-04-21T10:00:00",
                "endAt": "2026-04-20T10:00:00"
            }
            """;

        mockMvc.perform(post("/tasks")
                .with(withMockUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void testDeleteNonExistingTask() throws Exception {
        String invalidId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(delete("/tasks/" + invalidId)
                .with(withMockUser()))
                .andExpect(status().isNotFound());
    }
}
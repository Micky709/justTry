package me.eddypbr.todolist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import me.eddypbr.todolist.task.*;
import me.eddypbr.todolist.user.UserModel;

public class UpdateTask {

    @InjectMocks
    private TaskController controller;

    @Mock
    private ITaskRepository repo;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource({
        "Updated Title 1, Updated Desc 1",
        "Updated Title 2, Updated Desc 2"
    })
    public void testUpdateTaskSuccess(String title, String description) throws Exception {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserModel user = new UserModel();
        user.setId(userId);

        TaskModel existing = new TaskModel();
        existing.setId(id);
        existing.setIdUser(userId);

        TaskModel update = new TaskModel();
        update.setTitle(title);
        update.setDescription(description);

        when(request.getAttribute("user")).thenReturn(user);
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);

        ResponseEntity response = controller.update(update, request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repo).save(any());
    }

    @ParameterizedTest
    @CsvSource({
        "00000000-0000-0000-0000-000000000001",
        "00000000-0000-0000-0000-000000000002"
    })
    public void testUpdateTaskNotFound(String idStr) {

        UUID id = UUID.fromString(idStr);

        TaskModel update = new TaskModel();

        when(repo.findById(id)).thenReturn(Optional.empty());

        ResponseEntity response = controller.update(update, request, id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateTaskEmptyTitle() throws Exception {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserModel user = new UserModel();
        user.setId(userId);

        TaskModel existing = new TaskModel();
        existing.setId(id);
        existing.setIdUser(userId);

        TaskModel update = new TaskModel();
        update.setTitle("");
        update.setDescription("desc");

        when(request.getAttribute("user")).thenReturn(user);
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);

        ResponseEntity response = controller.update(update, request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateTaskNullDescription() throws Exception {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserModel user = new UserModel();
        user.setId(userId);

        TaskModel existing = new TaskModel();
        existing.setId(id);
        existing.setIdUser(userId);

        TaskModel update = new TaskModel();
        update.setTitle("Valid Title");
        update.setDescription(null);

        when(request.getAttribute("user")).thenReturn(user);
        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);

        ResponseEntity response = controller.update(update, request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateTaskUnauthorizedUser() throws Exception {

        UUID id = UUID.randomUUID();

        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        TaskModel existing = new TaskModel();
        existing.setId(id);
        existing.setIdUser(ownerId);

        UserModel user = new UserModel();
        user.setId(otherUserId);

        TaskModel update = new TaskModel();
        update.setTitle("Test");

        when(request.getAttribute("user")).thenReturn(user);
        when(repo.findById(id)).thenReturn(Optional.of(existing));

        ResponseEntity response = controller.update(update, request, id);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
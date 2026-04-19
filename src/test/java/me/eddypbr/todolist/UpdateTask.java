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

    private UUID userId;
    private UUID taskId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
    }
    
    private UserModel mockUser() {
        UserModel user = new UserModel();
        user.setId(userId);
        return user;
    }

    private TaskModel mockTask() {
        TaskModel task = new TaskModel();
        task.setId(taskId);
        task.setIdUser(userId);
        return task;
    }

    @ParameterizedTest
    @CsvSource({"Updated Title 1, Updated Desc 1"})
    public void testUpdateTaskSuccess(String title, String description) throws Exception {
        when(request.getAttribute("user")).thenReturn(mockUser());
        when(repo.findById(taskId)).thenReturn(Optional.of(mockTask()));
        when(repo.save(any())).thenReturn(mockTask());
        TaskModel update = new TaskModel();
        update.setTitle(title);
        update.setDescription(description);
        ResponseEntity response = controller.update(update, request, taskId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repo, times(1)).save(any());
    }

    @ParameterizedTest
    @CsvSource({"00000000-0000-0000-0000-000000000001"})
    public void testUpdateTaskNotFound(String idStr) throws Exception {
        UUID id = UUID.fromString(idStr);
        when(repo.findById(id)).thenReturn(Optional.empty());
        ResponseEntity response = controller.update(new TaskModel(), request, id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void testUpdateTaskEmptyTitle() throws Exception {
        when(request.getAttribute("user")).thenReturn(mockUser());
        when(repo.findById(taskId)).thenReturn(Optional.of(mockTask()));
        TaskModel update = new TaskModel();
        update.setTitle("");  
        update.setDescription("desc");
        ResponseEntity response = controller.update(update, request, taskId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testUpdateTaskNullDescription() throws Exception {
        when(request.getAttribute("user")).thenReturn(mockUser());
        when(repo.findById(taskId)).thenReturn(Optional.of(mockTask()));
        TaskModel update = new TaskModel();
        update.setTitle("Valid Title");
        update.setDescription(null);
        ResponseEntity response = controller.update(update, request, taskId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateTaskUnauthorizedUser() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        TaskModel existing = new TaskModel();
        existing.setId(taskId);
        existing.setIdUser(otherUserId);
        when(request.getAttribute("user")).thenReturn(mockUser());
        when(repo.findById(taskId)).thenReturn(Optional.of(existing));
        TaskModel update = new TaskModel();
        update.setTitle("Test");
        ResponseEntity response = controller.update(update, request, taskId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;
    private ItemRequestDto itemRequestDto;
    private ItemRequestResponseDto itemRequestResponseDto;
    private User requester;

    @BeforeEach
    @Test
    void setObject() {
        requester = User.builder()
                .id(1L)
                .name("equal")
                .email("equal@email.com")
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("description of ItemRequestDto")
                .requester(requester)
                .build();
        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("description of ItemRequestResponseDto")
                .items(new ArrayList<>())
                .created(LocalDateTime.now())
                .build();
    }

    @SneakyThrows
    @Test
    void addRequest() {
        long userId = requester.getId();

        when(itemRequestService.addItemRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        String contentAsString = mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestDto), contentAsString);
        verify(itemRequestService).addItemRequest(userId, itemRequestDto);

    }

    @SneakyThrows
    @Test
    void getItemsByUserId() {
        long userId = requester.getId();

        when(itemRequestService.getItemsRequests(userId)).thenReturn(List.of(itemRequestResponseDto));

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get(("/requests"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestResponseDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var result = List.of(itemRequestResponseDto);
        assertEquals(objectMapper.writeValueAsString(result), contentAsString);
        verify(itemRequestService).getItemsRequests(userId);
    }

    @SneakyThrows
    @Test
    void returnAll() {
        long userId = requester.getId();
        int from = 0;
        int size = 20;

        when(itemRequestService.getAllRequests(userId, from, size))
                .thenReturn(List.of(itemRequestResponseDto));
        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get(("/requests/all"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestResponseDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var result = List.of(itemRequestResponseDto);
        assertEquals(objectMapper.writeValueAsString(result), contentAsString);
        verify(itemRequestService).getAllRequests(userId, from, size);

    }

    @SneakyThrows
    @Test
    void get() {
        long userId = requester.getId();
        long requestId = itemRequestResponseDto.getId();


        when(itemRequestService.getRequestById(userId, requestId)).thenReturn(itemRequestResponseDto);

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/requests/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequestResponseDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestResponseDto), contentAsString);
        verify(itemRequestService).getRequestById(userId, requestId);

    }
}
//package com.plenkov.cloudstorage.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.plenkov.cloudstorage.dto.request.RegisterRequestDto;
//import com.plenkov.cloudstorage.dto.request.SignInRequestDto;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = AuthController.class)
//@Import({AuthService.class})
//public class AuthControllerTest {
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    public void registerTest() throws Exception {
//        RegisterRequestDto req = new RegisterRequestDto("user_1", "password");
//
//        String json = objectMapper.writeValueAsString(req);
//
//        mockMvc.perform(post("http://localhost:8080/api/auth/sign-up")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.username").value("user_1"));
//    }
//
//    @Test
//    public void loginTest() throws Exception {
//        SignInRequestDto req = new SignInRequestDto("user_1", "password");
//
//        String json = objectMapper.writeValueAsString(req);
//
//        mockMvc.perform(post("http://localhost:8080/api/auth/sign-in")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.username").value("user_1"));
//    }
//
//    @Test
//    public void signOutTest() throws Exception {
//        mockMvc.perform(post("http://localhost:8080/api/auth/sign-out"))
//                .andExpect(status().isNoContent());
//    }
//}

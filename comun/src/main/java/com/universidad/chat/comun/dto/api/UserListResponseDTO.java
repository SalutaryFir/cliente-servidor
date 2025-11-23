package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para listado de usuarios del servidor vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDTO implements Serializable {
    private int totalUsers;
    private int connectedUsers;
    private List<UserInfoDTO> users;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO implements Serializable {
        private String username;
        private String email;
        private boolean online;
        private String lastConnection;
    }
}

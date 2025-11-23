package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para listado de canales del servidor vía API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelListResponseDTO implements Serializable {
    private int totalChannels;
    private List<ChannelInfoDTO> channels;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelInfoDTO implements Serializable {
        private String channelName;
        private String creator;
        private int memberCount;
        private int messageCount;
        private String createdAt;
        private boolean federated;
    }
}

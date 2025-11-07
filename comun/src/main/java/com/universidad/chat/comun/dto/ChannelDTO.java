package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChannelDTO implements Serializable {
    private String channelName;
    private String creatorUsername;
}
package com.universidad.chat.comun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class LoginSuccessDTO implements Serializable {
    private UserDTO userInfo;
    private List<String> allUsernames;
    private List<String> allChannelNames; // <-- AÑADE ESTA LÍNEA
}
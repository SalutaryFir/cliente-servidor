package com.universidad.chat.comun.dto;

import java.io.Serializable;

public enum ActionType implements Serializable {
    // Acciones del Cliente al Servidor
    REGISTER_REQUEST,
    LOGIN_REQUEST,
    SEND_MESSAGE_TO_USER,
    SEND_MESSAGE_TO_CHANNEL,
    CREATE_CHANNEL_REQUEST,
    INVITE_USER_REQUEST,
    INVITATION_RESPONSE,
    DOWNLOAD_AUDIO_REQUEST,
    UPLOAD_AUDIO,            // <-- AÑADE ESTA LÍNEA


    // Acciones del Servidor al Cliente
    REGISTER_SUCCESS,
    REGISTER_FAILURE,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    NEW_MESSAGE,
    USER_LIST_UPDATE,
    CHANNEL_INVITATION,  // Notificación al usuario que está siendo invitado
    INVITE_SUCCESS,      // Notificación al usuario que ENVIÓ la invitación
    INVITE_FAILURE,
    AUDIO_DATA_RESPONSE,
    MESSAGE_HISTORY,     // Historial de mensajes (privados o de canal)

    // Acciones nuevas para canales
    CREATE_CHANNEL_SUCCESS,
    CREATE_CHANNEL_FAILURE,
    CHANNEL_LIST_UPDATE,

    // Acciones de Servidor a Servidor (Federación)
    SERVER_REGISTER,         // Un servidor se registra con otro
    SERVER_HEARTBEAT,        // Keep-alive entre servidores
    SERVER_UNREGISTER,       // Un servidor se desconecta
    SERVER_USER_LIST_SYNC,   // Sincronización de usuarios disponibles
    SERVER_TOPOLOGY_SYNC,    // Sincronización de topología de red (lista de servidores)
    FEDERATED_MESSAGE,       // Mensaje de chat enrutado entre servidores
    FEDERATED_AUDIO,         // Audio enrutado entre servidores
    FEDERATED_CHANNEL_INVITE,// Invitación a canal entre servidores
    FEDERATED_INVITATION_RESPONSE // Respuesta de invitación entre servidores
}
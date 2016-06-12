package com.proyecto.enrique.osporthello.Interfaces;

import com.proyecto.enrique.osporthello.Models.User;

/**
 * Autor: Enrique Ramos
 * Fecha última actualización: 12/06/2016
 * Descripción: Interfaz para comunicar los cambios de un usuario en una lista.
 */

public interface UserInfoInterface {
    void onInfoUserChanges(User userInfo, int index);
}

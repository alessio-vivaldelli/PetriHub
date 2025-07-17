package it.model;

import org.junit.jupiter.api.Test;

import it.petrinet.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

  @Test
  void testUserCreationAndGetters() {
    User user = new User("testuser", "password123", true);

    assertEquals("testuser", user.getUsername(), "Il getter per username non funziona.");
    assertEquals("password123", user.getPassword(), "Il getter per la password non funziona.");
    assertTrue(user.isAdmin(), "Il getter per isAdmin non funziona.");
  }

  @Test
  void testCheckPassword() {
    User user = new User("testuser", "password123", false);
    assertTrue(user.checkPassword("password123"), "checkPassword dovrebbe ritornare true con la password corretta.");
    assertFalse(user.checkPassword("wrongpassword"), "checkPassword dovrebbe ritornare false con una password errata.");
    assertFalse(user.checkPassword(""), "checkPassword dovrebbe ritornare false con una password vuota.");
  }
}

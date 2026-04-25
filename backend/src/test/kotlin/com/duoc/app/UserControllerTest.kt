package com.duoc.app

import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository
) {

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()

        userRepository.save(
            User(
                email = "test@correo.com",
                name = "Usuario Test",
                password = "1234",
                role = UserRole.CLIENT
            )
        )
    }

    @Test
    fun `GET api users email devuelve usuario`() {
        mockMvc.get("/api/users/email/test@correo.com") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("test@correo.com") }
            jsonPath("$.name") { value("Usuario Test") }
        }
    }
}

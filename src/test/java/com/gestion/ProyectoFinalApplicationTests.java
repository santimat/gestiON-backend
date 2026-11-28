package com.gestion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// sin perfil no hay defaults para JWT_SECRET/MYSQL_*/MINIO_*: el test usa los de desarrollo
@ActiveProfiles("dev")
@SpringBootTest
class ProyectoFinalApplicationTests {

    @Test
    void contextLoads() {
    }

}

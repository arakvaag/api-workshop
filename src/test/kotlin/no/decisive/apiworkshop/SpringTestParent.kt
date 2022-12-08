package no.decisive.apiworkshop

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
abstract class SpringTestParent {

    @Autowired
    protected lateinit var brevService: BrevService

    @Autowired
    protected lateinit var brevRepository: BrevRepository

    @BeforeEach
    fun toemRepository() {
        brevRepository.slettAlt()
    }
}
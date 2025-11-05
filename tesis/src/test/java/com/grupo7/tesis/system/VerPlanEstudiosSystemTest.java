package com.grupo7.tesis.system;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class VerPlanEstudiosSystemTest {

    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    private static final String PENSUM_URL = "/pensum/view";

    private static final String TEST_PDF_PATH = "src/test/resources/informes/InformeAvance.pdf";

    private static final String PRIMER_NOMBRE = "TestUser";
    private static final String SEGUNDO_NOMBRE = "Playwright";
    private static final String PRIMER_APELLIDO = "Sistema";
    private static final String SEGUNDO_APELLIDO = "Test";
    private static final String CODIGO_ESTUDIANTIL = "99999999";
    private static final String CARRERA = "Ingeniería de Sistemas";
    private static final String ANIO_INGRESO = "2020";
    private static final String CORREO = "test.playwright@javeriana.edu.co";
    private static final String CONTRASENA = "Password123!";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeEach
    void init() {
        this.playwright = Playwright.create();

        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false));
                        // .setSlowMo(800));

        this.browserContext = browser.newContext();
        this.page = browserContext.newPage();

    
    }

    @AfterEach
    void end() {
        browser.close();
        playwright.close();
    }

    // Visualizar el progreso en el plan de estudios según histórico
    @Test
    public void testVerProgresoEnPlanHistorico() {

        realizarRegistro();
        cargarArchivoPDF();

        page.waitForTimeout(3000);
        seleccionarPlanEstudios();
        page.waitForTimeout(3000);

        String urlActual = page.url();
        assertEquals(SERVER_URL + PENSUM_URL, urlActual);

    }

    // Visualizar el progreso en el plan de estudios según malla curricular
    @Test
    public void testVerProgresoEnPlanMalla() {

        realizarRegistro();
        cargarArchivoPDF();

        page.waitForTimeout(3000);
        seleccionarPlanEstudios();
        page.waitForTimeout(2000);
        page.locator("label[for='vistaToggle']").click();
        page.waitForTimeout(3000);

        String urlActual = page.url();
        assertEquals(SERVER_URL + PENSUM_URL, urlActual);

    }

    private void realizarRegistro() {
        page.navigate(SERVER_URL + REGISTER_URL);
        page.waitForLoadState();

        page.locator("input[name='primerNombre']").fill(PRIMER_NOMBRE);
        page.locator("input[name='segundoNombre']").fill(SEGUNDO_NOMBRE);
        page.locator("input[name='primerApellido']").fill(PRIMER_APELLIDO);
        page.locator("input[name='segundoApellido']").fill(SEGUNDO_APELLIDO);
        page.locator("input[formControlName='codigo']").fill(CODIGO_ESTUDIANTIL);
        page.locator("select[formControlName='carrera']").selectOption(CARRERA);
        page.locator("input[formControlName='anioIngreso']").fill(ANIO_INGRESO);
        page.locator("input[name='correo']").fill(CORREO);
        page.locator("input[name='clave']").fill(CONTRASENA);
        page.locator("input[formControlName='confirmarContrasenia']").fill(CONTRASENA);

        page.waitForTimeout(500);

        page.locator("button[type='submit']").click();
    }

    private void cargarArchivoPDF() {

        Locator fileInput = page.locator("input[type='file']");

        Path filePath = Paths.get(TEST_PDF_PATH);
        fileInput.setInputFiles(filePath);

        page.waitForTimeout(200);

        page.locator("button.process-btn").click();
        page.waitForTimeout(600);

    }

    public void seleccionarPlanEstudios() {
        Locator planLink = page.locator("a:has-text(\"Plan de estudios\")");
        planLink.click();
    }
    
}

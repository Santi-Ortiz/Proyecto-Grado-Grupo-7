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
public class VerHistorialSystemTest {
    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    private static final String HISTORIAL_URL = "/historial";

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
                        .setHeadless(false)
                        .setSlowMo(800));

        this.browserContext = browser.newContext();
        this.page = browserContext.newPage();

    
    }

    @AfterEach
    void end() {
        browser.close();
        playwright.close();
    }

    @Test
    public void verHistorial(){
        realizarRegistro();
        cargarArchivoPDF();

        page.waitForTimeout(3000);

        page.waitForURL(SERVER_URL + HISTORIAL_URL, new Page.WaitForURLOptions().setTimeout(10000));

        scrollToElement("text=Requisitos de Grado");
        scrollToElement("text=Historial de Cursos");
        clickIfVisible("button:has-text('Ver más')");

        String urlActual = page.url();
        assertEquals(SERVER_URL + HISTORIAL_URL, urlActual);
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

    private void scrollToElement(String selector) {
        page.locator(selector).scrollIntoViewIfNeeded();
        page.waitForTimeout(800);
    }

    private void clickIfVisible(String selector) {
        Locator btn = page.locator(selector);
        if (btn.isVisible()) {
            btn.click();
            page.waitForTimeout(800);
        }
    }

}

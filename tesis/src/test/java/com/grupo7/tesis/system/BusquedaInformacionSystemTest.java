package com.grupo7.tesis.system;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class BusquedaInformacionSystemTest {

    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    private static final String XPATH_BUSQUEDA_NAVBAR = "//nav/a[@href='/busquedas']";
    private static final String XPATH_BUSQUEDA_TEXTAREA = "//div[contains(@class, 'formulario')]/textarea";
    private static final String XPATH_BUSQUEDA_BUTTON = "//div[contains(@class, 'formulario')]/button";

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

    private static final String PROMPT = "¿Qué significa estar en prueba académica?";

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
                        // .setSlowMo(100));
        this.browserContext = browser.newContext();
        this.page = browserContext.newPage();
    }

    @AfterEach
    void end() {
        browser.close();
        playwright.close();
    }


    // Realizar búsqueda de información exitosa
    @Test
    public void testBusquedaInformacionExitosa() {

        realizarRegistro();
        cargarArchivoPDF();

        page.locator(XPATH_BUSQUEDA_NAVBAR).click();
        page.locator(XPATH_BUSQUEDA_TEXTAREA).fill(PROMPT);
        page.locator(XPATH_BUSQUEDA_BUTTON).click();
        page.waitForTimeout(50000);

        assertEquals(true, page.locator("//div[contains(@class, 'respuesta-box')]//p").isVisible());
    }

    // Búsqueda con consulta vacía
    @Test
    public void testBusquedaConsultaVacia() {

        realizarRegistro();
        cargarArchivoPDF();

        page.locator(XPATH_BUSQUEDA_NAVBAR).click();
        page.locator(XPATH_BUSQUEDA_TEXTAREA).fill("");
        page.locator(XPATH_BUSQUEDA_BUTTON).click();
        page.waitForTimeout(5000);

        assertEquals(true, page.locator("//div[contains(@class, 'respuesta-box')]//p").isHidden());

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

}

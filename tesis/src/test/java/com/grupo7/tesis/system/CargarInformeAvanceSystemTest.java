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
public class CargarInformeAvanceSystemTest {

    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    private static final String LOGIN_URL = "/login";

    private static final String TEST_PDF_PATH = "src/test/resources/informes/InformeAvance.pdf";
    private static final String TEST_PDF_PATH_INVALID = "src/test/resources/informes/Plan_estudios_INGSIS.pdf";

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

    // Cargar informe de avance correctamente
    @Test
    public void testCargarInformeAvance() {

        realizarRegistro();
        cargarArchivoPDF();

        page.waitForTimeout(4000);
        String urlFinal = page.url();

        assertEquals(SERVER_URL + "/historial", urlFinal);

    }

    // Cargar informe con usuario existente
    @Test
    public void testCargarInformeAvanceUsuarioExistente() {

        crearUsuario();
        realizarLogin();
        cargarArchivoPDF();

        page.waitForTimeout(2000);

        String urlFinal = page.url();

        assertEquals(SERVER_URL + "/historial", urlFinal);

    }

    // Cargar informe sin autenticación
    @Test
    public void testCargarInformeSinAutenticacion() {

        page.navigate(SERVER_URL);
        page.waitForTimeout(2000);

        String urlActual = page.url();

        assertEquals(SERVER_URL + LOGIN_URL, urlActual);
    }

    // Cargar archivo inválido
    @Test
    public void testCargarArchivoInvalido() {

        realizarRegistro();
        Locator fileInput = page.locator("input[type='file']");
        fileInput.setInputFiles(Paths.get(TEST_PDF_PATH_INVALID));
        page.waitForTimeout(1000);

        page.locator("button.process-btn").click();
        page.waitForTimeout(3000);

        String urlActual = page.url();

        assertEquals(SERVER_URL + LOGIN_URL, urlActual);

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

    private void realizarLogin() {

        page.navigate(SERVER_URL + LOGIN_URL);

        page.locator("input[type='email'], input[name='correo']").fill("prueba@javeriana.edu.co");
        page.locator("input[type='password'], input[name='clave']").fill("admin123");

        page.waitForTimeout(500);

        page.locator("button[type='submit']").click();

        page.waitForTimeout(2000);
    }

    private void crearUsuario() {

        page.navigate(SERVER_URL + REGISTER_URL);
        page.waitForLoadState();

        page.locator("input[name='primerNombre']").fill("Santiago");
        page.locator("input[name='segundoNombre']").fill("Perez");
        page.locator("input[name='primerApellido']").fill("Ortiz");
        page.locator("input[name='segundoApellido']").fill("Alarcon");
        page.locator("input[formControlName='codigo']").fill("87654321");
        page.locator("select[formControlName='carrera']").selectOption("Ingeniería de Sistemas");
        page.locator("input[formControlName='anioIngreso']").fill("2021");
        page.locator("input[name='correo']").fill("prueba@javeriana.edu.co");
        page.locator("input[name='clave']").fill("admin123");
        page.locator("input[formControlName='confirmarContrasenia']").fill("admin123");

        page.waitForTimeout(500);
        page.locator("button[type='submit']").click();

        page.waitForTimeout(500);
        page.locator(".user-menu i.fa-user-circle").hover();
        page.locator(".user-menu .dropdown a:has-text('Cerrar sesión')").click();

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
